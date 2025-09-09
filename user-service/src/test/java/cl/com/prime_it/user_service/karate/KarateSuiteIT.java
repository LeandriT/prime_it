package cl.com.prime_it.user_service.karate;

import cl.com.prime_it.user_service.config.TestSecurityConfig;
import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestSecurityConfig.class)
public class KarateSuiteIT {

    @LocalServerPort
    int port;

    @BeforeAll
    void beforeAll() {
        System.setProperty("karate.baseUrl", "http://localhost:" + port);
        System.out.println(">> karate.baseUrl = http://localhost:" + port);
    }

    @Karate.Test
    Karate users() {
        return Karate.run("classpath:karate/users.feature");
    }
}