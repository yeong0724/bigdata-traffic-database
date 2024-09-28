package com.onion.backend.controller;

import com.onion.backend.dto.advertisement.AdvertisementDto;
import com.onion.backend.entity.Advertisement;
import com.onion.backend.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AdvertisementController {
    private final AdvertisementService advertisementService;

    @Autowired
    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @PostMapping("/admin/ads")
    public ResponseEntity<Advertisement> writeAd(@RequestBody AdvertisementDto advertisementDto) {
        Advertisement advertisement = advertisementService.writeAd(advertisementDto);
        return ResponseEntity.ok(advertisement);
    }

    @GetMapping("/ads")
    public ResponseEntity<List<Advertisement>> getAdList() {
        List<Advertisement> advertisementList = advertisementService.getAdList();
        return ResponseEntity.ok(advertisementList);
    }

    @GetMapping("/ads/{adId}")
    public ResponseEntity<Advertisement> getAdList(@PathVariable Long adId) {
        Advertisement advertisement = advertisementService.getAd(adId);

        return ResponseEntity.ok(advertisement);
    }
}
