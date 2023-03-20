package ru.practicum.main_service.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.main_service.user.dto.NewUserRequest;
import ru.practicum.main_service.user.dto.UserDto;
import ru.practicum.main_service.user.dto.UserDtoShort;
import ru.practicum.main_service.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(NewUserRequest newUserRequest);

    UserDto toUserDto(User user);

    UserDtoShort toUserDtoShort(User user);
}
