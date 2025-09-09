package cl.com.prime_it.user_service.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.com.prime_it.user_service.dto.request.PartialUserRequest;
import cl.com.prime_it.user_service.dto.request.UserRequest;
import cl.com.prime_it.user_service.dto.response.UserResponse;
import cl.com.prime_it.user_service.exception.EmailAlreadyRegisteredException;
import cl.com.prime_it.user_service.exception.InvalidEmailFormatException;
import cl.com.prime_it.user_service.exception.PasswordValidationException;
import cl.com.prime_it.user_service.exception.UserNotFoundException;
import cl.com.prime_it.user_service.mapper.UserMapper;
import cl.com.prime_it.user_service.model.User;
import cl.com.prime_it.user_service.repository.UserRepository;
import cl.com.prime_it.user_service.service.JwtService;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository repository;
    @Mock
    private UserMapper mapper;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl service;

    private UUID uuid;
    private User entity;
    private UserResponse response;
    private UserRequest createReq;
    private UserRequest updateReq;
    private PartialUserRequest partialReq;

    @BeforeEach
    void init() throws Exception {
        // arrange
        uuid = UUID.randomUUID();
        entity = new User();
        entity.setName("Juan Perez");
        entity.setEmail("juan.perez@example.com");
        entity.setLastLogin(LocalDateTime.now());
        entity.setActive(true);
        entity.setPassword("enc");
        entity.setToken("jwt");

        response = UserResponse.builder()
                .uuid(uuid)
                .name("Juan Perez")
                .email("juan.perez@example.com")
                .lastLogin(LocalDateTime.now())
                .active(true)
                .phones(List.of())
                .build();

        createReq = UserRequest.builder()
                .name("Juan Perez")
                .email("juan.perez@example.com")
                .password("S3cr3tPwd-Test")
                .phones(List.of())
                .build();

        updateReq = UserRequest.builder()
                .name("Juan Actualizado")
                .email("juan.actualizado@example.com")
                .password("N3wSecr3tPwd-Test")
                .phones(List.of())
                .build();

        partialReq = PartialUserRequest.builder()
                .active(true)
                .build();

        setPrivateField(service, "emailRegex", "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        setPrivateField(service, "passwordRegex", "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\-_!@#$%^&+=.]).{8,}$");
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }


    @Test
    void when_index_called_then_return_page_of_users() {
        // arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        Page<User> page = new PageImpl<>(List.of(entity), pageable, 1);
        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toResponse(any(User.class))).thenReturn(response);

        // act
        Page<UserResponse> result = service.index(pageable);

        // assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("juan.perez@example.com");
        verify(repository).findAll(pageable);
        verify(mapper).toResponse(entity);
    }


    @Test
    void when_create_with_valid_data_then_returns_created_response() {
        // arrange
        when(repository.existsByEmail(createReq.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(createReq.getPassword())).thenReturn("encrypted_password");
        when(mapper.toEntity(createReq)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        // act
        UserResponse res = service.create(createReq);

        // assert
        assertThat(res).isNotNull();
        assertThat(res.getEmail()).isEqualTo("juan.perez@example.com");
        verify(repository).existsByEmail(createReq.getEmail());
        verify(passwordEncoder).encode(createReq.getPassword());
        verify(jwtService).createToken(createReq.getName());
        verify(repository).save(entity);
    }

    @Test
    void when_create_with_invalid_email_then_throws_InvalidEmailFormatException() {
        // arrange
        UserRequest badReq = createReq = UserRequest.builder()
                .name("Juan Perez")
                .email("invalid-email")
                .password("S3cr3tPwd-Test")
                .phones(List.of())
                .build();

        // act & assert
        assertThrows(InvalidEmailFormatException.class, () -> service.create(badReq));
        verify(repository, never()).existsByEmail(any());
        verify(repository, never()).save(any());
    }

    @Test
    void when_create_with_weak_password_then_throws_PasswordValidationException() {
        // arrange
        UserRequest badReq = createReq = UserRequest.builder()
                .name("Juan Perez")
                .email("juan.perez@example.com")
                .password("short")
                .phones(List.of())
                .build();
        // act & assert
        assertThrows(PasswordValidationException.class, () -> service.create(badReq));
        verify(repository, never()).save(any());
    }

    @Test
    void when_create_with_existing_email_then_throws_EmailAlreadyRegisteredException() {
        // arrange
        when(repository.existsByEmail(createReq.getEmail())).thenReturn(true);
        // act & assert
        assertThrows(EmailAlreadyRegisteredException.class, () -> service.create(createReq));
        verify(repository, never()).save(any());
    }


    @Test
    void when_update_existing_user_then_saves_without_return() {
        // arrange
        when(repository.findById(uuid)).thenReturn(Optional.of(entity));
        when(repository.existsByEmail(updateReq.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(updateReq.getPassword())).thenReturn("new_encrypted_password");
        when(repository.save(entity)).thenReturn(entity);
        doNothing().when(mapper).updateModel(eq(updateReq), any(User.class));

        // act
        service.update(uuid, updateReq);

        // assert
        verify(repository).findById(uuid);
        verify(repository).existsByEmail(updateReq.getEmail());
        verify(mapper).updateModel(updateReq, entity);
        verify(repository).save(entity);
    }

    @Test
    void when_update_with_same_email_then_does_not_validate_existence() {
        // arrange
        UserRequest sameEmailReq = createReq;
        when(repository.findById(uuid)).thenReturn(Optional.of(entity));
        when(passwordEncoder.encode(sameEmailReq.getPassword())).thenReturn("new_encrypted_password");
        when(repository.save(entity)).thenReturn(entity);
        doNothing().when(mapper).updateModel(eq(sameEmailReq), any(User.class));

        // act
        service.update(uuid, sameEmailReq);

        // assert
        verify(repository, never()).existsByEmail(any());
        verify(repository).findById(uuid);
        verify(mapper).updateModel(sameEmailReq, entity);
        verify(repository).save(entity);
    }

    @Test
    void when_update_with_invalid_email_then_throws_InvalidEmailFormatException() {
        // arrange
        UserRequest badReq = UserRequest.builder()
                .name("Juan Perez")
                .email("invalid-email")
                .password("S3cr3tPwd-Test")
                .phones(List.of())
                .build();
        when(repository.findById(uuid)).thenReturn(Optional.of(entity));
        // act & assert
        assertThrows(InvalidEmailFormatException.class, () -> service.update(uuid, badReq));
        verify(repository, never()).existsByEmail(any());
        verify(repository, never()).save(any());
    }

    @Test
    void when_update_with_weak_password_then_throws_PasswordValidationException() {
        // arrange
        UserRequest badReq = UserRequest.builder()
                .name("Juan Perez")
                .email("juan.perez@example.com")
                .password("week")
                .phones(List.of())
                .build();
        when(repository.findById(uuid)).thenReturn(Optional.of(entity));
        // act & assert
        assertThrows(PasswordValidationException.class, () -> service.update(uuid, badReq));
        verify(repository, never()).save(any());
    }

    @Test
    void when_update_with_existing_email_then_throws_EmailAlreadyRegisteredException() {
        // arrange
        when(repository.findById(uuid)).thenReturn(Optional.of(entity));
        when(repository.existsByEmail(updateReq.getEmail())).thenReturn(true);
        // act & assert
        assertThrows(EmailAlreadyRegisteredException.class, () -> service.update(uuid, updateReq));
        verify(repository, never()).save(any());
    }

    @Test
    void when_update_not_found_then_throw_UserNotFoundException() {
        // arrange
        when(repository.findById(uuid)).thenReturn(Optional.empty());
        // act & assert
        assertThrows(UserNotFoundException.class, () -> service.update(uuid, updateReq));
        verify(repository, never()).save(any());
    }


    @Test
    void when_partialUpdate_existing_then_return_updated_response() {
        // arrange
        when(repository.findById(uuid)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);
        // act
        UserResponse res = service.partialUpdate(uuid, partialReq);
        // assert
        assertThat(res).isNotNull();
        verify(repository).findById(uuid);
        verify(repository).save(entity);
        verify(mapper).toResponse(entity);
    }

    @Test
    void when_partialUpdate_not_found_then_throw_UserNotFoundException() {
        // arrange
        when(repository.findById(uuid)).thenReturn(Optional.empty());
        // act & assert
        assertThrows(UserNotFoundException.class, () -> service.partialUpdate(uuid, partialReq));
        verify(repository, never()).save(any());
    }

    @Test
    void when_show_existing_then_return_response() {
        // arrange
        when(repository.findById(uuid)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);
        // act
        UserResponse res = service.show(uuid);
        // assert
        assertThat(res.getUuid()).isEqualTo(uuid);
        verify(repository).findById(uuid);
        verify(mapper).toResponse(entity);
    }

    @Test
    void when_show_not_found_then_throw_UserNotFoundException() {
        // arrange
        when(repository.findById(uuid)).thenReturn(Optional.empty());
        // act & assert
        assertThrows(UserNotFoundException.class, () -> service.show(uuid));
    }


    @Test
    void when_delete_existing_then_ok() {
        // arrange
        when(repository.findById(uuid)).thenReturn(Optional.of(new User()));
        doNothing().when(repository).delete(any(User.class));
        // act
        service.delete(uuid);
        // assert
        verify(repository).findById(uuid);
        verify(repository).delete(any(User.class));
    }

    @Test
    void when_delete_not_found_then_throw_UserNotFoundException() {
        // arrange
        when(repository.findById(uuid)).thenReturn(Optional.empty());
        // act & assert
        assertThrows(UserNotFoundException.class, () -> service.delete(uuid));
        verify(repository, never()).delete(any(User.class));
    }
}
