package com.dev.post.service;

import com.dev.post.model.Mail;
import com.dev.post.model.User;
import java.util.List;

public interface UserService {

    User findByLogin(String login);

    List<User> findAll();

    User findById(Long userId);

    User update(User user, String apiRole);

    boolean hasApiRole(User user);

    List<User> getRecipients(String recipients);

    boolean hasMail(User user, Mail mail);
}
