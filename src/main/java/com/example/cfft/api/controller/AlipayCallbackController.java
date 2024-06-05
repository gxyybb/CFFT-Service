package com.example.cfft.api.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.example.cfft.api.config.AlipayConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AlipayCallbackController {

    @Autowired
    private AlipayConfig alipayConfig;

    @GetMapping("/alipay/callback")
    public String callback(HttpServletRequest request,
                           @RequestParam Map<String, String> params) {
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
