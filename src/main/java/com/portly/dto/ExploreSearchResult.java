package com.portly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExploreSearchResult {
    private List<ExplorePortfolio> portfolios;
    private long total;
    private int page;
    private int totalPages;
}
