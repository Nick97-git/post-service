package com.dev.post.repository;

import com.dev.post.model.Mail;
import com.dev.post.model.User;
import java.util.List;

public interface MailRepository {

    List<Mail> findUserMails(User user);

    void deleteMailById(String mailId, User user);

    Mail createMail(Mail mail);

    Mail findMailById(Long mailId);

    Integer getNumOfReferences(String mailId);

    Integer getNumOfReferences(Long mailId, Long userId);

    void deleteMailFully(String mailId);
}
