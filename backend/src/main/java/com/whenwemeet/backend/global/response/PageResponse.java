package com.whenwemeet.backend.global.response;

import com.whenwemeet.backend.global.entity.Pagination;
import lombok.AllArgsConstructor;
import lombok.Getter;


public record PageResponse<T>(
        T data,
        Pagination pagination
) {}
