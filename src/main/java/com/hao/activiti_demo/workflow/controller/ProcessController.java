package com.hao.activiti_demo.workflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.hao.activiti_demo.workflow.dto.AuditRequest;
import com.hao.activiti_demo.workflow.dto.OpenRequest;
import com.hao.activiti_demo.workflow.dto.ProcessDTO;
import com.hao.activiti_demo.workflow.dto.TaskDTO;
import com.hao.activiti_demo.workflow.multiThread.AuditTask;
import com.hao.activiti_demo.workflow.service.IProcessService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.hao.activiti_demo.workflow.constant.WorkflowConstant.*;

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

    @PostMapping("/audit_batch")
    public Map<String, Object> auditBatch(@Valid @RequestBody List<AuditRequest> requestList){
        List<TaskDTO> successTaskDTOS = new ArrayList<>();
        List<String> errorTaskBizKeys = new ArrayList<>();
        List<Future<Map>> futureList = new ArrayList<>();

        ExecutorService executor = Executors.newCachedThreadPool();

        // Set the requestAttributes to be shared by the child thread
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(requestAttributes, true);
        requestList.forEach(r -> {
            AuditTask task = new AuditTask(processService, r);
            Future<Map> future = executor.submit(task);
            futureList.add(future);
        });
        executor.shutdown();

        try {
            for(Future<Map> future : futureList){
                if(future.get().containsKey(FUTURE_ERROR)){
                    errorTaskBizKeys.add((String) future.get().get(FUTURE_ERROR));
                } else {
                    successTaskDTOS.add((TaskDTO) future.get().get(FUTURE_SUCCESS));
                }
            }
        } catch (Exception e){
            log.error("Failed to audit batch.", e);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", successTaskDTOS);
        resultMap.put("error", errorTaskBizKeys);
        return resultMap;
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
    public List<TaskDTO> back(@Valid @RequestBody AuditRequest request){
        Map<String, Object> variableMap = JSONObject.parseObject(request.getVariables());
        return processService.back(request.getBusinessKey(), request.getUserId(), variableMap);
    }
}
