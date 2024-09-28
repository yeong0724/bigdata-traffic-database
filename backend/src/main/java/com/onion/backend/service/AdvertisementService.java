package com.onion.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onion.backend.dto.advertisement.AdvertisementDto;
import com.onion.backend.entity.Advertisement;
import com.onion.backend.repository.AdvertisementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdvertisementService {
    private static final String REDIS_KEY = "ad:";

    private final AdvertisementRepository advertisementRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    @Autowired
    public AdvertisementService(
            AdvertisementRepository advertisementRepository,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.advertisementRepository = advertisementRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Advertisement writeAd(AdvertisementDto advertisementDto) {
        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(advertisementDto.getTitle());
        advertisement.setContent(advertisementDto.getContent());
        advertisement.setIsDeleted(advertisementDto.getIsDeleted());
        advertisement.setIsVisible(advertisementDto.getIsVisible());
        advertisement.setStartDate(advertisementDto.getStartDate());
        advertisement.setEndDate(advertisementDto.getEndDate());
        advertisement.setViewCount(advertisementDto.getViewCount());
        advertisement.setClickCount(advertisementDto.getClickCount());

        advertisementRepository.save(advertisement);
        redisTemplate.opsForValue().set(REDIS_KEY + advertisement.getId(), advertisement);
        return advertisement;
    }

    public List<Advertisement> getAdList() {
        return advertisementRepository.findAll();
    }

    public Advertisement getAd(Long adId) {
        Object redisCache = redisTemplate.opsForValue().get(REDIS_KEY + adId.toString());
        if (redisCache != null) {
            return objectMapper.convertValue(redisCache, Advertisement.class);
        }

        return advertisementRepository.findById(adId).orElse(null);
    }
}
