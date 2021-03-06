package com.hao.activiti_demo.workflow.dto;

import lombok.Data;
import org.springframework.lang.NonNull;

@Data
public class OpenRequest {

    @NonNull
    private String processDefinitionKey;
    @NonNull
    private String businessKey;

    // the user who opens the process
    @NonNull
    private String userId;
    private String variables;
}
