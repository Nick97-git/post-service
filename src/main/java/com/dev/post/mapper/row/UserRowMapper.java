package com.dev.post.mapper.row;

import com.dev.post.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class UserRowMapper implements RowMapper<User> {
    private static final String ID_COLUMN_NAME = "id";
    private static final String LOGIN_COLUMN_NAME = "login";
    private static final String PASSWORD_COLUMN_NAME = "password";
    private static final String PHOTO_COLUMN_NAME = "photo";
    private static final String FULL_NAME_COLUMN_NAME = "full_name";

    @Override
    public User mapRow(ResultSet resultSet, int i) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong(ID_COLUMN_NAME));
        user.setLogin(resultSet.getString(LOGIN_COLUMN_NAME));
        user.setPassword(resultSet.getString(PASSWORD_COLUMN_NAME));
        user.setPhoto(resultSet.getBytes(PHOTO_COLUMN_NAME));
        user.setFullName(resultSet.getString(FULL_NAME_COLUMN_NAME));
        return user;
    }
}
