package com.trae.controller;

import com.trae.common.Result;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private ChatClient chatClient;

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody String message) {
        try {
            log.info("收到AI对话请求：{}", message);
            ChatResponse response = chatClient.call(new Prompt(message));
            String result = response.getResult().getOutput().getContent();
            log.info("AI回复：{}", result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("AI对话异常：", e);
            return Result.error("AI服务异常：" + e.getMessage());
        }
    }
}