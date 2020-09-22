package com.dev.post.service;

import com.dev.post.model.Mail;
import com.dev.post.model.User;
import java.util.List;

public interface MailService {

    List<Mail> findUserMails(User user);

    void deleteMailById(String mailId, User user);

    Mail createMail(Mail mail);

    Mail findMailById(Long mailId);

    boolean hasReference(Long mailId, String login);

    boolean hasNoReference(String mailId);
}
