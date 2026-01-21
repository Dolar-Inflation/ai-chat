package com.messenger.aichat.Controllers;

import com.messenger.aichat.Service.HuggingFaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    @Autowired
    private final HuggingFaceService huggingFaceService;

    public ChatController(HuggingFaceService huggingFaceService) {
        this.huggingFaceService = huggingFaceService;
    }

    @PostMapping("/chat")
    public String sendMessage(@RequestBody String message) {

        return huggingFaceService.sendPrompt(message);
    }

}
