package xyz.shiqihao.common.util;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class JwtUtils {
    private final String secretKey;

    public JwtUtils(@Value("${jwtSecretKey}") String secretKey) {
        this.secretKey = secretKey;
    }

    private SecretKey generateKey() {
        // https://github.com/jwtk/jjwt?tab=readme-ov-file#secret-keys
        SecretKey secretKey = Jwts.SIG.HS256.key().build();
        String secretStr = Encoders.BASE64URL.encode(secretKey.getEncoded());
        log.info(secretStr);
        return secretKey;
    }

    private SecretKey loadSecretKey() {
        // https://github.com/jwtk/jjwt?tab=readme-ov-file#secretkey-formats
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
    }

    public String buildJwt(Long userId, String userName) {
        return Jwts.builder()
                .claims()
                .add("userId", String.valueOf(userId))
                .add("userName", userName)
                .add("expireAt", LocalDateTime.now().plusWeeks(1).toString())
                .and()
                .signWith(loadSecretKey())
                .compact();
    }

    public Map<String, String> parseJwt(String token) {
        Claims jwtClaim = Jwts.parser().verifyWith(loadSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Map<String, String> res = new HashMap<>();
        if (jwtClaim.containsKey("userId")) {
            res.put("userId", (String) jwtClaim.get("userId"));
        }
        if (jwtClaim.containsKey("userName")) {
            res.put("userName", (String) jwtClaim.get("userName"));
        }
        if (jwtClaim.containsKey("expireAt")) {
            res.put("expireAt", (String) jwtClaim.get("expireAt"));
        }
        return res;
    }
}
