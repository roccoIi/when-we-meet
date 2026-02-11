package com.whenwemeet.backend.global.response;

import com.whenwemeet.backend.global.entity.Pagination;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageResponse<T> {
    private T data;
    private Pagination pagination;
}
