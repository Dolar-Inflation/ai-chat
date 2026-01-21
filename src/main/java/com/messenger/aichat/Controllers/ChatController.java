package com.messenger.aichat.Controllers;

import com.messenger.aichat.Service.HuggingFaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PostMapping(
            value = "/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.IMAGE_PNG_VALUE
    )
    public byte[] getImage(@RequestParam("prompt") String message) {
        return huggingFaceService.sendImagePrompt(message);
    }
}
