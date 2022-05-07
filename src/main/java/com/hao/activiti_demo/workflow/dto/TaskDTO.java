package com.hao.activiti_demo.workflow.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TaskDTO {

    private String id;
    private String name;
    private String taskKey;
    private String assignee;

    private String processDefinitionId;
    private String processInstanceId;
    private String businessKey;

    private String createTime;
    private String claimTime;
    private String completeTime;

    // in the process instance
    private Map<String, Object> variables;
    // in this task
    private Map<String, Object> taskVariables;

    // whether it is the first node
    private Boolean firstNode;
    // whether it is the final node
    private Boolean finalNode;

    private List<Map<String, Object>> attachments;

}
