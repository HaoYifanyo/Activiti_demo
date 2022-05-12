package com.hao.activiti_demo.workflow.service;

import com.hao.activiti_demo.workflow.dto.ProcessDTO;
import com.hao.activiti_demo.workflow.dto.TaskDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public interface IProcessService {

    ProcessDTO openProcess(String processDefinitionKey, String businessKey, String userId, Map<String, Object> variableMap);

    TaskDTO audit(String businessKey, String userId, Map<String, Object> variableMap, Map<String, Object> transientVariableMap);

    /**
     * When the first node is the submitter,
     * we generally need to open the process and audit the first node at the same time
     */
    ProcessDTO openAuditProcess(String processDefinitionKey, String businessKey, String userId, Map<String, Object> variableMap);

    /**
     * If there are no special restrictions, the process can be discarded at any node.
     */
    void discard(String businessKey, String userId, Map<String, Object> variableMap);
}
