package com.ndash.identity_framework.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A clean wrapper for paginated data to send to clients.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;

    /**
     * Helper to convert Spring Page<T> to PaginatedResponse<T>
     */
//    public static <T> PaginatedResponse<T> fromPage(Page<T> page) {
//        return new PaginatedResponse<>(
//                page.getContent(),
//                page.getNumber(),
//                page.getSize(),
//                page.getTotalElements(),
//                page.getTotalPages(),
//                page.isLast(),
//                page.isFirst()
//        );
//    }
}