package com.example.demo.controller;

import com.example.demo.Model.StepModelList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.RequestsService;

@RestController
@RequestMapping("/test")
@EnableAutoConfiguration
public class AssureController {

    @Autowired
    private RequestsService requestsService;

    /**
     * 通过json执行rest-assured
     * @param stepModelList
     */
    @PostMapping("/json")
    public void testJson(@RequestBody StepModelList stepModelList) {
        String rootPath = "F:/project/HttpTools-master";
        String FILEPATH = rootPath + "/src/main/java/Case";
        String REPORTPATH = rootPath + "/src/main/java/Report";
        System.setProperty("FILEPATH",FILEPATH);
        System.setProperty("REPORTPATH",REPORTPATH);
        System.out.println("debugRun");
        requestsService.run(stepModelList);
    }


}
