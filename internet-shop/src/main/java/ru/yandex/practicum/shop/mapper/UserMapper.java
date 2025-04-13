package ru.yandex.practicum.shop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.shop.dto.UserDto;
import ru.yandex.practicum.shop.dto.UserResponseDto;
import ru.yandex.practicum.shop.entity.User;
import ru.yandex.practicum.shop.util.PasswordEncoderUtils;

@Mapper(componentModel = "spring", uses = PasswordEncoderUtils.class)
public interface UserMapper {

    UserResponseDto map(User user);

    @Mapping(target = "password", source = "password", qualifiedByName = "encodePassword")
    @Mapping(target = "role", constant = "ROLE_CLIENT")
    @Mapping(target = "createdAt", expression = "java(userDto.getCreatedAt() == null ? java.time.LocalDate.now() : userDto.getCreatedAt())")
    @Mapping(target = "modifiedAt", expression = "java(userDto.getModifiedAt() == null ? java.time.LocalDate.now() : userDto.getModifiedAt())")
    User map(UserDto userDto);
}
