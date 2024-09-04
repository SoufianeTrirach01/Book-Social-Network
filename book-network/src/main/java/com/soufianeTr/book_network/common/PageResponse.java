package com.soufianeTr.book_network.common;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@Builder
@Setter
@NoArgsConstructor
@Getter
public class PageResponse<T> {
    private List<T> content;
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}
