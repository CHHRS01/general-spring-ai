package com.trae.controller;

import com.trae.common.Result;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/image")
public class ImageAnalysisController {

    @Autowired
    private ChatClient chatClient;

    @PostMapping("/analyze")
    public Result<String> analyzeImageAndText(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "text", required = false) String text) {
        try {
            log.info("收到图片分析请求，文本内容：{}", text);

            // 将图片转换为Base64
            String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
            log.debug("图片转换为Base64完成，长度：{}", base64Image.length());

            // 构建多模态分析提示
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("请分析这张图片");
            if (text != null && !text.trim().isEmpty()) {
                promptBuilder.append("，并结合以下文本进行解释：\n").append(text);
            }

            // 构建包含图片的请求
            List<Message> messages = new ArrayList<>();
            List<Media> media = new ArrayList<>();
            media.add(new Media(MimeType.valueOf(image.getContentType()), base64Image));
            messages.add(new UserMessage(promptBuilder.toString(), media));

            // 调用Spring AI进行分析
            ChatResponse response = chatClient.call(new Prompt(messages));
            String result = response.getResult().getOutput().getContent();
            
            log.info("图片分析完成");
            return Result.success(result);

        } catch (Exception e) {
            log.error("图片分析过程中发生异常：", e);
            return Result.error("图片分析服务异常：" + e.getMessage());
        }
    }
}