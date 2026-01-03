package com.swipelab.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CreateRecipientGroupRequest {
    private String name;
    private List<String> usernames;
}
