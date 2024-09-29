package com.onion.backend.controller;

import com.onion.backend.dto.advertisement.AdHistoryResult;
import com.onion.backend.dto.advertisement.AdvertisementDto;
import com.onion.backend.entity.Advertisement;
import com.onion.backend.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "AdvertisementController", description = "광고 API")
public class AdvertisementController {
    private final AdvertisementService advertisementService;

    @Autowired
    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @Operation(summary = "광고 등록")
    @PostMapping("/admin/ads")
    public ResponseEntity<Advertisement> writeAd(@RequestBody AdvertisementDto advertisementDto) {
        Advertisement advertisement = advertisementService.writeAd(advertisementDto);
        return ResponseEntity.ok(advertisement);
    }

    @Operation(summary = "광고 목록 조회")
    @GetMapping("/ads")
    public ResponseEntity<List<Advertisement>> getAdList() {
        List<Advertisement> advertisementList = advertisementService.getAdList();
        return ResponseEntity.ok(advertisementList);
    }

    @Operation(summary = "광고 조회")
    @GetMapping("/ads/{adId}")
    public ResponseEntity<Advertisement> getAdList(
            @PathVariable Long adId,
            HttpServletRequest request,
            @RequestParam(required = false) Boolean isTrueView
    ) {
        String ipAddress = request.getRemoteAddr();

        Advertisement advertisement = advertisementService.getAd(adId, ipAddress, isTrueView != null && isTrueView);

        return ResponseEntity.ok(advertisement);
    }

    @Operation(summary = "광고 클릭")
    @PostMapping("/ads/{adId}")
    public Object clickAd(@PathVariable Long adId, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        advertisementService.clickAd(adId, ipAddress);
        return ResponseEntity.ok("click");
    }

    @Operation(summary = "광고 조회 통계 조회")
    @GetMapping("/ads/history")
    public ResponseEntity<List<AdHistoryResult>> getAdHistory() {
        List<AdHistoryResult> result = advertisementService.getAdViewHistoryGroupedByAdId();
        advertisementService.insertAdViewStat(result);

        List<AdHistoryResult> clickResult = advertisementService.getAdClickHistoryGroupedByAdId();
        advertisementService.insertAdClickStat(clickResult);

        return ResponseEntity.ok(clickResult);
    }
}
