package com.hao.activiti_demo.workflow.controller;

import com.hao.activiti_demo.workflow.dto.OpenRequest;
import com.sun.xml.internal.ws.client.ResponseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 *
 */

@Slf4j
@RestController
@RequestMapping("/process")
public class ProcessController {

    @PostMapping("/open")
    public Map<String, Object> openProcess(@RequestBody OpenRequest request){

        return null;
    }
}
