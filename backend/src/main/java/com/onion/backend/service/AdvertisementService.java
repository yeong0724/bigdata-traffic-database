package com.onion.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onion.backend.dto.advertisement.AdHistoryResult;
import com.onion.backend.dto.advertisement.AdvertisementDto;
import com.onion.backend.entity.*;
import com.onion.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdvertisementService {
    private static final String REDIS_KEY = "ad:";

    private final AdvertisementRepository advertisementRepository;
    private final AdViewHistoryRepository adViewHistoryRepository;
    private final AdClickHistoryRepository adClickHistoryRepository;
    private final AdViewStatRepository adViewStatRepository;
    private final AdClickStatRepository adClickStatRepository;

    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;

    private final ObjectMapper objectMapper;

    @Autowired
    public AdvertisementService(
            AdvertisementRepository advertisementRepository,
            AdViewHistoryRepository adViewHistoryRepository,
            AdClickHistoryRepository adClickHistoryRepository,
            AdViewStatRepository adViewStatRepository,
            AdClickStatRepository adClickStatRepository,
            RedisTemplate<String, Object> redisTemplate,
            MongoTemplate mongoTemplate,
            ObjectMapper objectMapper
    ) {
        this.advertisementRepository = advertisementRepository;
        this.adViewHistoryRepository = adViewHistoryRepository;
        this.adClickHistoryRepository = adClickHistoryRepository;
        this.adViewStatRepository = adViewStatRepository;
        this.adClickStatRepository = adClickStatRepository;
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Advertisement writeAd(AdvertisementDto advertisementDto) {
        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(advertisementDto.getTitle());
        advertisement.setContent(advertisementDto.getContent());
        // advertisement.setIsDeleted(advertisementDto.getIsDeleted());
        // advertisement.setIsVisible(advertisementDto.getIsVisible());
        advertisement.setStartDate(LocalDateTime.now());
        // advertisement.setEndDate(advertisementDto.getEndDate());
        advertisement.setViewCount(advertisementDto.getViewCount());
        advertisement.setClickCount(advertisementDto.getClickCount());

        advertisementRepository.save(advertisement);
        redisTemplate.opsForValue().set(REDIS_KEY + advertisement.getId(), advertisement);
        return advertisement;
    }

    public List<Advertisement> getAdList() {
        return advertisementRepository.findAll();
    }

    public Advertisement getAd(Long adId, String clientIp, Boolean isTrueView) {
        this.insertAdViewHistory(adId, clientIp, isTrueView);

        Object redisCache = redisTemplate.opsForValue().get(REDIS_KEY + adId.toString());
        if (redisCache != null) {
            return objectMapper.convertValue(redisCache, Advertisement.class);
        }

        return advertisementRepository.findById(adId).orElse(null);
    }

    private void insertAdViewHistory(Long adId, String clientIp, Boolean isTrueView) {
        AdViewHistory adViewHistory = new AdViewHistory();
        adViewHistory.setAdId(adId);
        adViewHistory.setClientIp(clientIp);
        adViewHistory.setIsTrueView(isTrueView);
        adViewHistory.setCreatedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!principal.equals("anonymousUser")) {
            UserDetails userDetails = (UserDetails) principal;
            adViewHistory.setUsername(userDetails.getUsername());
        }

        adViewHistoryRepository.save(adViewHistory);
    }

    public void clickAd(Long adId, String clientIp) {
        AdClickHistory adClickHistory = new AdClickHistory();
        adClickHistory.setAdId(adId);
        adClickHistory.setClientIp(clientIp);
        adClickHistory.setCreatedDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!principal.equals("anonymousUser")) {
            UserDetails userDetails = (UserDetails) principal;
            adClickHistory.setUsername(userDetails.getUsername());
        }
        adClickHistoryRepository.save(adClickHistory);
    }

    public List<AdHistoryResult> getAdViewHistoryGroupedByAdId() {
        List<AdHistoryResult> usernameResult = this.getAdViewHistoryGroupedByAdIdAndUsername();
        List<AdHistoryResult> clientIpResult = this.getAdViewHistoryGroupedByAdIdAndClientIp();

        HashMap<Long, Long> totalResult = new HashMap<>();
        for (AdHistoryResult item : usernameResult) {
            totalResult.put(item.getAdId(), item.getCount());
        }
        for (AdHistoryResult item : clientIpResult) {
            totalResult.merge(item.getAdId(), item.getCount(), Long::sum);
        }

        List<AdHistoryResult> resultList = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : totalResult.entrySet()) {
            AdHistoryResult result = new AdHistoryResult();
            result.setAdId(entry.getKey());
            result.setCount(entry.getValue());
            resultList.add(result);
        }
        return resultList;
    }

    private List<AdHistoryResult> getAdViewHistoryGroupedByAdIdAndUsername() {
        // 어제의 시작과 끝 시간 계산
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // Match 단계: 어제 날짜에 해당하고, username 이 있는 문서 필터링
        MatchOperation matchStage = Aggregation.match(
                Criteria.where("createdDate").gte(startOfDay).lt(endOfDay)
                        .and("username").exists(true)
        );

        // Group 단계: adId로 그룹화하고 고유한 username 집합을 생성 - 동일한 username(Id)에 대해서는 하나만 집계가 된다.
        GroupOperation groupStage = Aggregation.group("adId")
                .addToSet("username").as("uniqueUsernames");

        /**
         * Projection 단계: 고유한 username 집합의 크기를 count 로 계산
         * _id -> adId 로 표기를 변경
         */
        ProjectionOperation projectStage = Aggregation.project()
                .andExpression("_id").as("adId")
                .andExpression("size(uniqueUsernames)").as("count");

        // Aggregation 수행
        Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage, projectStage);
        AggregationResults<AdHistoryResult> results = mongoTemplate.aggregate(aggregation, "adViewHistory", AdHistoryResult.class);

        return results.getMappedResults();
    }

    private List<AdHistoryResult> getAdViewHistoryGroupedByAdIdAndClientIp() {
        // 어제의 시작과 끝 시간 계산
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // Match 단계: 어제 날짜에 해당하고, username 이 있는 문서 필터링
        MatchOperation matchStage = Aggregation.match(
                Criteria.where("createdDate").gte(startOfDay).lt(endOfDay)
                        .and("username").exists(false)
        );


        // Group 단계: adId로 그룹화하고 고유한 username 집합을 생성 - 동일 clientIp 에 대해서는 하나의 데이터만 조회된다.
        GroupOperation groupStage = Aggregation.group("adId")
                .addToSet("clientIp").as("uniqueClientIp");

        // Projection 단계: 고유한 username 집합의 크기를 count 로 계산
        ProjectionOperation projectStage = Aggregation.project()
                .andExpression("_id").as("adId")
                .andExpression("size(uniqueClientIp)").as("count");

        // Aggregation 수행
        Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage, projectStage);
        AggregationResults<AdHistoryResult> results = mongoTemplate.aggregate(aggregation, "adViewHistory", AdHistoryResult.class);

        return results.getMappedResults();
    }

    public void insertAdViewStat(List<AdHistoryResult> result) {
        ArrayList<AdViewStat> arrayList = new ArrayList<>();
        for (AdHistoryResult item : result) {
            AdViewStat adViewStat = new AdViewStat();
            adViewStat.setAdId(item.getAdId());
            adViewStat.setCount(item.getCount());

            // yyyy-MM-dd 형식의 DateTimeFormatter 생성
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // LocalDateTime 을 yyyy-MM-dd 형식의 문자열로 변환
            String formattedDate = LocalDateTime.now().minusDays(1).format(formatter);

            adViewStat.setDt(formattedDate);
            arrayList.add(adViewStat);
        }

        adViewStatRepository.saveAll(arrayList);
    }

    public List<AdHistoryResult> getAdClickHistoryGroupedByAdId() {
        List<AdHistoryResult> usernameResult = this.getAdClickHistoryGroupedByAdIdAndUsername();
        List<AdHistoryResult> clientIpResult = this.getAdClickHistoryGroupedByAdIdAndClientip();
        HashMap<Long, Long> totalResult = new HashMap<>();
        for (AdHistoryResult item : usernameResult) {
            totalResult.put(item.getAdId(), item.getCount());
        }
        for (AdHistoryResult item : clientIpResult) {
            totalResult.merge(item.getAdId(), item.getCount(), Long::sum);
        }

        List<AdHistoryResult> resultList = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : totalResult.entrySet()) {
            AdHistoryResult result = new AdHistoryResult();
            result.setAdId(entry.getKey());
            result.setCount(entry.getValue());
            resultList.add(result);
        }
        return resultList;
    }

    private List<AdHistoryResult> getAdClickHistoryGroupedByAdIdAndUsername() {
        // 어제의 시작과 끝 시간 계산
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // Match 단계: 어제 날짜에 해당하고, username 이 있는 문서 필터링
        MatchOperation matchStage = Aggregation.match(
                Criteria.where("createdDate").gte(startOfDay).lt(endOfDay)
                        .and("username").exists(true)
        );

        // Group 단계: adId로 그룹화하고 고유한 username 집합을 생성
        GroupOperation groupStage = Aggregation.group("adId")
                .addToSet("username").as("uniqueUsernames");

        // Projection 단계: 고유한 username 집합의 크기를 count 로 계산
        ProjectionOperation projectStage = Aggregation.project()
                .andExpression("_id").as("adId")
                .andExpression("size(uniqueUsernames)").as("count");

        // Aggregation 수행
        Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage, projectStage);
        AggregationResults<AdHistoryResult> results = mongoTemplate.aggregate(aggregation, "adClickHistory", AdHistoryResult.class);

        return results.getMappedResults();
    }

    private List<AdHistoryResult> getAdClickHistoryGroupedByAdIdAndClientip() {
        // 어제의 시작과 끝 시간 계산
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // Match 단계: 어제 날짜에 해당하고, username 이 있는 문서 필터링
        MatchOperation matchStage = Aggregation.match(
                Criteria.where("createdDate").gte(startOfDay).lt(endOfDay)
                        .and("username").exists(false)
        );

        // Group 단계: adId로 그룹화하고 고유한 username 집합을 생성
        GroupOperation groupStage = Aggregation.group("adId")
                .addToSet("clientIp").as("uniqueClientIp");

        // Projection 단계: 고유한 username 집합의 크기를 count 로 계산
        ProjectionOperation projectStage = Aggregation.project()
                .andExpression("_id").as("adId")
                .andExpression("size(uniqueClientIp)").as("count");

        // Aggregation 수행
        Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage, projectStage);
        AggregationResults<AdHistoryResult> results = mongoTemplate.aggregate(aggregation, "adClickHistory", AdHistoryResult.class);

        return results.getMappedResults();
    }

    public void insertAdClickStat(List<AdHistoryResult> result) {
        ArrayList<AdClickStat> arrayList = new ArrayList<>();
        for (AdHistoryResult item : result) {
            AdClickStat adClickStat = new AdClickStat();
            adClickStat.setAdId(item.getAdId());
            adClickStat.setCount(item.getCount());
            // yyyy-MM-dd 형식의 DateTimeFormatter 생성
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // LocalDateTime을 yyyy-MM-dd 형식의 문자열로 변환
            String formattedDate = LocalDateTime.now().minusDays(1).format(formatter);
            adClickStat.setDt(formattedDate);
            arrayList.add(adClickStat);
        }

        adClickStatRepository.saveAll(arrayList);
    }
}
