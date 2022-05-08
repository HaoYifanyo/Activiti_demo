package com.hao.activiti_demo.workflow.dto;

import lombok.Data;
import org.springframework.lang.NonNull;

@Data
public class AuditRequest {

    @NonNull
    private String businessKey;

    // the user who audits the process
    @NonNull
    private String userId;
    private String variables;
    private String transientVariables;

    // reserved, used in the free jump method
    private String jumpTaskId;
}
