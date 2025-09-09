package cl.com.prime_it.user_service.service.impl;

import cl.com.prime_it.user_service.service.JwtService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.time.Instant;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JWTServiceImpl implements JwtService {
    @Value("${app.config.expiration-date:28800000}")
    private long expirationDate;
    @Value("${app.config.jwt-sign}")
    private String sign;

    @Override
    public String createToken(String name) {
        return JWT.create()
                .withClaim("name", name)
                .withIssuedAt(Instant.now())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationDate))
                .sign(Algorithm.HMAC256(sign));
    }
}
