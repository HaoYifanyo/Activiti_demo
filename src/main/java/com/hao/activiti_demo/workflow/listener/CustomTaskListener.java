package com.hao.activiti_demo.workflow.listener;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
            log.error("");
        }

        List<String> userIds = getCandidateUsers(candidateGroups);
        if(userIds.isEmpty()){
            log.error("");
        }
        delegateTask.addCandidateUsers(userIds);

    }

    private List<String> getCandidateUsers(List<String> candidateGroups) {
        return null;
    }
}
