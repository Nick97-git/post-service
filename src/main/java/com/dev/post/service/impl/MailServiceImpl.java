package com.dev.post.service.impl;

import com.dev.post.exception.DeleteMailException;
import com.dev.post.model.Mail;
import com.dev.post.model.User;
import com.dev.post.repository.MailRepository;
import com.dev.post.service.MailService;
import com.dev.post.service.UserService;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MailServiceImpl implements MailService {
    private final MailRepository mailRepository;
    private final UserService userService;

    @Override
    public List<Mail> findUserMails(User user) {
        return mailRepository.findUserMails(user);
    }

    @SneakyThrows
    @Override
    public void deleteMailById(String mailId, User user) {
        Mail mail = findMailById(Long.valueOf(mailId));
        if (Objects.isNull(mail)) {
            throw new DeleteMailException("Mail with id: " + mailId
                    + " doesn't exists");
        }
        if (!userService.hasMail(user, mail)) {
            throw new DeleteMailException("You don't have mail with id: "
                    + mailId + " in your mail box");
        }
        mailRepository.deleteMailById(mailId, user);
        if (hasNoReference(mailId)) {
            mailRepository.deleteMailFully(mailId);
        }
    }

    @Override
    public Mail createMail(Mail mail) {
        return mailRepository.createMail(mail);
    }

    @Override
    public Mail findMailById(Long mailId) {
        return mailRepository.findMailById(mailId);
    }

    @Override
    public boolean hasReference(Long mailId, String login) {
        Integer numOfReferences = mailRepository.getNumOfReferences(mailId,
                userService.findByLogin(login).getId());
        return !Objects.isNull(numOfReferences) && numOfReferences != 0;
    }

    @Override
    public boolean hasNoReference(String mailId) {
        Mail mail = findMailById(Long.valueOf(mailId));
        if (mail == null) {
            return true;
        }
        Integer maxPossibleReferences = mail.getRecipients().size() + 1;
        Integer currentReferences = mailRepository.getNumOfReferences(mailId);
        return maxPossibleReferences.equals(currentReferences);
    }
}
