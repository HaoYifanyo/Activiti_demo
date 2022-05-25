package com.hao.activiti_demo.workflow.multiThread;

import com.alibaba.fastjson.JSONObject;
import com.hao.activiti_demo.workflow.dto.AuditRequest;
import com.hao.activiti_demo.workflow.dto.TaskDTO;
import com.hao.activiti_demo.workflow.service.IProcessService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.hao.activiti_demo.workflow.constant.WorkflowConstant.*;

public class AuditTask implements Callable<Map> {

    private IProcessService processService;
    private AuditRequest request;

    public AuditTask(IProcessService processService, AuditRequest request) {
        this.processService = processService;
        this.request = request;
    }

    @Override
    public Map call() {
        TaskDTO taskDTO;
        Map<String, Object> resultMap = new HashMap<>();
        try{
            Map<String, Object> variableMap = JSONObject.parseObject(request.getVariables());
            Map<String, Object> transientVariableMap = JSONObject.parseObject(request.getTransientVariables());
            taskDTO = processService.audit(request.getBusinessKey(), request.getUserId(), variableMap, transientVariableMap);
            resultMap.put(FUTURE_SUCCESS, taskDTO);
        } catch (Exception e){
            resultMap.put(FUTURE_ERROR, request.getBusinessKey());
        }
        return resultMap;
    }
}
