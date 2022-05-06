package com.hao.activiti_demo.workflow.dto;

import lombok.Data;

@Data
public class OpenRequest {

    private String processDefinitionKey;
    private String businessKey;

    // the user who opens the process
    private String userId;
    private String variables;
}
