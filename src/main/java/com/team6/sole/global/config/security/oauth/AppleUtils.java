package com.team6.sole.global.config.security.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.sole.global.config.security.oauth.dto.AppleClientRequestDto;
import com.team6.sole.global.config.security.oauth.dto.ApplePublicKeyResponse;
import com.team6.sole.global.config.security.oauth.dto.AppleResponseToken;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.BadRequestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AppleUtils {
    @Value("${APPLE.AUD}")
    private String AUD;

    @Value("${APPLE.TEAM.ID}")
    private String TEAM_ID;

    @Value("${APPLE.KEY.ID}")
    private String KEY_ID;

    private final WebClient webClient;


    /**
     * Apple Server에서 공개 키를 받아서 서명 확인
     *
     * @param identityToken
     * @return
     */
    public Claims verifyPublicKey(String identityToken) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException, JsonProcessingException {

        ApplePublicKeyResponse response = getApplePublicKey();

        String headerOfIdentityToken = identityToken.substring(0, identityToken.indexOf("."));
        Map<String, String> header = new ObjectMapper().readValue(new String(Base64.getDecoder().decode(headerOfIdentityToken), "UTF-8"), Map.class);
        ApplePublicKeyResponse.Key key = response.getMatchedKeyBy(header.get("kid"), header.get("alg"))
                .orElseThrow(() -> new NullPointerException("Failed get public key from apple's id server."));

        byte[] nBytes = Base64.getUrlDecoder().decode(key.getN());
        byte[] eBytes = Base64.getUrlDecoder().decode(key.getE());

        BigInteger n = new BigInteger(1, nBytes);
        BigInteger e = new BigInteger(1, eBytes);

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
        KeyFactory keyFactory = KeyFactory.getInstance(key.getKty());
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(identityToken)
                .getBody();
    }

    // 애플에서 공개키 받기
    public ApplePublicKeyResponse getApplePublicKey() {
        String getAppleURL = "https://appleid.apple.com/auth/keys";

        try {
            return webClient.get()
                    .uri(getAppleURL)
                    .retrieve()
                    .bodyToMono(ApplePublicKeyResponse.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(ErrorCode.APPLE_BAD_REQUEST);
        }
    }

    // token 발급을 위한 client_secret 생성
    public String makeClientSecret() throws IOException {
        Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .setHeaderParam("kid", KEY_ID)
                .setHeaderParam("alg", "ES256")
                .setIssuer(TEAM_ID)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .setAudience("https://appleid.apple.com")
                .setSubject(AUD)
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();
    }

    // token 발급을 위한 private key 생성
    private PrivateKey getPrivateKey() throws IOException {
        ClassPathResource resource = new ClassPathResource("AuthKey_8N7FLJNRMM.p8");
        String privateKey = new String(Files.readAllBytes(Paths.get(resource.getURI())));
        Reader pemReader = new StringReader(privateKey);
        PEMParser pemParser = new PEMParser(pemReader);
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
        return converter.getPrivateKey(object);
    }

    // token 발급
    public AppleResponseToken getToken(AppleClientRequestDto appleClientRequestDto) throws IOException {
        String getAppleTokenURL = "https://appleid.apple.com/auth/token";

        try {
            return webClient
                    .post()
                    .uri(getAppleTokenURL, builder -> {
                        try {
                            return builder
                                    .queryParam("code", appleClientRequestDto.getAuthorizationCode())
                                    .queryParam("client_id", AUD)
                                    .queryParam("client_secret", makeClientSecret())
                                    .queryParam("grant_type", "authorization_code")
                                    .build();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .retrieve()
                    .bodyToMono(AppleResponseToken.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(ErrorCode.APPLE_BAD_REQUEST);
        }
    }
}
