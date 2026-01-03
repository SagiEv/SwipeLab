package com.swipelab.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class RecipientGroupListResponse {
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;
    private Integer totalGroups;
    private List<RecipientGroupResponse> recipientGroups;
}