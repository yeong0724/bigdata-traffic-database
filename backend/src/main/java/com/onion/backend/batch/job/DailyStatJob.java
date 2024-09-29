package com.onion.backend.batch.job;

import com.onion.backend.dto.advertisement.AdHistoryResult;
import com.onion.backend.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DailyStatJob {
    private final AdvertisementService advertisementService;

    @Autowired
    public DailyStatJob(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @Scheduled(cron = "00 34 14 * * ?")
    public void insertAdViewStatAtMidnight() {
        List<AdHistoryResult> viewResult = advertisementService.getAdViewHistoryGroupedByAdId();
        advertisementService.insertAdViewStat(viewResult);

        List<AdHistoryResult> clickResult = advertisementService.getAdClickHistoryGroupedByAdId();
        advertisementService.insertAdClickStat(clickResult);
    }
}
