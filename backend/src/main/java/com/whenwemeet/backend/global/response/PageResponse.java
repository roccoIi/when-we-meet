package com.whenwemeet.backend.global.response;

import com.whenwemeet.backend.global.entity.Pagination;


public record PageResponse<T>(
        T data,
        Pagination pagination
) {}
