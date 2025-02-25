package com.trae.controller;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/stream")
public class StreamController {

    @Autowired
    private OllamaChatClient chatClient;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody String message) {
        Prompt prompt = new Prompt(message);
        return ((StreamingChatClient) chatClient)
                .stream(prompt)
                .map(ChatResponse::getResult)
                .map(result -> result.getOutput().getContent());
    }
}