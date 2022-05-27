package com.hao.activiti_demo.workflow.multiThread;

import com.alibaba.fastjson.JSONObject;
import com.hao.activiti_demo.workflow.dto.OpenRequest;
import com.hao.activiti_demo.workflow.dto.ProcessDTO;
import com.hao.activiti_demo.workflow.service.IProcessService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.hao.activiti_demo.workflow.constant.WorkflowConstant.FUTURE_ERROR;
import static com.hao.activiti_demo.workflow.constant.WorkflowConstant.FUTURE_SUCCESS;

public class OpenAuditTask implements Callable<Map> {

    private IProcessService processService;
    private OpenRequest request;

    public OpenAuditTask(IProcessService processService, OpenRequest request) {
        this.processService = processService;
        this.request = request;
    }

    @Override
    public Map call() {
        ProcessDTO processDTO;
        Map<String, Object> resultMap = new HashMap<>();
        try{
            Map<String, Object> variableMap = JSONObject.parseObject(request.getVariables());
            processDTO = processService.openAuditProcess(request.getProcessDefinitionKey(), request.getBusinessKey(), request.getUserId(), variableMap);
            resultMap.put(FUTURE_SUCCESS, processDTO);
        } catch (Exception e){
            resultMap.put(FUTURE_ERROR, request.getBusinessKey());
        }
        return resultMap;
    }
}
