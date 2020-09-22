package com.dev.post.controller;

import com.dev.post.mapper.MailMapper;
import com.dev.post.mapper.UserMapper;
import com.dev.post.model.User;
import com.dev.post.model.dto.MailResponseDto;
import com.dev.post.model.dto.MailSearchDto;
import com.dev.post.model.dto.MailSearchIndexDto;
import com.dev.post.service.MailService;
import com.dev.post.service.SearchService;
import com.dev.post.service.UserService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Slf4j
@AllArgsConstructor
public class MainController {
    private static final String MAIL_BOX_PAGE_VIEW_NAME = "mail-box";
    private final MailMapper mailMapper;
    private final MailService mailService;
    private final SearchService searchService;
    private final UserMapper userMapper;
    private final UserService userService;

    @RequestMapping(value = "/mail-box", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView getMainPage(ModelAndView modelAndView) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByLogin(login);
        List<MailResponseDto> mails = mailService.findUserMails(user).stream()
                .map(mail -> mailMapper.convertMailToMailResponseDto(mail,
                        userService.findById(mail.getSenderId())))
                .collect(Collectors.toList());
        log.info("User " + user.getLogin() + " opened mail box page");
        return getModelAndView(modelAndView, user, mails);
    }

    @RequestMapping(value = "/mail-box/search", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView getMailBoxWithMailsFoundBySearchText(MailSearchDto mailSearchDto,
                                                    ModelAndView modelAndView) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByLogin(login);
        List<MailSearchIndexDto> mailSearchIndexDtos = searchService
                .findMails(mailSearchDto.getSearchText(), login);
        List<MailResponseDto> mails = mailSearchIndexDtos.stream()
                .map(mailSearchIndexDto -> mailService
                        .findMailById(mailSearchIndexDto.getObjectID()))
                .map(mail -> mailMapper.convertMailToMailResponseDto(mail,
                        userService.findById(mail.getSenderId())))
                .collect(Collectors.toList());
        log.info("User " + user.getLogin() + " has got mails found by search text: "
                + mailSearchDto.getSearchText());
        return getModelAndView(modelAndView, user, mails);
    }

    private ModelAndView getModelAndView(ModelAndView modelAndView, User user,
                                         List<MailResponseDto> mails) {
        modelAndView.setViewName(MAIL_BOX_PAGE_VIEW_NAME);
        modelAndView.addObject("photo", userMapper.getPhoto(user));
        modelAndView.addObject("user", user);
        modelAndView.addObject("mails", mails);
        return modelAndView;
    }
}
