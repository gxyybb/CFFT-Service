package com.example.cfft.api.controller;

import com.example.cfft.common.utils.Static;
import com.example.cfft.common.vo.ResultVO;
import com.nimbusds.jose.shaded.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private AudioController audioController;

    @PostMapping
    @CrossOrigin(origins = "*")
    public ResultVO chat(@RequestParam("message") String message, @RequestParam(value = "use4", required = false, defaultValue = "false") boolean use4) {
        try {
            System.out.println(message);
            String url = Static.CHAT_URL;
            String requestData = use4 ?
                    String.format("{\"input_string\": \"%s\", \"model_version\": \"gpt-4\"}", message) :
                    String.format("{\"input_string\": \"%s\"}", message);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestData))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode == 200) {
                if (responseBody.startsWith("{")) {
                    Gson gson = new Gson();
                    try {
                        Map<String, Object> responseMap = gson.fromJson(responseBody, Map.class);
                        String generatedText = responseMap.get("generated_text").toString();
                        return ResultVO.success(generatedText);
                    } catch (Exception e) {
                        return ResultVO.error("Error parsing response body: " + e.getMessage());
                    }
                } else {
                    return ResultVO.error("Response body is not in JSON format");
                }
            } else {
                return ResultVO.error("Unexpected status code: " + statusCode);
            }
        } catch (Exception e) {
            return ResultVO.error("请检查网络连接: " + e.getMessage());
        }
    }

    @PostMapping("/file")
    @CrossOrigin(origins = "*")
    public ResultVO chatByFile(@RequestParam("file") MultipartFile file, @RequestParam("token") String token, @RequestParam(value = "use4", required = false, defaultValue = "false") boolean use4) {
        Map<String, String> map = new HashMap<>();

        return Optional.ofNullable(file.getContentType())
                .filter(contentType -> contentType.startsWith("audio/"))
                .map(contentType -> audioController.convertAudio(file, token))
                .flatMap(resultVO -> {
                    if (resultVO.getCode() == 200) {
                        return Optional.ofNullable(resultVO.getMsg())
                                .flatMap(msg -> {
                                    Gson gson = new Gson();
                                    List<String> messageList = gson.fromJson(msg, List.class);
                                    return Optional.ofNullable(messageList)
                                            .filter(list -> !list.isEmpty())
                                            .map(list -> {
                                                map.put("user", list.get(0));
                                                return list.get(0);
                                            });
                                })
                                .map(message -> {
                                    ResultVO chatResult = chat(message, use4);
                                    map.put("ai", chatResult.getMsg());
                                    Gson gson = new Gson();
                                    return ResultVO.success(gson.toJson(map));
                                });
                    }
                    return Optional.of(resultVO);
                })
                .orElse(ResultVO.error("上传的文件不是音频文件"));
    }

}
