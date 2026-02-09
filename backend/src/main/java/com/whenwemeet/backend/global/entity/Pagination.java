package com.whenwemeet.backend.global.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pagination {
    private long currentPage;
    private long totalPages;
    private long totalItems;
    private boolean hasMore;
}
