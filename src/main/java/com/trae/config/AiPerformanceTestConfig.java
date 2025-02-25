package com.trae.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai.performance.test")
public class AiPerformanceTestConfig {
    /**
     * 并发用户数
     */
    private int concurrentUsers = 10;

    /**
     * 每个用户执行的请求数
     */
    private int requestsPerUser = 5;

    /**
     * 请求超时时间（毫秒）
     */
    private long requestTimeout = 30000;

    /**
     * 性能测试的提示语
     */
    private String testPrompt = "测试大模型的性能和响应时间";

    /**
     * 是否收集详细的性能指标
     */
    private boolean collectDetailedMetrics = true;

    /**
     * 测试结果输出目录
     */
    private String resultOutputDir = "performance-test-results";
}