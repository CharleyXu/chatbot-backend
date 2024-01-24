package com.xu.chatgpt.controller;

import com.xu.chatgpt.client.ChatClient;
import com.xu.chatgpt.entity.chat.ChatCompletionRequest;
import com.xu.chatgpt.entity.chat.ChatCompletionResponse;
import com.xu.chatgpt.sse.SseEventSourceListener;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Created by CharleyXu on 2024/1/23
 */
@RestController
@AllArgsConstructor
public class ChatController {

    private final ChatClient chatClient;

    @PostMapping(value = "/v0/chat/completions")
    public ChatCompletionResponse chatCompletions(@RequestBody ChatCompletionRequest request) {
        return chatClient.chatCompletions(request);
    }

    @PostMapping("/v1/chat/completions")
    public SseEmitter sseChatCompletions(@RequestBody ChatCompletionRequest request) {
        SseEmitter sseEmitter = new SseEmitter();
        chatClient.streamChatCompletions(request, new SseEventSourceListener(sseEmitter));
        return sseEmitter;
    }

}
