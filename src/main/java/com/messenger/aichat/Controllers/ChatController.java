package com.messenger.aichat.Controllers;

import com.messenger.aichat.Service.HuggingFaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(produces = "model/gltf-binary",value = "/3D")
    public ResponseEntity<byte[]> send3DPrompt(@RequestParam("prompt") String message) {

        try { byte[] glbBytes = huggingFaceService.send3DtextPrompt(message);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("model/gltf-binary"));
            headers.setContentDisposition( ContentDisposition.attachment() .filename("model.glb") .build() );
            return new ResponseEntity<>(glbBytes, headers, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(null);
        }


    }

}
