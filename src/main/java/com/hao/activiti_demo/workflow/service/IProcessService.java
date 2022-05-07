package com.hao.activiti_demo.workflow.service;

import com.hao.activiti_demo.workflow.dto.ProcessDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public interface IProcessService {

    ProcessDTO openProcess(String processDefinitionKey, String businessKey, String userId, Map<String, Object> variableMap);
}
