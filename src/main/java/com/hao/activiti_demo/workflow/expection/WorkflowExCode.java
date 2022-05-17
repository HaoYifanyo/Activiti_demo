package com.hao.activiti_demo.workflow.expection;

public enum WorkflowExCode {

    INSTANCE_NOT_EXIST("001", "Process instance does not exist."),
    TASKS_NOT_FOUND("002", "Can't find eligible to-do tasks."),
    NO_NODES_BACKED("003", "There are no nodes that can be backed."),
    NO_INCOMING_FLOWS("004", "Failed to jump, there are no incoming flows of the target node."),

    OPEN_PROCESS_ERROR("101", "Failed to open process."),
    BACK_PROCESS_ERROR("102", "Failed to back process."),
    ;

    private String code;
    private String message;

    WorkflowExCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
