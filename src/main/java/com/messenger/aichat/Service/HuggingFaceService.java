package com.messenger.aichat.Service;

import com.messenger.aichat.Configuration.HuggingFaceConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.List;
import java.util.Map;

@Service
public class HuggingFaceService {

    private final HuggingFaceConfig huggingFaceConfig;
    private final String apiKey;
    private final RestTemplate restTemplate;

    public HuggingFaceService(HuggingFaceConfig huggingFaceConfig, @Value("${huggingface.api.key}") String apiKey, RestTemplate restTemplate) {
        this.huggingFaceConfig = huggingFaceConfig;
        this.apiKey=apiKey;
        this.restTemplate = restTemplate;
    }


    public String sendPrompt(String message){

        String url = "https://router.huggingface.co/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);

        headers.setContentType(MediaType.APPLICATION_JSON);


        Map<String,Object> body = Map.of(
                "model", "meta-llama/Meta-Llama-3-70B-Instruct",
                "messages", List.of(Map.of("role", "user", "content", message) ),
                "max_tokens", 200 );

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(body, headers);

        return restTemplate.postForObject(url, entity, String.class);
    }

}
