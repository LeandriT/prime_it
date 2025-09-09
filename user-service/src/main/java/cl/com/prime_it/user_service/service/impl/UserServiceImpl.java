package cl.com.prime_it.user_service.service.impl;

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
import cl.com.prime_it.user_service.service.UserService;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    //validation

    @Value("${app.config.email-pattern}")
    private String emailRegex;

    @Value("${app.config.password-pattern}")
    private String passwordRegex;

    @Override
    public Page<UserResponse> index(Pageable pageable) {
        log.info("Starting index users");
        return repository.findAll(pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse create(UserRequest userRequest) {
        log.info("Starting create users");
        this.validateEmailFormat(userRequest.getEmail());
        this.validatePasswordStrong(userRequest.getPassword());
        this.validateEmailAlreadyRegistered(userRequest.getEmail());
        String encryptedPassword = passwordEncoder.encode(userRequest.getPassword());
        User entity = mapper.toEntity(userRequest);
        entity.setLastLogin(LocalDateTime.now());
        entity.setActive(true);
        entity.setToken(jwtService.createToken(userRequest.getName()));
        entity.setPassword(encryptedPassword);
        repository.save(entity);
        log.info("Ending create users");
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void update(UUID uuid, UserRequest userRequest) {
        log.info("Starting update users");
        User user = repository.findById(uuid)
                .orElseThrow(() -> notFound(uuid));
        this.validateEmailFormat(userRequest.getEmail());
        this.validatePasswordStrong(userRequest.getPassword());

        if (!user.getEmail().equals(userRequest.getEmail())) {
            this.validateEmailAlreadyRegistered(userRequest.getEmail());
        }
        String encryptedPassword = passwordEncoder.encode(userRequest.getPassword());
        mapper.updateModel(userRequest, user);
        user.setPassword(encryptedPassword);
        repository.save(user);
        log.info("Ending update users");
    }

    @Override
    public UserResponse partialUpdate(UUID uuid, PartialUserRequest userRequest) {
        log.info("Starting partial update users");
        User user = repository.findById(uuid)
                .orElseThrow(() -> notFound(uuid));
        user.setActive(userRequest.getActive());
        repository.save(user);
        log.info("Ending partial update users");
        return mapper.toResponse(user);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public UserResponse show(UUID uuid) {
        log.info("Starting show users");
        User user = repository.findById(uuid)
                .orElseThrow(() -> notFound(uuid));
        log.info("Ending show users");
        return mapper.toResponse(user);
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        repository.delete(user);
    }

    private UserNotFoundException notFound(UUID uuid) {
        return new UserNotFoundException(uuid);
    }

    private void validateEmailFormat(String email) {
        if (Objects.nonNull(email) && !Pattern.matches(emailRegex, email)) {
            throw new InvalidEmailFormatException(email);
        }

    }

    private void validatePasswordStrong(String password) {
        if (Objects.nonNull(password) && !Pattern.matches(passwordRegex, password)) {
            throw new PasswordValidationException(password);
        }
    }

    private void validateEmailAlreadyRegistered(String email) {
        boolean existUserWithEmail = repository.existsByEmail(email);
        if (existUserWithEmail) {
            throw new EmailAlreadyRegisteredException(email);
        }
    }
}
