package com.example.campaignserver.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Paginated response wrapper.
 *
 * <pre>
 * {
 *   "content": [...],
 *   "page": 0,
 *   "size": 20,
 *   "totalElements": 100,
 *   "totalPages": 5,
 *   "last": false
 * }
 * </pre>
 */
@Data
@Builder
public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
