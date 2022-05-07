package com.hao.activiti_demo.workflow.service.impl;

import com.hao.activiti_demo.workflow.dto.ProcessDTO;
import com.hao.activiti_demo.workflow.service.IProcessService;
import com.hao.activiti_demo.workflow.util.ProcessUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProcessServiceImpl implements IProcessService {

    @Autowired
    IdentityService identityService;
    @Autowired
    TaskService taskService;
    @Autowired
    HistoryService historyService;
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    RepositoryService repositoryService;

    @Transactional
    @Override
    public ProcessDTO openProcess(String processDefinitionKey, String businessKey, String userId, Map<String, Object> variableMap) {
        try{
            ProcessDTO processDTO = new ProcessDTO();
            identityService.setAuthenticatedUserId(userId);
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variableMap);

            String instanceId = processInstance.getProcessInstanceId();
            processDTO.setProcessInstanceId(instanceId);
            processDTO.setProcessDefinitionId(processInstance.getProcessDefinitionId());
            processDTO.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
            processDTO.setVariables(processInstance.getProcessVariables());
            processDTO.setBusinessKey(processInstance.getBusinessKey());

            // todoTaskList
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(instanceId).list();
            processDTO.setTaskDTOs(taskList.stream().map(ProcessUtil::buildTaskDTO).collect(Collectors.toList()));
            return processDTO;
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
