package com.hao.activiti_demo.workflow.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hao.activiti_demo.workflow.constant.WorkflowConstant;
import com.hao.activiti_demo.workflow.dto.ProcessDTO;
import com.hao.activiti_demo.workflow.dto.TaskDTO;
import com.hao.activiti_demo.workflow.service.IProcessService;
import com.hao.activiti_demo.workflow.util.ProcessUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    @Transactional
    @Override
    public TaskDTO audit(String businessKey, String userId, Map<String, Object> variableMap, Map<String, Object> transientVariableMap) {
        List<Task> todoTaskList = taskService.createTaskQuery()
                .processInstanceBusinessKey(businessKey)
                .taskCandidateOrAssigned(userId)
                .orderByTaskCreateTime().desc()
                .active()
                .list();
        if(!todoTaskList.isEmpty()){
            Task task = todoTaskList.get(0);
            String taskId = task.getId();
            String instanceId = task.getProcessInstanceId();
            taskService.claim(taskId, userId);

            addAttachments(variableMap, taskId, instanceId);

            taskService.complete(taskId, variableMap, transientVariableMap);

            TaskDTO taskDTO = getHisTask(taskId);
            taskDTO.setFirstNode(isFirstNode(taskDTO.getTaskKey(), taskDTO.getProcessDefinitionId()));
            taskDTO.setFinalNode(isFinalNode(instanceId));
            return taskDTO;
        }

        return null;
    }

    @Override
    public ProcessDTO openAuditProcess(String processDefinitionKey, String businessKey, String userId, Map<String, Object> variableMap) {
        ProcessDTO processDTO = openProcess(processDefinitionKey, businessKey, userId, variableMap);

        Map<String, Object> transientVariableMap = new HashMap<>();
        transientVariableMap.put(WorkflowConstant.DISCARD, false);
        audit(processDTO.getBusinessKey(), userId, variableMap, transientVariableMap);

        // todoTaskList
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processDTO.getProcessInstanceId()).list();
        processDTO.setTaskDTOs(taskList.stream().map(ProcessUtil::buildTaskDTO).collect(Collectors.toList()));
        return processDTO;
    }


    private void addAttachments(Map<String, Object> variableMap, String taskId, String processInstanceId) {
        if(null == variableMap || variableMap.isEmpty()){
            return;
        }

        JSONArray fileList = variableMap.containsKey(WorkflowConstant.FILE_LIST) ? (JSONArray) variableMap.get(WorkflowConstant.FILE_LIST) : null;
        if(null != fileList && !fileList.isEmpty()){
            for (int i = 0; i < fileList.size(); i++) {
                JSONObject jsonObject = fileList.getJSONObject(i);
                Attachment attachment = taskService.createAttachment(null, taskId, processInstanceId, fileName(jsonObject),null, jsonObject.getString(WorkflowConstant.FILE_URL));
                taskService.saveAttachment(attachment);
            }
        }
    }

    private String fileName(JSONObject jsonObject) {
        return new StringJoiner(",")
                .add(WorkflowConstant.FILE_ID + "=" + jsonObject.getString(WorkflowConstant.FILE_ID))
                .add(WorkflowConstant.FILE_NAME + "=" + jsonObject.getString(WorkflowConstant.FILE_NAME))
                .toString();
    }

    private TaskDTO getHisTask(String taskId) {
        if(StringUtils.isNotBlank(taskId)){
            HistoricTaskInstance taskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
            if(null != taskInstance){
                return ProcessUtil.buildTaskDTO(taskInstance);
            }
        }
        return null;
    }

    private Boolean isFirstNode(String taskKey, String processDefinitionId) {
        Collection<FlowElement> flowElements = repositoryService.getBpmnModel(processDefinitionId).getProcesses().get(0).getFlowElements();
        if(!flowElements.isEmpty()){
            // Get sequenceFlows whose source is StartEvent
            List<SequenceFlow> sequenceFlows = flowElements.stream()
                    .filter(r -> r instanceof SequenceFlow)
                    .map(r -> (SequenceFlow)r)
                    .filter(r -> r.getSourceFlowElement() instanceof StartEvent)
                    .collect(Collectors.toList());

            if(!sequenceFlows.isEmpty()){
                String firstNodeKey = sequenceFlows.get(0).getTargetFlowElement().getId();
                return firstNodeKey.equals(taskKey);
            }
        }
        return false;
    }

    private Boolean isFinalNode(String instanceId) {
        return historyService.createHistoricActivityInstanceQuery().processInstanceId(instanceId).finished().count() > 0;
    }

}
