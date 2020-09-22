package com.dev.post.mapper;

import com.dev.post.model.Mail;
import com.dev.post.model.User;
import com.dev.post.model.dto.MailAnswerDto;
import com.dev.post.model.dto.MailCreateDto;
import com.dev.post.model.dto.MailResponseDto;
import com.dev.post.model.dto.MailSearchIndexDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MailMapper {
    private static final DateTimeFormatter DIFFERENT_YEARS = DateTimeFormatter
            .ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter CURRENT_DAY = DateTimeFormatter.ofPattern("HH:mm");
    private static final String DELIMITER = ",";

    public MailResponseDto convertMailToMailResponseDto(Mail mail, User sender) {
        MailResponseDto mailResponseDto = new MailResponseDto();
        mailResponseDto.setId(mail.getId());
        mailResponseDto.setDate(convertDate(mail.getDate()));
        mailResponseDto.setText(mail.getText());
        mailResponseDto.setSubject(mail.getSubject());
        mailResponseDto.setSender(getName(sender));
        mailResponseDto.setRecipients(convertRecipientsToString(mail.getRecipients()));
        return mailResponseDto;
    }

    public Mail convertMailCreateDtoToMail(MailCreateDto mailCreateDto, Long userId) {
        Mail mail = new Mail();
        mail.setText(mailCreateDto.getText());
        mail.setSubject(mailCreateDto.getSubject());
        mail.setSenderId(userId);
        mail.setDate(LocalDateTime.now());
        return mail;
    }

    public Mail convertToMailForAnswer(Mail mail, MailAnswerDto mailAnswerDto,
                                       Long userId, User mailAuthor) {
        Mail mailForAnswer = new Mail();
        mailForAnswer.setSubject(mail.getSubject());
        mailForAnswer.setText(mailAnswerDto.getText());
        mailForAnswer.setDate(LocalDateTime.now());
        mailForAnswer.setSenderId(userId);
        mailForAnswer.setRecipients(getRecipients(mail, userId, mailAuthor));
        return mailForAnswer;
    }

    public MailSearchIndexDto convertMailToMailSearchIndexDto(Mail mail, User sender) {
        MailSearchIndexDto mailSearchIndexDto = new MailSearchIndexDto();
        mailSearchIndexDto.setObjectID(mail.getId());
        mailSearchIndexDto.setSubject(mail.getSubject());
        mailSearchIndexDto.setText(mail.getText());
        mailSearchIndexDto.setSenderNames(getNames(sender));
        mailSearchIndexDto.setRecipientsNames(mail.getRecipients().stream()
                .map(this::getNames)
                .collect(Collectors.joining(DELIMITER)));
        return mailSearchIndexDto;
    }

    public String convertRecipientsToString(List<User> recipients) {
        return recipients.stream()
                .map(this::getName)
                .collect(Collectors.joining(DELIMITER));
    }

    public String getName(User sender) {
        return !Objects.isNull(sender.getFullName())
                ? sender.getFullName()
                : sender.getLogin();
    }

    private String convertDate(LocalDateTime mailDate) {
        LocalDateTime nowDate = LocalDateTime.now();
        if (mailDate.getYear() == nowDate.getYear()
                && mailDate.getMonth().equals(nowDate.getMonth())
                && mailDate.getDayOfMonth() == nowDate.getDayOfMonth()) {
            return mailDate.format(CURRENT_DAY);
        }
        if (mailDate.getYear() != nowDate.getYear()) {
            return mailDate.format(DIFFERENT_YEARS);
        }
        return mailDate.getDayOfMonth() + " "
                + mailDate.getMonth().getDisplayName(TextStyle.FULL, Locale.US);
    }

    private String getNames(User user) {
        return Objects.isNull(user.getFullName())
                ? user.getLogin()
                : user.getLogin() + DELIMITER + user.getFullName();
    }

    private List<User> getRecipients(Mail mail, Long userId, User mailAuthor) {
        List<User> users = new ArrayList<>();
        users.add(mailAuthor);
        return mail.getSenderId().equals(userId) ? mail.getRecipients() : users;
    }

    public String getRecipients(String recipients, User sender, User currentUser) {
        return sender.getLogin().equals(currentUser.getLogin())
                || (!Objects.isNull(sender.getFullName())
                && sender.getFullName().equals(currentUser.getFullName()))
                ? recipients
                : sender.getLogin();
    }
}
