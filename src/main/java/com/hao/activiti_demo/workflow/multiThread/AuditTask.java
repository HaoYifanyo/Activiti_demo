package com.hao.activiti_demo.workflow.multiThread;

import com.hao.activiti_demo.workflow.dto.AuditRequest;
import com.hao.activiti_demo.workflow.service.IProcessService;

import java.util.concurrent.Callable;

public class AuditTask implements Callable {

    private IProcessService processService;
    private AuditRequest request;

    public AuditTask(IProcessService processService, AuditRequest request) {
        this.processService = processService;
        this.request = request;
    }

    @Override
    public Object call() throws Exception {
        return null;
    }
}
