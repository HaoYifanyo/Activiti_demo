package com.hao.activiti_demo.workflow.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hao.activiti_demo.workflow.constant.WorkflowConstant;
import com.hao.activiti_demo.workflow.dto.ProcessDTO;
import com.hao.activiti_demo.workflow.dto.TaskDTO;
import com.hao.activiti_demo.workflow.expection.WorkflowException;
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

import static com.hao.activiti_demo.workflow.expection.WorkflowExCode.*;

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
            log.info("Open process——start, processDefinitionKey:{}, businessKey:{}, userId:{}, variableMap:{}",
                    processDefinitionKey, businessKey, userId, variableMap);
            ProcessDTO processDTO = new ProcessDTO();
            identityService.setAuthenticatedUserId(userId);
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variableMap);

            String instanceId = processInstance.getProcessInstanceId();
            processDTO.setProcessInstanceId(instanceId);
            processDTO.setProcessDefinitionId(processInstance.getProcessDefinitionId());
            processDTO.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
            processDTO.setVariables(processInstance.getProcessVariables());
            processDTO.setBusinessKey(processInstance.getBusinessKey());

            List<Task> taskList = getTodoTaskList(instanceId);
            processDTO.setTaskDTOs(taskList.stream().map(ProcessUtil::buildTaskDTO).collect(Collectors.toList()));
            log.info("Open process——end, return processDTO:{}", processDTO);
            return processDTO;
        } catch (RuntimeException e) {
            log.error("Failed to open process.", e);
            throw new WorkflowException(OPEN_PROCESS_ERROR);
        }
    }

    @Transactional
    @Override
    public TaskDTO audit(String businessKey, String userId, Map<String, Object> variableMap, Map<String, Object> transientVariableMap) {
        List<Task> todoTaskList = getTodoTaskList(businessKey, userId);
        if(todoTaskList.isEmpty()){
            log.error("Failed to audit, can't find eligible to-do tasks.");
            throw new WorkflowException(TASKS_NOT_FOUND);
        }

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

    private List<Task> getTodoTaskList(String instanceId) {
        return taskService.createTaskQuery().processInstanceId(instanceId).list();
    }

    private List<Task> getTodoTaskList(String businessKey, String userId) {
        return taskService.createTaskQuery()
                    .processInstanceBusinessKey(businessKey)
                    .taskCandidateOrAssigned(userId)
                    .orderByTaskCreateTime().desc()
                    .active()
                    .list();
    }

    @Transactional
    @Override
    public ProcessDTO openAuditProcess(String processDefinitionKey, String businessKey, String userId, Map<String, Object> variableMap) {
        ProcessDTO processDTO = openProcess(processDefinitionKey, businessKey, userId, variableMap);

        Map<String, Object> transientVariableMap = new HashMap<>();
        transientVariableMap.put(WorkflowConstant.DISCARD, false);
        audit(processDTO.getBusinessKey(), userId, variableMap, transientVariableMap);

        List<Task> taskList = getTodoTaskList(processDTO.getProcessInstanceId());
        processDTO.setTaskDTOs(taskList.stream().map(ProcessUtil::buildTaskDTO).collect(Collectors.toList()));
        return processDTO;
    }

    @Transactional
    @Override
    public void discard(String businessKey, String userId, Map<String, Object> variableMap) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
        if(null == processInstance){
            log.error("Failed to discard, process instance does not exist.");
            throw new WorkflowException(INSTANCE_NOT_EXIST);
        }

        List<Task> todoTaskList = getTodoTaskList(businessKey, userId);
        if(todoTaskList.isEmpty()){
            log.error("Failed to discard, can't find eligible to-do tasks.");
            throw new WorkflowException(TASKS_NOT_FOUND);
        }
        Task task = todoTaskList.get(0);
        String taskId = task.getId();

        addAttachments(variableMap, task.getId(), task.getProcessInstanceId());
        taskService.setVariablesLocal(taskId, variableMap);
        runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "discard, userId:" + userId);
    }

    @Transactional
    @Override
    public TaskDTO back(String businessKey, String userId, Map<String, Object> variableMap) {
        log.info("Back to previous node——start, businessKey:{}, userId:{}, variableMap:{}", businessKey, userId, variableMap);
        List<Task> todoTaskList = getTodoTaskList(businessKey, userId);
        if(todoTaskList.isEmpty()){
            log.error("Failed to back, can't find eligible to-do tasks.");
            throw new WorkflowException(TASKS_NOT_FOUND);
        }

        Task task = todoTaskList.get(0);
        List<TaskDTO> doneTaskList = getDoneTaskList(task.getProcessInstanceId());
        if(doneTaskList.isEmpty()){
            log.error("Failed to back, there are no nodes that can be backed.");
            throw new WorkflowException(NO_NODES_BACKED);
        }

        String jumpTaskKey = doneTaskList.get(0).getTaskKey();
        // todo
        return null;
    }

    private List<TaskDTO> getDoneTaskList(String processInstanceId) {
        if(StringUtils.isNotBlank(processInstanceId)){
            List<HistoricTaskInstance> taskInstanceList = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .orderByHistoricTaskInstanceStartTime()
                    .desc()
                    .list();
            if(null != taskInstanceList){
                List<TaskDTO> taskDTOList = taskInstanceList.stream().map(ProcessUtil::buildTaskDTO).collect(Collectors.toList());
                return taskDTOList;
            }
        }
        return null;
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
