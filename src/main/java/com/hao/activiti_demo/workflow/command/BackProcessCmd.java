package com.hao.activiti_demo.workflow.command;

import org.activiti.engine.impl.cmd.NeedsActiveTaskCmd;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

import java.util.Map;

public class BackProcessCmd extends NeedsActiveTaskCmd<Void> {

    private String taskId;
    private String jumpTaskKey;
    private Map<String, Object> variableMap;
    private String userId;

    public BackProcessCmd(String taskId, String jumpTaskKey, Map<String, Object> variableMap, String userId) {
        super(taskId);
        this.taskId = taskId;
        this.jumpTaskKey = jumpTaskKey;
        this.variableMap = variableMap;
        this.userId = userId;
    }

    @Override
    protected Void execute(CommandContext commandContext, TaskEntity taskEntity) {
        return null;
    }
}
