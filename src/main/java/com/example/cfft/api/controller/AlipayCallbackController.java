package com.example.cfft.api.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.example.cfft.api.config.AlipayConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "支付宝回调", description = "处理支付宝回调请求")
public class AlipayCallbackController {

    @Autowired
    private AlipayConfig alipayConfig;

    @Operation(summary = "处理支付宝回调请求", description = "验证支付宝签名并处理回调请求")
    @GetMapping("/alipay/callback")
    public String callback(HttpServletRequest request,
                           @Parameter(description = "支付宝返回的参数") @RequestParam Map<String, String> params) {
        // 获取支付宝返回的参数
        Map<String, String> parameterMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            parameterMap.put(entry.getKey(), String.join(",", entry.getValue()));
        }

        // 验证签名
        boolean signVerified = false;
        try {
            signVerified = AlipaySignature.rsaCheckV1(parameterMap,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType());
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return "error";
        }

        if (signVerified) {
            // 处理授权成功逻辑
            String authCode = params.get("auth_code");
            String appId = params.get("app_id");
            // 根据业务需求处理授权码和应用ID
            return "success";
        } else {
            return "fail";
        }
    }
}
