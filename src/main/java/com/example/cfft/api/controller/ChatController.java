package com.example.cfft.api.controller;

import com.example.cfft.common.utils.PathUtil;
import com.example.cfft.common.utils.Static;
import com.example.cfft.common.vo.ResultVO;
import com.nimbusds.jose.shaded.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "聊天接口", description = "处理聊天相关操作")
@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private AudioController audioController;

    @Operation(summary = "发送聊天消息", description = "发送消息并获取聊天响应")
    @PostMapping
    @CrossOrigin(origins = "*")
    public ResultVO chat(
            @Parameter(description = "聊天消息", required = true) @RequestParam("message") String message,
            @Parameter(description = "是否使用GPT-4模型", required = false) @RequestParam(value = "use4", required = false, defaultValue = "false") boolean use4) {
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
                        return ResultVO.error("解析响应体时出错: " + e.getMessage());
                    }
                } else {
                    return ResultVO.error("响应体不是JSON格式");
                }
            } else {
                return ResultVO.error("意外的状态码: " + statusCode);
            }
        } catch (Exception e) {
            return ResultVO.error("请检查网络连接: " + e.getMessage());
        }
    }

    @Operation(summary = "通过文件发送聊天消息", description = "上传音频文件并获取聊天响应")
    @PostMapping("/file")
    @CrossOrigin(origins = "*")
    public ResultVO chatByFile(
            @Parameter(description = "音频文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "用户令牌", required = true) @RequestParam("token") String token,
            @Parameter(description = "是否使用GPT-4模型", required = false) @RequestParam(value = "use4", required = false, defaultValue = "false") boolean use4) {

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
                                            .map(list -> list.get(0));
                                })
                                .map(message-> {
                                    System.out.println(message);
                                            return chat(message, use4).getMsg();
                                        }
                                ).map(s -> {
                                    System.out.println(s);
                                    return synthesize(s);
                                });
                    }
                    return Optional.of(resultVO);
                })
                .orElse(ResultVO.error("上传的文件不是音频文件"));
    }

    @PostMapping("/synthesize")
    public ResultVO synthesize(@RequestParam String text) {
        String flaskUrl = "http://localhost:5000/synthesize";
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(120))
                .build();

        // 清理文本，去掉不必要的空白和重复
        text = text.trim();

        Map<Object, Object> data = new HashMap<>();
        data.put("text", text);

        // 将数据转换为表单格式
        String form = data.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        // 打印表单数据以进行调试
        System.out.println("Form data: " + form);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(flaskUrl))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                // 将返回的文件路径转换为完整的HTTP URL
                String processedString = PathUtil.convertToHttpUrl(responseBody);

                return ResultVO.success(processedString);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return ResultVO.error();
    }



}
