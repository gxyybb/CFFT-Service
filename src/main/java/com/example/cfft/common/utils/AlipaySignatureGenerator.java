package com.example.cfft.common.utils;

import com.alipay.api.internal.util.AlipaySignature;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class AlipaySignatureGenerator {
    public static void main(String[] args) throws Exception {
        String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDRd4XofFRJJ5KE3/RKEoaApgOv6Wx7ZpJtiKKTWi4PXWSqbHAqmsiNMFMFw4rpmtZp9sk6eiW+gjOgMHY923W1Urfy1yWnwEaAUvh+lore6Og/1Hbd94UM3t4JiUfv5AA7EWeNVjeCLRntK/TJsNX5DAUCXihQr6X2z1KYL3jH6KFHIczYkyRjV5hwq9FEmp6PTD7aJ6RzCTxNQj+ysbnaaIEnpZZLi16lktiPQCU2xWbHyW2ny9cgE+o2A9izp4QXiakfVeyn0m+gAbNGuqRHJyEuVgg7eylyTsaSe+XdHwB5K4DkQV4S7MIBTfKZtHiUpiIczainar9yf68KXDQXAgMBAAECggEAb8a0MmeGIrN3QGix14UjOStd9FU/imol4UV0W+VXhuNzgG31PQbz1kPfScgbLKVvnYQPxaDA23RnUCgMMN4XZfcKKhAdC/NSq8+awxJacLEgnk8mTzRyT3fuTHBxbtXgOaUD+Sv02wZuO12ldqtiw09irOzJ3qScj6P6kzNh5GqK0Naj+HEbUxVREeMRdiSJHkWvJw78iZOZ4ZdOZFfl2yaLjo/psFlijKC3YKdozu03jDxK6UoZlhWvsOt8A1LOQXAg/Juevi2CQDpuhKC8n+AxAy7jghZST1U9X6Qv+2wHfCWlRSvpAKJ6pSnLapvrMUhtiIs7qPfxBSEhfOn1IQKBgQD+EBBwVnGRCrKt9vDjpOobxZjC7Szuc+QnWjM8+jeFJIEUOnc4nJgogAlANb6PX0NGgQSschgKfcOaBqielSxK3Z4/7ylrIcuOehcoq0eLW78SQOG7sDhVNyX3mZ702eOSOUsO+5ZbL2DN9lOlOTVqREW63cN7T+V7uxc2ddiqowKBgQDTEGgknlIJX/WrrK1nxgDNucCA5eC2dgXfanTCvsGEa1LOii/5e76/sScQ40A4a3K8VMuN1w9lrqVrOJ1z7XltBZPnHlZpRB1OJZ5N0+Dy6K1q34R4sSL/hmJtbp45qRtCYszYi8CLvgNG4VZqKCLWlZG/7hLIu3zCM13Dsvg7/QKBgBMue08/94sP17WyszU8ukAmhwYQihfmwA8IjF3KNm7i9qvkTiQEDqjgIdKrPBvvAVFNZGDwrEf+fsoePpTahmGQoeYJ1IK2Jnw3U5BKfjhTUPW72E8nWtl0NHWhL5Mk8D2V/ci/zfZMeW6TbnVHAHVpeipFnACvY5AX7AS8mQMpAoGAaOl+2rTXklFgjDUI/MwvGhN63JdnW0rPIH6KC0NGbcvrHT8TGa+o4R/tizdqdKD0Jyj/BUIhJSnqj6FUoosoEEj1e6xYx3pUFsQcLBNlfViq2rS/5D4oZ1slEcAqZf7ozdbOe9T7oHqSpmjby2in6IHwMLbmH2ESYnWl5eDQAwECgYEA2qtvHcFdud/JqGH6yA3tGbsXZIfSaHUtgnRbqV+j9esh9P7fvdHanO1ClTuOzayeT+FCOdAgQTHkAF533IVZD59WqcLUd5wpWxWKmSxlMwzuIw7G5iZNh5ZXXZ7fwYwhVD+8Lvh+DNAGwMAE/jyVUmGtzWBqBfT4sAgboFLEZk0=";
        String charset = "UTF-8";
        String signType = "RSA2";

        // 需要签名的参数
        Map<String, String> params = new HashMap<>();
        params.put("out_trade_no", "20210612000001");
        params.put("trade_status", "TRADE_SUCCESS");
        params.put("total_amount", "0.01");
        params.put("app_id", "9021000137674643");
        params.put("sign_type", "RSA2");

        // 构建排序后的待签名字符串
        String content = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        // 生成签名
        String sign = AlipaySignature.rsaSign(content, privateKey, charset, signType);
        System.out.println("Generated Sign: " + sign);
    }
}
