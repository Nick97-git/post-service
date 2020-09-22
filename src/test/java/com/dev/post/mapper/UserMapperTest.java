package com.dev.post.mapper;

import com.dev.post.model.User;
import com.dev.post.model.dto.UserResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserMapperTest {
    private UserMapper userMapper;

    @BeforeEach
    public void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    public void checkConvertUserToUserResponseDtoIsOk() {
        User user = new User();
        user.setLogin("login");
        user.setFullName("full_name");
        UserResponseDto expected = new UserResponseDto();
        expected.setLogin(user.getLogin());
        expected.setFullName(user.getFullName());
        UserResponseDto actual = userMapper.convertUserToUserResponseDto(user);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void checkConvertUserToUserResponseDtoWithNullUser() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            userMapper.convertUserToUserResponseDto(null);
        });
    }

    @Test
    public void checkGetPhotoWithNotNullPhoto() {
        User user = new User();
        user.setPhoto(new byte[1000]);
        Assertions.assertNotNull(userMapper.getPhoto(user));
    }
}
