package com.hao.activiti_demo.workflow.config;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class activitiConfig implements ProcessEngineConfigurationConfigurer {
    @Override
    public void configure(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
        // "single-resource" means treating each bpmn file as a separate resource.
        // Every time the program is restarted, if a bpmn file was modified, only the corresponding process definition will be updated.
        springProcessEngineConfiguration.setDeploymentMode("single-resource");
    }
}
