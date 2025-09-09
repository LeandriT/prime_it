package cl.com.prime_it.user_service.integrationTest;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.com.prime_it.user_service.dto.request.PartialUserRequest;
import cl.com.prime_it.user_service.model.Phone;
import cl.com.prime_it.user_service.model.User;
import cl.com.prime_it.user_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    private UUID existingId;

    @BeforeEach
    void setUp() {

        User u = new User();
        u.setName("Jane Doe");
        u.setEmail("jane.doe@test.dev");
        u.setPassword("Secret123!");
        u.setActive(true);
        u.setLastLogin(LocalDateTime.now());
        u.setPhones(new ArrayList<>(List.of(
                phone("+56912345678", "2", "56")
        )));

        existingId = userRepository.save(u).getUuid();
    }


    private Phone phone(String number, String city, String country) {
        Phone p = new Phone();
        p.setNumber(number);
        p.setCityCode(city);
        p.setCountryCode(country);
        return p;
    }

    private String toJson(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }


    @Test
    @DisplayName("GET /api/users debe retornar 200 y página vacía o con datos")
    void when_index__then_200() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /api/users debe crear y retornar 201 con el usuario")
    void when_create_valid__then_201_and_body() throws Exception {
        // arrange
        Map<String, Object> req = Map.of(
                "name", "John Smith",
                "email", "john.smith@test.dev",
                "password", "S3cret!",
                "phones", List.of(Map.of("number", "1234567", "cityCode", 2, "countryCode", 56))
        );

        // act & assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(jsonPath("$.name").value("John Smith"))
                .andExpect(jsonPath("$.email").value("john.smith@test.dev"))
                .andExpect(jsonPath("$.phones[0].number").value("1234567"));
    }

    @Test
    @DisplayName("POST /api/users con email inválido debe retornar 400")
    void when_create_invalid_email__then_400() throws Exception {
        Map<String, Object> req = Map.of(
                "name", "Bad Email",
                "email", "bad-email",
                "password", "S3cret!",
                "phones", List.of(Map.of("number", "123", "cityCode", 2, "countryCode", 56))
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/users/{uuid} debe retornar 200 cuando existe")
    void when_show_exists__then_200() throws Exception {
        mockMvc.perform(get("/api/users/{uuid}", existingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(existingId.toString()))
                .andExpect(jsonPath("$.email").value("jane.doe@test.dev"));
    }

    @Test
    @DisplayName("GET /api/users/{uuid} debe retornar 404 cuando no existe")
    void when_show_not_found__then_404() throws Exception {
        mockMvc.perform(get("/api/users/{uuid}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/users/{uuid} debe retornar 204 al actualizar")
    void when_update_valid__then_204() throws Exception {
        Map<String, Object> req = Map.of(
                "name", "Jane Updated",
                "email", "jane.doe@test.dev",
                "password", "Secret123!",
                "phones", List.of(Map.of("number", "9999999", "cityCode", 9, "countryCode", 56))
        );

        mockMvc.perform(put("/api/users/{uuid}", existingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isNoContent());

        User updated = userRepository.findById(existingId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Jane Updated");
        assertThat(updated.getPhones()).hasSize(1);
        assertThat(updated.getPhones().get(0).getNumber()).isEqualTo("9999999");
    }

    @Test
    @DisplayName("PATCH /api/users/{uuid} debe retornar 200 y reflejar cambio")
    void when_patch_active__then_200() throws Exception {
        PartialUserRequest partialUserRequest = PartialUserRequest.builder().active(true).build();

        mockMvc.perform(patch("/api/users/{uuid}", existingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(partialUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(existingId.toString()))
                .andExpect(jsonPath("$.is_active").value(true));
    }

    @Test
    @DisplayName("DELETE /api/users/{uuid} debe retornar 204 y eliminar")
    void when_delete__then_204() throws Exception {
        mockMvc.perform(delete("/api/users/{uuid}", existingId))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(existingId)).isFalse();
    }
}