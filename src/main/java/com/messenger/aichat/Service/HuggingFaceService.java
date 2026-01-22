package com.messenger.aichat.Service;

import com.messenger.aichat.Configuration.HuggingFaceConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

@Service
public class HuggingFaceService {

    private final HuggingFaceConfig huggingFaceConfig;
    private final String apiKey;
    private final RestTemplate restTemplate;
    private final String imageApiKey;
    private final String neural4dApiKey;

    public HuggingFaceService(HuggingFaceConfig huggingFaceConfig, @Value("${huggingface.api.key}") String apiKey,
                              @Value("${stabilityai.api.key}") String imageApiKey,
                              RestTemplate restTemplate,@Value("${neural4d.api.key}")
                                  String neural4dApiKey) {
        this.neural4dApiKey = neural4dApiKey;
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
//    public byte[] send3DtextPrompt(String message) throws InterruptedException {
//
//        String url="https://api.neural4d.com/v1/tasks/text-to-3d";
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(neural4dApiKey);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
//
//        Map<String,Object> body = Map.of(
//                "prompt",message,
//                "version","premium-v1",
//                "faceLimit",50000);
//        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(body, headers);
//        Map<String,Object> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class).getBody();
//        String task_ID = (String) response.get("id");
//
//        String taskUrl = "https://api.neural4d.com/v1/tasks/"+task_ID;
//
//        ResponseEntity<Map> StatusResponse = restTemplate.exchange(taskUrl,HttpMethod.GET,entity, Map.class);
//
//        Map<String, Object> statusBody = StatusResponse.getBody();
//        String state = (String) statusBody.get("status");
//        while (true){
//
//            if ("completed".equalsIgnoreCase(state)){
//                String downloadUrl = (String) statusBody.get("downloadUrl");
//                return download3DModel(downloadUrl);
//
//            }
//            if ("failed".equalsIgnoreCase(state)) {
//                throw new IllegalStateException("Neural4D task failed: " + task_ID);
//            }
//
//            Thread.sleep(3000);
//
//
//        }


//    }

    public byte[] send3DtextPrompt(String message) throws InterruptedException, IOException {


        String createUrl = "https://alb.neural4d.com:3000/api/generateModelWithText";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(neural4dApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> body = Map.of(
                "prompt", message,
                "modelCount", 1,
                "disablePbr", 0
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        Map<String, Object> response =
                restTemplate.exchange(createUrl, HttpMethod.POST, entity, Map.class).getBody();

        if (response == null || !response.containsKey("uuids")) {
            throw new IllegalStateException("Neural4D did not return UUID");
        }

        List<String> uuids = (List<String>) response.get("uuids");
        String uuid = uuids.get(0);


        String retrieveUrl = "https://alb.neural4d.com:3000/api/retrieveModel";

        Map<String, Object> retrieveBody = Map.of("uuid", uuid);
        HttpEntity<Map<String, Object>> retrieveEntity = new HttpEntity<>(retrieveBody, headers);

        while (true) {

            Map<String, Object> retrieveResponse =
                    restTemplate.exchange(retrieveUrl, HttpMethod.POST, retrieveEntity, Map.class).getBody();

            if (retrieveResponse == null) {
                throw new IllegalStateException("Neural4D returned empty retrieveModel response");
            }

            Integer codeStatus = (Integer) retrieveResponse.get("codeStatus");

            if (codeStatus == 0) {

                String modelUrl = (String) retrieveResponse.get("modelUrl");
                byte[] glbBytes = download3DModel(modelUrl);

                Path saved = SaveFile(glbBytes, "model.glb", "generated_models");

                System.out.println("Saved to: " + saved.toAbsolutePath());

                return download3DModel(modelUrl);
            }

            if (codeStatus == -1) {
                throw new IllegalStateException("Neural4D token invalid or expired");
            }

            if (codeStatus == -2) {
                throw new IllegalStateException("UUID does not exist: " + uuid);
            }

            if (codeStatus == -3) {
                throw new IllegalStateException("Model generation failed: " + uuid);
            }


            Thread.sleep(3000);
        }
    }

    public Path SaveFile(byte[] data , String fileName, String folderName) throws IOException {

        Path path = Paths.get(folderName);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        Path filepath = path.resolve(fileName);
        Files.write(filepath, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return filepath;

    }





    public byte[] download3DModel(String downloadUrl){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(neural4dApiKey);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity <byte[]> response = restTemplate.exchange(downloadUrl, HttpMethod.GET, entity, byte[].class);
        return response.getBody();
    }


}
