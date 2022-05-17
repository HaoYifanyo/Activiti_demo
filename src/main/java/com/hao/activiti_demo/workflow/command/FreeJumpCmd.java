package com.hao.activiti_demo.workflow.command;

import com.hao.activiti_demo.workflow.expection.WorkflowException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.impl.cmd.NeedsActiveTaskCmd;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

import java.util.List;
import java.util.Map;

import static com.hao.activiti_demo.workflow.expection.WorkflowExCode.NO_INCOMING_FLOWS;

public class FreeJumpCmd extends NeedsActiveTaskCmd<Void> {

    private String taskId;
    private String jumpTaskKey;
    private Map<String, Object> variableMap;
    private String userId;

    public FreeJumpCmd(String taskId, String jumpTaskKey, Map<String, Object> variableMap, String userId) {
        super(taskId);
        this.taskId = taskId;
        this.jumpTaskKey = jumpTaskKey;
        this.variableMap = variableMap;
        this.userId = userId;
    }

    @Override
    protected Void execute(CommandContext commandContext, TaskEntity taskEntity) {
        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        taskEntityManager.changeTaskAssignee(taskEntity, userId);
        taskEntityManager.deleteTask(taskEntity, "Free Jump", false, false);

        BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(taskEntity.getProcessDefinitionId());
        FlowNode targetFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(jumpTaskKey);
        List<SequenceFlow> incomingFlows = targetFlowNode.getIncomingFlows();
        if(incomingFlows.isEmpty()){
            throw new WorkflowException(NO_INCOMING_FLOWS);
        }

        ExecutionEntity executionEntity = taskEntity.getExecution();
//        executionEntity = commandContext.getExecutionEntityManager().findById(executionEntity.getId());
        executionEntity.setCurrentFlowElement(incomingFlows.get(0));

        commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(executionEntity, true);
        return null;
    }
}
