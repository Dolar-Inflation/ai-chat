package com.messenger.aichat.Service;

import com.messenger.aichat.Configuration.HuggingFaceConfig;
import org.springframework.beans.factory.annotation.ParameterResolutionDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;



import java.util.List;
import java.util.Map;

@Service
public class HuggingFaceService {

    private final HuggingFaceConfig huggingFaceConfig;
    private final String apiKey;
    private final RestTemplate restTemplate;
    private final String imageApiKey;

    public HuggingFaceService(HuggingFaceConfig huggingFaceConfig, @Value("${huggingface.api.key}") String apiKey,@Value("${stabilityai.api.key}") String imageApiKey, RestTemplate restTemplate) {
        this.huggingFaceConfig = huggingFaceConfig;
        this.apiKey=apiKey;
        this.restTemplate = restTemplate;
        this.imageApiKey = imageApiKey;
    }


    public String sendPrompt(String message){

        String url = "https://router.huggingface.co/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);

        headers.setContentType(MediaType.APPLICATION_JSON);


        Map<String,Object> body = Map.of(
                "model", "zai-org/GLM-Image",
                "messages", List.of(Map.of("role", "user", "content", message) ),
                "max_tokens", 200 );

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(body, headers);

        return restTemplate.postForObject(url, entity, String.class);
    }

    public byte[] sendImagePrompt(String message){
        String url = "https://api.stability.ai/v2beta/stable-image/generate/sd3";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(imageApiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(List.of(MediaType.parseMediaType("image/*")));


        MultiValueMap<String, Object> body =  new LinkedMultiValueMap<>();
           body.add( "prompt", message);
           body.add("output_format", "png");
           body.add("aspect_ratio", "1:1" );

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

//        Map response = restTemplate.postForObject(url, entity, Map.class);


//        String base64 = (String) response.get("image");
        ResponseEntity<byte[]> response = restTemplate.exchange( url, HttpMethod.POST, entity, byte[].class );

        return response.getBody();

//        return restTemplate.postForObject(url, entity, byte[].class);
    }


}
