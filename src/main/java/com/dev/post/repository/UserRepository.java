package com.dev.post.repository;

import com.dev.post.model.User;
import java.util.List;

public interface UserRepository {

    User findByLogin(String login);

    List<User> findAll();

    User findById(Long userId);

    User update(User user, String apiRole, boolean hasApiRole);
}
