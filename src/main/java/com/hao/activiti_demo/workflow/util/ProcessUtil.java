package com.hao.activiti_demo.workflow.util;

import com.hao.activiti_demo.workflow.dto.TaskDTO;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.TaskInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ProcessUtil {

    public static TaskDTO buildTaskDTO(TaskInfo taskInfo){
        if (null != taskInfo){
            TaskDTO taskDTO = new TaskDTO();

            taskDTO.setId(taskInfo.getId());
            taskDTO.setName(taskInfo.getName());
            taskDTO.setAssignee(taskInfo.getAssignee());
            taskDTO.setProcessInstanceId(taskInfo.getProcessInstanceId());
            taskDTO.setProcessDefinitionId(taskInfo.getProcessDefinitionId());
            taskDTO.setTaskKey(taskInfo.getTaskDefinitionKey());
            taskDTO.setVariables(taskInfo.getProcessVariables());
            taskDTO.setTaskVariables(taskInfo.getTaskLocalVariables());

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (null != taskInfo.getCreateTime()) {
                taskDTO.setCreateTime(formatter.format(taskInfo.getCreateTime()));
            }
            if (null != taskInfo.getClaimTime()) {
                taskDTO.setClaimTime(formatter.format(taskInfo.getClaimTime()));
            }

            if (taskInfo instanceof HistoricTaskInstance){
                if (null != ((HistoricTaskInstance) taskInfo).getEndTime()) {
                    taskDTO.setCompleteTime(formatter.format(((HistoricTaskInstance) taskInfo).getEndTime()));
                }
            }

            return taskDTO;
        }
        return null;
    }
}
