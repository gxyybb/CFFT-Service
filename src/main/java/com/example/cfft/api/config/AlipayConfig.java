package com.example.cfft.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlipayConfig {

    @Value("${alipay.gateway.url}")
    private String gatewayUrl;

    @Value("${alipay.app.id}")
    private String appId;

    @Value("${alipay.merchant.private.key}")
    private String merchantPrivateKey;

    @Value("${alipay.alipay.public.key}")
    private String alipayPublicKey;

    @Value("${alipay.sign.type}")
    private String signType;

    @Value("${alipay.charset}")
    private String charset;

    @Value("${alipay.return-url}")
    private String returnUrl;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    // Getters
    public String getGatewayUrl() { return gatewayUrl; }
    public String getAppId() { return appId; }
    public String getMerchantPrivateKey() { return merchantPrivateKey; }
    public String getAlipayPublicKey() { return alipayPublicKey; }
    public String getSignType() { return signType; }
    public String getCharset() { return charset; }
    public String getReturnUrl() { return returnUrl; }
    public String getNotifyUrl() { return notifyUrl; }
}
