package cl.com.prime_it.user_service.mapper;

import cl.com.prime_it.user_service.dto.request.UserRequest;
import cl.com.prime_it.user_service.dto.response.UserResponse;
import cl.com.prime_it.user_service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;


@Mapper(
        componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {
    User toEntity(UserRequest userRequest);

    UserResponse toResponse(User user);

    void updateModel(UserRequest userRequest, @MappingTarget User user);
}