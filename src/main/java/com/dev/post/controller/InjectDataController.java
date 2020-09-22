package com.dev.post.controller;

import com.algolia.search.SearchIndex;
import com.dev.post.mapper.MailMapper;
import com.dev.post.model.Mail;
import com.dev.post.model.User;
import com.dev.post.model.dto.MailSearchIndexDto;
import com.dev.post.service.MailService;
import com.dev.post.service.UserService;
import com.dev.post.singleton.SearchIndexSingleton;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class InjectDataController {
    private final MailMapper mailMapper;
    private final MailService mailService;
    private final UserService userService;
    private final SearchIndexSingleton searchIndexSingleton;

    @PostConstruct
    public void init() {
        injectMails();
        injectDataToIndex();
    }

    private void injectDataToIndex() {
        Mail firstMail = mailService.findMailById(1L);
        User firstMailSender = userService.findById(firstMail.getSenderId());
        Mail secondMail = mailService.findMailById(2L);
        User secondMailSender = userService.findById(secondMail.getSenderId());
        SearchIndex<MailSearchIndexDto> searchIndex = searchIndexSingleton.getSearchIndex();
        searchIndex.clearObjects();
        searchIndex.saveObject(mailMapper
                .convertMailToMailSearchIndexDto(firstMail, firstMailSender));
        searchIndex.saveObject(mailMapper
                .convertMailToMailSearchIndexDto(secondMail, secondMailSender));
    }

    private void injectMails() {
        Mail firstMail = new Mail();
        firstMail.setSubject("Theme");
        firstMail.setText("Text");
        firstMail.setSenderId(1L);
        firstMail.setDate(LocalDateTime.parse("2019-09-18T17:44:48.115522"));
        List<User> firstMailRecipients = new ArrayList<>();
        firstMailRecipients.add(userService.findById(2L));
        firstMailRecipients.add(userService.findById(3L));
        firstMail.setRecipients(firstMailRecipients);
        mailService.createMail(firstMail);
        Mail secondMail = new Mail();
        secondMail.setSubject("New theme");
        secondMail.setText("Second text");
        secondMail.setSenderId(2L);
        secondMail.setDate(LocalDateTime.parse("2020-09-17T17:44:48.115522"));
        List<User> secondMailRecipients = new ArrayList<>();
        secondMailRecipients.add(userService.findById(1L));
        secondMail.setRecipients(secondMailRecipients);
        mailService.createMail(secondMail);
    }
}
