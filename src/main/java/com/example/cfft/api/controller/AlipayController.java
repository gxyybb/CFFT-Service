package com.example.cfft.api.controller;

import com.example.cfft.api.config.AlipayConfig;
import com.example.cfft.service.AlipayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@Tag(name = "支付宝支付", description = "处理支付宝支付相关操作")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private AlipayConfig alipayConfig;

    @Operation(summary = "创建支付请求", description = "根据传入的订单号、金额和标题创建支付请求")
    @GetMapping("/pay")
    public String pay(
            @Parameter(description = "订单号", required = true) @RequestParam("outTradeNo") String outTradeNo,
            @Parameter(description = "总金额", required = true) @RequestParam("totalAmount") Double totalAmount,
            @Parameter(description = "标题", required = true) @RequestParam("subject") String subject) {
        try {
            return alipayService.createPayment(outTradeNo, totalAmount, subject);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @Operation(summary = "处理支付通知", description = "处理支付宝支付后的异步通知")
    @PostMapping("/notify_url")
    public String notify(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> params = new HashMap<>();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            params.put(entry.getKey(), String.join(",", entry.getValue()));
        }

        System.out.println("params:" + params);

        // 使用Optional处理支付结果
        String outTradeNo = Optional.ofNullable(params.get("out_trade_no")).orElse("");
        String tradeStatus = Optional.ofNullable(params.get("trade_status")).orElse("");

        System.out.println(tradeStatus);

        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            // 支付成功，更新订单状态
            // 在这里处理订单状态更新的逻辑
        }

        return "success";
    }
}
