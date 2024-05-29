package com.example.cfft.common.utils;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;

import java.security.SecureRandom;
import java.util.Date;

import java.util.Optional;
import java.util.UUID;

public class TokenUtil {

    // 修改为你的新密钥
    private static final byte[] SECRET_KEY = generate256BitKey();

    private static byte[] generate256BitKey() {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[32];
        random.nextBytes(key);
        return key;
    }


    public static String generateToken(String userId) {
        try {
            JWSSigner signer = new MACSigner(SECRET_KEY);


            JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
                    .subject(userId)
                    .issueTime(new Date())
                    .jwtID(UUID.randomUUID().toString())
                    .expirationTime(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                    .claim("userId", userId)
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaims);

            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Integer getUserIdFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(SECRET_KEY);

            if (signedJWT.verify(verifier)) {
                JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
                return Integer.valueOf(claims.getSubject());
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean verifyToken(String token) {
        // 从 token 中解析用户 ID
        Optional<Integer> userIdFromToken = Optional.ofNullable(getUserIdFromToken(token));

        // 获取用户 ID 或提供一个默认值
        Integer userId = userIdFromToken.orElse(-1);

        // 检查用户 ID 是否为预期的管理员 ID
        return userId.equals(123456789);
    }
}
