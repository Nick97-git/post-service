package com.dev.post.mapper;

import com.dev.post.model.User;
import com.dev.post.model.dto.UserResponseDto;
import com.dev.post.model.dto.UserUpdateDto;
import java.util.Base64;
import java.util.Objects;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class UserMapper {

    public UserResponseDto convertUserToUserResponseDto(User user) {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setLogin(user.getLogin());
        userResponseDto.setFullName(user.getFullName());
        return userResponseDto;
    }

    public String getPhoto(User user) {
        return Objects.isNull(user.getPhoto())
                ? null
                : Base64.getEncoder().encodeToString(user.getPhoto());
    }

    @SneakyThrows
    public User convertUserUpdateDtoToUser(UserUpdateDto userUpdateDto,
                                           User user, MultipartFile file) {
        byte[] photo = file.getBytes();
        user.setFullName(userUpdateDto.getFullName());
        user.setPhoto(photo.length != 0 ? photo : user.getPhoto());
        return user;
    }
}
