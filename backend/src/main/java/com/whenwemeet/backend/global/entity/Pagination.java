package com.whenwemeet.backend.global.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

public record Pagination (
        Long currentPage,
        Long totalPages,
        Long totalItems,
        Boolean hasMore
) {}
