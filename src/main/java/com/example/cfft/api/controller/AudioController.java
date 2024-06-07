package com.example.cfft.api.controller;

import com.example.cfft.common.utils.FileUtil;

import com.example.cfft.common.utils.TokenUtil;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.service.UserService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/audio")
@CrossOrigin("*")
@Tag(name = "音频转换", description = "处理音频文件的上传和转换")
public class AudioController {

    @Autowired
    private UserService userService;

    @Operation(summary = "转换音频文件", description = "上传并转换音频文件")
    @PostMapping("/convert")
    public ResultVO convertAudio(
            @Parameter(description = "上传的音频文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "用户的令牌", required = true) @RequestParam("token") String token) {
        if (file.isEmpty()) {
            return ResultVO.error("文件为空");
        }
        return Optional.ofNullable(TokenUtil.getUserIdFromToken(token))
                .flatMap(userId -> Optional.ofNullable(userService.getById(userId)))
                .map(user -> {
                    String uploadDir = user.getAvatar() + "audio/";

                    // 保存上传的文件
                    String originalFilePath = uploadDir;
                    String savedFilePath = FileUtil.saveFile(originalFilePath, file);

                    // 调用FFmpeg进行转换
                    String outputFilePath = FileUtil.extractAudio(savedFilePath);
                    return Optional.ofNullable(outputFilePath).map(path -> {
                        try {
                            Map<String, String> requestBody = new HashMap<>();
                            requestBody.put("file_path", path);

                            // 将请求体转换为JSON
                            ObjectMapper objectMapper = new ObjectMapper();
                            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

                            // 构建Http请求
                            HttpRequest request = HttpRequest.newBuilder()
                                    .uri(new URI("http://localhost:5001/convert"))
                                    .header("Content-Type", "application/json")
                                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                                    .build();

                            // 创建HttpClient并发送请求
                            HttpClient client = HttpClient.newHttpClient();
                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                            // 解析响应并返回result字段内容
                            JsonNode responseJson = objectMapper.readTree(response.body());
                            JsonNode resultNode = responseJson.path("result");

                            // 返回结果
                            if (resultNode.isMissingNode()) {
                                return ResultVO.error("result字段不存在");
                            } else {
                                return ResultVO.success(resultNode.toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return ResultVO.error("调用Flask服务失败");
                        }
                    }).orElse(ResultVO.error("wav文件生成失败"));
                }).orElse(ResultVO.error());
    }
}
