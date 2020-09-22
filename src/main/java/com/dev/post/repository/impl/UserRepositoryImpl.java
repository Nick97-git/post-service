package com.dev.post.repository.impl;

import com.dev.post.mapper.row.UserRowMapper;
import com.dev.post.model.Role;
import com.dev.post.model.User;
import com.dev.post.repository.UserRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private static final String DELETE_USER_ROLE = "delete from users_roles "
            + "where user_id = ? and role_id = ?;";
    private static final String SELECT_ALL_USERS = "select * from users;";
    private static final String SELECT_ROLE_ID_BY_ROLE_NAME = "select id from roles "
            + "where role_name = ?;";
    private static final String SELECT_ROLES_BY_USER_ID = "select roles.id, roles.role_name "
            + "from users_roles inner join roles on users_roles.role_id=roles.id"
            + " where users_roles.user_id = ?;";
    private static final String SELECT_USER_BY_ID = "select * from users where id = ?";
    private static final String SELECT_USER_BY_LOGIN = "select * from users where login = ?";
    private static final String UPDATE_USER_INFO = "update users set full_name = ?, photo = ? "
            + "where id = ?;";
    private static final String UPDATE_USER_ROLES = "insert into users_roles(user_id, role_id) "
            + "values (?, ?);";
    private static final String ROLE_ID_COLUMN_NAME = "id";
    private static final String ROLE_NAME_COLUMN_NAME = "role_name";
    private static final String API_ROLE_TURNED_ON = "on";
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User findByLogin(String login) {
        try {
            User user = jdbcTemplate.queryForObject(SELECT_USER_BY_LOGIN,
                    new Object[]{login}, userRowMapper);
            user.setRoles((getRoles(user.getId())));
            return user;
        } catch (DataAccessException e) {
            return null;
        }
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query(SELECT_ALL_USERS, userRowMapper);
    }

    @Override
    public User findById(Long userId) {
        return jdbcTemplate.queryForObject(SELECT_USER_BY_ID,
                new Object[]{userId}, userRowMapper);
    }

    @Override
    public User update(User user, String apiRole, boolean hasApiRole) {
        jdbcTemplate.update(UPDATE_USER_INFO, user.getFullName(), user.getPhoto(), user.getId());
        Long roleId = getRoleId();
        if (!API_ROLE_TURNED_ON.equals(apiRole) && hasApiRole) {
            deleteUserRole(user, roleId);
        } else if (API_ROLE_TURNED_ON.equals(apiRole) && !hasApiRole) {
            updateUserRoles(user, roleId);
        }
        return user;
    }

    private Long getRoleId() {
        return jdbcTemplate.queryForObject(SELECT_ROLE_ID_BY_ROLE_NAME,
                new Object[]{Role.RoleName.APICALL.name()},
                (resultSet, rowNum) -> resultSet.getLong(ROLE_ID_COLUMN_NAME));
    }

    private void deleteUserRole(User user, Long roleId) {
        jdbcTemplate.update(DELETE_USER_ROLE, user.getId(), roleId);
        user.getRoles().removeIf(role -> role.getId().equals(roleId));
    }

    private void updateUserRoles(User user, Long roleId) {
        jdbcTemplate.update(UPDATE_USER_ROLES, user.getId(), roleId);
        Role apiRole = new Role();
        apiRole.setId(roleId);
        apiRole.setRoleName(Role.RoleName.APICALL);
        user.getRoles().add(apiRole);
    }

    private List<Role> getRoles(Long id) {
        return jdbcTemplate.query(SELECT_ROLES_BY_USER_ID, new Object[]{id},
                (resultSet, rowNum) -> {
                    Role role = new Role();
                    role.setId(resultSet.getLong(ROLE_ID_COLUMN_NAME));
                    role.setRoleName(Role.RoleName
                            .valueOf(resultSet.getString(ROLE_NAME_COLUMN_NAME)));
                    return role;
                });
    }
}
