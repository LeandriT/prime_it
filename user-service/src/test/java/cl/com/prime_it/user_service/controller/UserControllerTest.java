package cl.com.prime_it.user_service.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.com.prime_it.user_service.dto.request.PartialUserRequest;
import cl.com.prime_it.user_service.dto.request.PhoneRequest;
import cl.com.prime_it.user_service.dto.request.UserRequest;
import cl.com.prime_it.user_service.dto.response.PhoneResponse;
import cl.com.prime_it.user_service.dto.response.UserResponse;
import cl.com.prime_it.user_service.exception.GlobalExceptionHandler;
import cl.com.prime_it.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    private static final String BASE = "/api/users";

    @Resource
    private MockMvc mockMvc;

    @Resource
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UUID uuid;
    private UserResponse sampleResponse;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        sampleResponse = UserResponse.builder()
                .uuid(uuid)
                .name("Juan Perez")
                .email("juan.perez@example.com")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now().minusHours(1))
                .token("token-x")
                .active(true)
                .phones(List.of(this.samplePhoneResponse()))
                .build();
    }

    private UserRequest sampleCreateRequest() {
        return UserRequest.builder()
                .name("Juan Perez")
                .email("juan.perez@example.com")
                .password("Secr3tPwd")
                .phones(List.of(this.samplePhoneRequest()))
                .build();
    }

    private PhoneRequest samplePhoneRequest() {
        return PhoneRequest.builder()
                .cityCode("CODE-001")
                .countryCode("CH")
                .number("STRET 101")
                .build();
    }

    private PhoneResponse samplePhoneResponse() {
        return PhoneResponse.builder()
                .cityCode("CODE-001")
                .countryCode("CH")
                .number("STRET 101")
                .build();
    }

    private UserRequest sampleUpdateRequest() {

        return UserRequest.builder()
                .name("Juan Actualizado")
                .email("juan.actualizado@example.com")
                .password("NewSecr3tPwd")
                .phones(List.of())
                .build();
    }

    private PartialUserRequest samplePartialRequest() {
        return PartialUserRequest.builder()
                .active(true)
                .build();
    }

    @Test
    void index_ok() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of(sampleResponse),
                PageRequest.of(0, 20, Sort.by("name")), 1);

        Mockito.when(userService.index(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get(BASE)
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].uuid", is(uuid.toString())))
                .andExpect(jsonPath("$.content[0].email", is("juan.perez@example.com")));
    }

    @Test
    void create_created() throws Exception {
        UserRequest req = sampleCreateRequest();
        Mockito.when(userService.create(any(UserRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid", is(uuid.toString())))
                .andExpect(jsonPath("$.email", is("juan.perez@example.com")));
    }

    @Test
    void create_validationError_badRequest() throws Exception {

        UserRequest invalid = UserRequest.builder()
                .name("")
                .email("no-es-email")
                .password("")
                .phones(List.of())
                .build();

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Validation failed")));
    }

    @Test
    void update_noContent() throws Exception {
        UserRequest req = sampleUpdateRequest();

        Mockito.doNothing().when(userService).update(eq(uuid), any(UserRequest.class));

        mockMvc.perform(put(BASE + "/{uuid}", uuid.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    void partialUpdate_ok() throws Exception {
        PartialUserRequest req = samplePartialRequest();
        sampleResponse.setName("Nombre Parcial");
        UserResponse updated = sampleResponse;

        Mockito.when(userService.partialUpdate(eq(uuid), any(PartialUserRequest.class))).thenReturn(updated);

        mockMvc.perform(patch(BASE + "/{uuid}", uuid.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Nombre Parcial")));
    }

    @Test
    void show_ok() throws Exception {
        Mockito.when(userService.show(uuid)).thenReturn(sampleResponse);

        mockMvc.perform(get(BASE + "/{uuid}", uuid.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is(uuid.toString())))
                .andExpect(jsonPath("$.email", is("juan.perez@example.com")));
    }

    @Test
    void delete_noContent() throws Exception {
        Mockito.doNothing().when(userService).delete(uuid);

        mockMvc.perform(delete(BASE + "/{uuid}", uuid.toString()))
                .andExpect(status().isNoContent());
    }
}