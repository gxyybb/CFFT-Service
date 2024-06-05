package com.example.cfft.api.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.example.cfft.api.config.AlipayConfig;
import com.example.cfft.service.AlipayService;
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
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private AlipayConfig alipayConfig;

    @GetMapping("/pay")
    public String pay(@RequestParam("outTradeNo") String outTradeNo,
                      @RequestParam("totalAmount") Double totalAmount,
                      @RequestParam("subject") String subject) {
        try {
            return alipayService.createPayment(outTradeNo, totalAmount, subject);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

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
