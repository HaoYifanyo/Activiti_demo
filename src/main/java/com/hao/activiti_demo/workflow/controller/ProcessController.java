package com.hao.activiti_demo.workflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.hao.activiti_demo.workflow.dto.AuditRequest;
import com.hao.activiti_demo.workflow.dto.OpenRequest;
import com.hao.activiti_demo.workflow.dto.ProcessDTO;
import com.hao.activiti_demo.workflow.dto.TaskDTO;
import com.hao.activiti_demo.workflow.service.IProcessService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

/**
 *
 */

@Slf4j
@RestController
@RequestMapping("/process")
public class ProcessController {

    private Logger logger = LoggerFactory.getLogger(ProcessController.class);

    @Autowired
    IProcessService processService;

    @PostMapping("/open")
    public ProcessDTO openProcess(@Valid @RequestBody OpenRequest request){
        Map<String, Object> variableMap = JSONObject.parseObject(request.getVariables());
        ProcessDTO processDTO = processService.openProcess(request.getProcessDefinitionKey(), request.getBusinessKey(), request.getUserId(), variableMap);

        return processDTO;
    }

    @PostMapping("/audit")
    public TaskDTO audit(@Valid @RequestBody AuditRequest request){
        Map<String, Object> variableMap = JSONObject.parseObject(request.getVariables());
        Map<String, Object> transientVariableMap = JSONObject.parseObject(request.getTransientVariables());

        TaskDTO taskDTO = processService.audit(request.getBusinessKey(), request.getUserId(), variableMap, transientVariableMap);
        return taskDTO;
    }

    @PostMapping("/open_audit")
    public ProcessDTO openAuditProcess(@Valid @RequestBody OpenRequest request){
        Map<String, Object> variableMap = JSONObject.parseObject(request.getVariables());
        ProcessDTO processDTO = processService.openAuditProcess(request.getProcessDefinitionKey(), request.getBusinessKey(), request.getUserId(), variableMap);

        return processDTO;
    }

    @PostMapping("/discard")
    public void discard(@Valid @RequestBody AuditRequest request){
        Map<String, Object> variableMap = JSONObject.parseObject(request.getVariables());
        processService.discard(request.getBusinessKey(), request.getUserId(), variableMap);
    }

    @PostMapping("/back")
    public TaskDTO back(@Valid @RequestBody AuditRequest request){
        Map<String, Object> variableMap = JSONObject.parseObject(request.getVariables());
        TaskDTO taskDTO = processService.back(request.getBusinessKey(), request.getUserId(), variableMap);
        return taskDTO;
    }
}
