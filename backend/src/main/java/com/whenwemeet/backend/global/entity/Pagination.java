package com.whenwemeet.backend.global.entity;

public record Pagination (
        Long currentPage,
        Long totalPages,
        Long totalItems,
        Boolean hasMore
) {}
