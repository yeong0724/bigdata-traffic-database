package com.onion.backend.dto.advertisement;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdvertisementDto {
    private String title;

    private String content;

    private final Boolean isDeleted = false;

    private final Boolean isVisible = true;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private final Integer viewCount = 0;

    private final Integer clickCount = 0;
}