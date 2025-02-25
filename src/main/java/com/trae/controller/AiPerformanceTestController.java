package com.trae.controller;

import com.trae.service.AiPerformanceTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/performance")
@RequiredArgsConstructor
public class AiPerformanceTestController {
    private final AiPerformanceTestService performanceTestService;

    @PostMapping("/test")
    public String runPerformanceTest() {
        performanceTestService.runPerformanceTest();
        return "性能测试已启动，请查看日志和测试结果目录获取详细信息";
    }
}