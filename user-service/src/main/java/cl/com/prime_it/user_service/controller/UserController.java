package cl.com.prime_it.user_service.controller;

import cl.com.prime_it.user_service.dto.request.PartialUserRequest;
import cl.com.prime_it.user_service.dto.request.UserRequest;
import cl.com.prime_it.user_service.dto.response.UserResponse;
import cl.com.prime_it.user_service.dto.retention.OnCreate;
import cl.com.prime_it.user_service.dto.retention.OnUpdate;
import cl.com.prime_it.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Operaciones CRUD para usuarios")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Listar usuarios",
            description = "Obtiene una lista paginada de usuarios.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Listado de usuarios",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))
                            )
                    )
            }
    )
    public ResponseEntity<Page<UserResponse>> index(Pageable pageable) {
        return ResponseEntity.ok(userService.index(pageable));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Crear nuevo usuario",
            description = "Crea un nuevo usuario.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos del usuario a crear",
                    content = @Content(schema = @Schema(implementation = UserRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Usuario creado exitosamente",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solicitud inválida",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<UserResponse> create(@Validated(OnCreate.class) @RequestBody UserRequest userRequest) {
        return new ResponseEntity<>(userService.create(userRequest), HttpStatus.CREATED);
    }

    @PutMapping(path = "/{uuid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza todos los datos de un usuario existente.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos del usuario a actualizar",
                    content = @Content(schema = @Schema(implementation = UserRequest.class))
            ),
            parameters = {
                    @Parameter(name = "uuid", description = "UUID del usuario", required = true)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Usuario actualizado exitosamente"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Usuario no encontrado",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<Void> update(@PathVariable UUID uuid,
                                       @RequestBody @Validated(OnUpdate.class) UserRequest userRequest) {
        userService.update(uuid, userRequest);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/{uuid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Actualización parcial del usuario",
            description = "Actualiza parcialmente los datos de un usuario.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos parciales del usuario a actualizar",
                    content = @Content(schema = @Schema(implementation = PartialUserRequest.class))
            ),
            parameters = {
                    @Parameter(name = "uuid", description = "UUID del usuario", required = true)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Usuario actualizado parcialmente",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Usuario no encontrado",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<UserResponse> partialUpdate(@PathVariable UUID uuid,
                                                      @Valid @RequestBody PartialUserRequest userRequest) {
        return ResponseEntity.ok(userService.partialUpdate(uuid, userRequest));
    }

    @GetMapping(path = "/{uuid}")
    @Operation(
            summary = "Obtener usuario por ID",
            parameters = {
                    @Parameter(name = "uuid", description = "UUID del usuario", required = true)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Usuario encontrado",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Usuario no encontrado",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<UserResponse> show(@PathVariable UUID uuid) {
        return ResponseEntity.ok(userService.show(uuid));
    }

    @DeleteMapping(path = "/{uuid}")
    @Operation(
            summary = "Eliminar usuario",
            parameters = {
                    @Parameter(name = "uuid", description = "UUID del usuario", required = true)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Usuario eliminado exitosamente"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Usuario no encontrado",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        userService.delete(uuid);
        return ResponseEntity.noContent().build();
    }
}