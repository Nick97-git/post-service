package com.dev.post.service.impl;

import com.dev.post.model.Mail;
import com.dev.post.model.Role;
import com.dev.post.model.User;
import com.dev.post.repository.UserRepository;
import com.dev.post.service.UserService;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public User update(User user, String apiRole) {
        return userRepository.update(user, apiRole, hasApiRole(user));
    }

    @Override
    public boolean hasApiRole(User user) {
        for (Role role : user.getRoles()) {
            if (role.getRoleName().equals(Role.RoleName.APICALL)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<User> getRecipients(String recipients) {
        String[] logins = recipients.split(",");
        return Arrays.stream(logins)
                .map(this::findByLogin)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasMail(User user, Mail mail) {
        for (User recipient : mail.getRecipients()) {
            if (isOwner(user, recipient)) {
                return true;
            }
        }
        User sender = findById(mail.getSenderId());
        return isOwner(user, sender);
    }

    private boolean isOwner(User user, User recipient) {
        return recipient.getLogin().equals(user.getLogin())
                || (!Objects.isNull(user.getFullName())
                && user.getFullName().equals(recipient.getFullName()));
    }
}
