package com.trae.service;

import com.trae.config.AiPerformanceTestConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiPerformanceTestService {
    private final ChatClient chatClient;
    private final AiPerformanceTestConfig config;

    public void runPerformanceTest() {
        log.info("开始执行性能测试，并发用户数：{}，每用户请求数：{}", config.getConcurrentUsers(), config.getRequestsPerUser());

        ExecutorService executor = Executors.newFixedThreadPool(config.getConcurrentUsers());
        CountDownLatch latch = new CountDownLatch(config.getConcurrentUsers());
        List<Future<List<TestResult>>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // 提交并发任务
        for (int i = 0; i < config.getConcurrentUsers(); i++) {
            futures.add(executor.submit(() -> {
                List<TestResult> results = new ArrayList<>();
                try {
                    for (int j = 0; j < config.getRequestsPerUser(); j++) {
                        TestResult result = executeSingleTest();
                        results.add(result);
                        if (result.isSuccess()) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
                return results;
            }));
        }

        try {
            latch.await();
            List<TestResult> allResults = new ArrayList<>();
            for (Future<List<TestResult>> future : futures) {
                allResults.addAll(future.get());
            }

            long totalTime = System.currentTimeMillis() - startTime;
            generateTestReport(allResults, totalTime, successCount.get(), failureCount.get());

        } catch (Exception e) {
            log.error("性能测试执行异常", e);
        } finally {
            executor.shutdown();
        }
    }

    private TestResult executeSingleTest() {
        TestResult result = new TestResult();
        result.setStartTime(System.currentTimeMillis());

        try {
            Prompt prompt = new Prompt(config.getTestPrompt());
            ChatResponse response = chatClient.call(prompt);
            result.setSuccess(true);
            result.setResponse(response.getResult().getOutput().getContent());
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            log.error("测试请求执行失败", e);
        } finally {
            result.setEndTime(System.currentTimeMillis());
        }

        return result;
    }

    private void generateTestReport(List<TestResult> results, long totalTime, int successCount, int failureCount) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File reportDir = new File(config.getResultOutputDir());
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }

        File reportFile = new File(reportDir, "performance_test_report_" + timestamp + ".txt");

        try (FileWriter writer = new FileWriter(reportFile)) {
            writer.write(String.format("性能测试报告 - %s\n\n", timestamp));
            writer.write(String.format("总请求数：%d\n", results.size()));
            writer.write(String.format("成功请求数：%d\n", successCount));
            writer.write(String.format("失败请求数：%d\n", failureCount));
            writer.write(String.format("总执行时间：%.2f秒\n", totalTime / 1000.0));

            // 计算响应时间统计
            double avgResponseTime = results.stream()
                    .mapToLong(r -> r.getEndTime() - r.getStartTime())
                    .average()
                    .orElse(0.0);

            writer.write(String.format("平均响应时间：%.2f毫秒\n", avgResponseTime));

            if (config.isCollectDetailedMetrics()) {
                writer.write("\n详细请求记录：\n");
                for (int i = 0; i < results.size(); i++) {
                    TestResult result = results.get(i);
                    writer.write(String.format("\n请求 #%d:\n", i + 1));
                    writer.write(String.format("状态：%s\n", result.isSuccess() ? "成功" : "失败"));
                    writer.write(String.format("响应时间：%d毫秒\n", result.getEndTime() - result.getStartTime()));
                    if (!result.isSuccess()) {
                        writer.write(String.format("错误信息：%s\n", result.getErrorMessage()));
                    }
                }
            }

        } catch (IOException e) {
            log.error("生成测试报告失败", e);
        }
    }

    @lombok.Data
    private static class TestResult {
        private long startTime;
        private long endTime;
        private boolean success;
        private String response;
        private String errorMessage;
    }
}