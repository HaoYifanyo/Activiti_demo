package com.hao.activiti_demo.workflow.listener;

import com.hao.activiti_demo.workflow.constant.WorkflowConstant;
import com.hao.activiti_demo.workflow.expection.WorkflowException;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.hao.activiti_demo.workflow.expection.WorkflowExCode.NO_CANDIDATE_GROUPS;
import static com.hao.activiti_demo.workflow.expection.WorkflowExCode.NO_CANDIDATE_USERS;

@Component
@Slf4j
public class CustomTaskListener implements TaskListener {

    @Autowired
    TaskService taskService;
    @Autowired
    HistoryService historyService;

    @Override
    public void notify(DelegateTask delegateTask) {
        String eventName = delegateTask.getEventName();
        if(EVENTNAME_CREATE.equals(eventName)){
            create(delegateTask);
        }
    }

    private void create(DelegateTask delegateTask){
        String instanceId = delegateTask.getProcessInstanceId();
        String definitionKey = delegateTask.getTaskDefinitionKey();

        if(StringUtils.isNotBlank(delegateTask.getAssignee())){
            return;
        }

        // If this task has been audited before, We can set the assignee to the user who last audited the task.
        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instanceId)
                .orderByHistoricActivityInstanceStartTime().desc()
                .list();
        for (HistoricActivityInstance hi : historicActivityInstances) {
            String activityId = hi.getActivityId();
            if(activityId.equals(definitionKey)){
                log.info("CustomTaskListener: set the assignee to the user who last audited the task.");
                // todo
//                delegateTask.setAssignee(hi.getAssignee());
                taskService.claim(delegateTask.getId(), hi.getAssignee());
                return;
            }
        }

        Set<IdentityLink> identityLinks = delegateTask.getCandidates();
        List<String> candidateGroups = Optional.ofNullable(identityLinks).orElse(Collections.emptySet())
                .stream().filter(r -> ((IdentityLinkEntity)r).isGroup())
                .map(IdentityLink::getGroupId).collect(Collectors.toList());
        if(candidateGroups.isEmpty()){
            log.error("CustomTaskListener: Can't find candidate groups.");
            throw new WorkflowException(NO_CANDIDATE_GROUPS);
        }

        Map<String, String> candidateMap = parseCandidateGroups(candidateGroups);
        List<String> userIds = getCandidateUsers(candidateGroups);
        if(userIds.isEmpty()){
            log.error("CustomTaskListener: Can't find candidate users.");
            throw new WorkflowException(NO_CANDIDATE_USERS);
        }
        delegateTask.addCandidateUsers(userIds);
        log.info("CustomTaskListener: add candidate users, userIds:{}", userIds);
    }

    private Map<String, String> parseCandidateGroups(List<String> candidateGroups) {
        Map<String, String> candidateMap = new HashMap<>();
        for(String candidate : candidateGroups){
            String[] candidates = candidate.split("=");
            if(candidates.length < 2){
                break;
            }
            String candidateType = candidates[0];
            if(ArrayUtils.contains(WorkflowConstant.SUPPORT_CANDIDATE_TYPE, candidateType)){
                String candidateValue = candidates[1];
                candidateMap.put(candidateType, candidateValue.replaceAll("\\|", ","));
            }
        }
        return candidateMap;
    }

    private List<String> getCandidateUsers(List<String> candidateGroups) {
        // todo: call the function of user module
        return null;
    }
}
