package cl.com.prime_it.user_service.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JWTServiceImplTest {

    private JWTServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        // arrange
        service = new JWTServiceImpl();
        setPrivateField(service, "sign", "test-secret");
        setPrivateField(service, "expirationDate", 5_000L);
    }

    @Test
    void when__createToken__then_contains_name_and_valid_signature() {
        // arrange
        String name = "Juan Perez";
        Algorithm alg = Algorithm.HMAC256("test-secret");
        JWTVerifier verifier = JWT.require(alg).build();

        // act
        String token = service.createToken(name);
        DecodedJWT jwt = verifier.verify(token);

        // assert
        assertThat(jwt).isNotNull();
        assertThat(jwt.getClaim("name").asString()).isEqualTo(name);
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isNotNull();
    }

    @Test
    void when__createToken_with_expiration_config__then_exp_is_now_plus_delta() {
        // arrange
        long expectedMs = 5_000L;

        // act
        String token = service.createToken("any");
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256("test-secret")).build().verify(token);

        // assert
        long iatMs = jwt.getIssuedAt().getTime();
        long expMs = jwt.getExpiresAt().getTime();
        long diffMs = expMs - iatMs;

        assertThat(diffMs).isBetween(expectedMs - 1_000L, expectedMs + 1_000L);
    }

    @Test
    void when__verify_with_wrong_secret__then_throws() {
        // arrange
        String token = service.createToken("name");
        JWTVerifier wrong = JWT.require(Algorithm.HMAC256("other-secret")).build();

        // act // assert
        assertThrows(Exception.class, () -> wrong.verify(token));
    }


    private static void setPrivateField(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}