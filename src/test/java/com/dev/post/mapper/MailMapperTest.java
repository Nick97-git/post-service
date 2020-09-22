package com.dev.post.mapper;

import com.dev.post.model.Mail;
import com.dev.post.model.User;
import com.dev.post.model.dto.MailCreateDto;
import com.dev.post.model.dto.MailResponseDto;
import com.dev.post.model.dto.MailSearchIndexDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MailMapperTest {
    private static final DateTimeFormatter DIFFERENT_YEARS = DateTimeFormatter
            .ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter CURRENT_DAY = DateTimeFormatter.ofPattern("HH:mm");
    private MailMapper mailMapper;
    private List<User> recipients;
    private Mail mail;

    @BeforeEach
    public void setUp() {
        mailMapper = new MailMapper();
        setUpRecipients();
        setUpMail();
    }

    private void setUpMail() {
        mail = new Mail();
        mail.setId(1L);
        mail.setSubject("subject");
        mail.setText("text");
    }

    private void setUpRecipients() {
        User john = new User();
        john.setLogin("john");
        User mary = new User();
        mary.setLogin("mary");
        recipients = new ArrayList<>();
        recipients.add(john);
        recipients.add(mary);
    }

    @Test
    public void checkConvertMailToMailResponseDtoIsOk() {
        mail.setDate(LocalDateTime.of(2019, 12, 12, 12, 12));
        mail.setRecipients(recipients);
        User user = new User();
        user.setLogin("login");
        MailResponseDto expected = new MailResponseDto();
        expected.setId(mail.getId());
        expected.setText(mail.getText());
        expected.setSubject(mail.getSubject());
        expected.setRecipients("john,mary");
        expected.setSender(user.getLogin());
        expected.setDate(mail.getDate().format(DIFFERENT_YEARS));
        MailResponseDto actual = mailMapper.convertMailToMailResponseDto(mail, user);
        Assertions.assertEquals(expected, actual);
        mail.setDate(mail.getDate().plusYears(1));
        actual = mailMapper.convertMailToMailResponseDto(mail, user);
        expected.setDate("12 December");
        Assertions.assertEquals(expected, actual);
        mail.setDate(LocalDateTime.now());
        actual = mailMapper.convertMailToMailResponseDto(mail, user);
        expected.setDate(mail.getDate().format(CURRENT_DAY));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void checkConvertMailToMailResponseDtoWithNullData() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            mailMapper.convertMailToMailResponseDto(null, null);
        });
    }

    @Test
    public void checkConvertMailCreateDtoToMailIsOk() {
        MailCreateDto mailCreateDto = new MailCreateDto();
        mailCreateDto.setText("Text");
        mailCreateDto.setSubject("Subject");
        Mail expected = new Mail();
        expected.setText(mailCreateDto.getText());
        expected.setSubject(mailCreateDto.getSubject());
        expected.setSenderId(1L);
        Mail actual = mailMapper.convertMailCreateDtoToMail(mailCreateDto, 1L);
        Assertions.assertEquals(expected.getSenderId(), actual.getSenderId());
        Assertions.assertEquals(expected.getText(), actual.getText());
        Assertions.assertEquals(expected.getSubject(), actual.getSubject());
    }

    @Test
    public void checkConvertMailCreateDtoToMailWithNullData() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            mailMapper.convertMailCreateDtoToMail(null, 1L);
        });
    }

    @Test
    public void checkConvertRecipientsToStringIsOk() {
        String expected = "john,mary";
        String actual = mailMapper.convertRecipientsToString(recipients);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void checkConvertRecipientsToStringWithNullData() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            mailMapper.convertRecipientsToString(null);
        });
    }

    @Test
    public void getNameWithExistentFullName() {
        User user = new User();
        user.setLogin("login");
        user.setFullName("full_name");
        String expected = user.getFullName();
        String actual = mailMapper.getName(user);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void checkConvertMailToMailSearchIndexDto() {
        recipients.get(1).setFullName("Mary Benet");
        mail.setRecipients(recipients);
        User sender = new User();
        sender.setLogin("nick");
        sender.setFullName("Arkhanhelskyi Mykyta Dmytrovych");
        MailSearchIndexDto expected = new MailSearchIndexDto();
        expected.setObjectID(mail.getId());
        expected.setText(mail.getText());
        expected.setSubject(mail.getSubject());
        expected.setSenderNames("nick,Arkhanhelskyi Mykyta Dmytrovych");
        expected.setRecipientsNames("john,mary,Mary Benet");
        MailSearchIndexDto actual = mailMapper.convertMailToMailSearchIndexDto(mail, sender);
        Assertions.assertEquals(expected, actual);
    }
}
