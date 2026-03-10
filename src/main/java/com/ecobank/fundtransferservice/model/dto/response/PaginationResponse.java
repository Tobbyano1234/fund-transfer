package com.ecobank.fundtransferservice.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse {
    private Integer pageNumber;
    private Integer pageSize;
    private Boolean hasNextPage;
    private Boolean hasPrevPage;
    private Integer totalPage;
    private Long total;
}

