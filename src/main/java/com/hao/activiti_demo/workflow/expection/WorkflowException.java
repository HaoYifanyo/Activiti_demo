package com.hao.activiti_demo.workflow.expection;

public class WorkflowException extends RuntimeException {

    private WorkflowExCode exCode;

    public WorkflowException(WorkflowExCode exCode) {
        this.exCode = exCode;
    }
}
