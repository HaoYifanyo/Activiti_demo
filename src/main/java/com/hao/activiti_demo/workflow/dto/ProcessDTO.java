package com.hao.activiti_demo.workflow.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProcessDTO {

    private String processDefinitionKey;
    private String processDefinitionId;
    private String processInstanceId;
    private String businessKey;

    // Tasks that are currently running after the process is opened
    private List<TaskDTO> taskDTOs;
    private Map<String, Object> variables;
}
