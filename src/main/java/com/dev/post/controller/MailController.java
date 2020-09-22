package com.dev.post.controller;

import com.dev.post.exception.NoSuchRecipientException;
import com.dev.post.mapper.MailMapper;
import com.dev.post.model.Mail;
import com.dev.post.model.User;
import com.dev.post.model.dto.MailAnswerDto;
import com.dev.post.model.dto.MailCreateDto;
import com.dev.post.model.dto.MailDeleteDto;
import com.dev.post.model.dto.MailResponseDto;
import com.dev.post.model.dto.MailSearchDto;
import com.dev.post.service.MailService;
import com.dev.post.service.SearchService;
import com.dev.post.service.UserService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
@AllArgsConstructor
public class MailController {
    private static final String MAIL_INFO_PAGE_VIEW_NAME = "mail_info";
    private static final String NEW_MAIL_PAGE_VIEW_NAME = "new_mail";
    private static final String REDIRECT_TO_MAIL_BOX_PAGE = "redirect:/mail-box";
    private static final String REDIRECT_TO_MAIL_PAGE = "redirect:/mail";
    private static final String NON_EXISTENT_RECIPIENT_ERROR_MESSAGE =
            "Non-existent recipient has been written!";
    private static final String RECIPIENTS_ERROR_MESSAGE = "Recipients can't be null or blank!";
    private final MailMapper mailMapper;
    private final MailService mailService;
    private final SearchService searchService;
    private final UserService userService;

    @GetMapping("/api/mails")
    @ResponseBody
    public List<MailResponseDto> getMail() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByLogin(login);
        List<MailResponseDto> mails = mailService.findUserMails(user).stream()
                .map(mail -> mailMapper.convertMailToMailResponseDto(mail,
                        userService.findById(mail.getSenderId())))
                .collect(Collectors.toList());
        log.info("User " + user.getLogin() + " has got list of mails");
        return mails;
    }

    @PostMapping("/api/mails")
    @ResponseBody
    public List<MailResponseDto> getMailsBySearchText(@RequestBody MailSearchDto mailSearchDto) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        List<MailResponseDto> mails = searchService.findMails(mailSearchDto.getSearchText(), login)
                .stream()
                .map(mailSearchIndexDto -> mailService
                        .findMailById(mailSearchIndexDto.getObjectID()))
                .map(mail -> mailMapper.convertMailToMailResponseDto(mail,
                        userService.findById(mail.getSenderId())))
                .collect(Collectors.toList());
        log.info("User " + login + " has got mails found by search text: "
                + mailSearchDto.getSearchText());
        return mails;
    }

    @DeleteMapping("/api/mail/{mailId}")
    @ResponseBody
    public void deleteMail(@PathVariable("mailId") String mailId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByLogin(login);
        mailService.deleteMailById(mailId, user);
        log.info("Mail with id: " + mailId + " has been deleted for user " + user.getLogin());
        if (mailService.hasNoReference(mailId)) {
            searchService.delete(mailId);
            log.info("Mail with id: " + mailId + " has been deleted from database");
        }
    }

    @PostMapping("/api/mail/{mailId}")
    @ResponseBody
    public MailResponseDto answerMailApi(@PathVariable("mailId") String mailId,
                                         @RequestBody MailAnswerDto mailAnswerDto) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByLogin(login);
        Mail mail = mailService.findMailById(Long.parseLong(mailId));
        User mailAuthor = userService.findById(mail.getId());
        Mail mailForAnswer = mailMapper.convertToMailForAnswer(mail, mailAnswerDto,
                user.getId(), mailAuthor);
        Mail createdMail = mailService.createMail(mailForAnswer);
        searchService.save(mailMapper.convertMailToMailSearchIndexDto(createdMail, user));
        log.info("User " + user.getLogin() + " has answered for mail with id " + mailId);
        return mailMapper.convertMailToMailResponseDto(mailForAnswer, user);
    }

    @SneakyThrows
    @PostMapping("/api/mail")
    @ResponseBody
    public MailResponseDto createMailApi(@RequestBody @Valid MailCreateDto mailCreateDto) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByLogin(login);
        Mail mail = mailMapper.convertMailCreateDtoToMail(mailCreateDto, user.getId());
        mail.setRecipients(userService.getRecipients(mailCreateDto.getRecipients()));
        if (mail.getRecipients().contains(null)) {
            throw new NoSuchRecipientException(NON_EXISTENT_RECIPIENT_ERROR_MESSAGE);
        }
        Mail createdMail = mailService.createMail(mail);
        searchService.save(mailMapper.convertMailToMailSearchIndexDto(createdMail, user));
        log.info("User " + user.getLogin() + " has written mail for "
                + mailCreateDto.getRecipients());
        return mailMapper.convertMailToMailResponseDto(createdMail, user);
    }

    @GetMapping("/mail/info/{mailId}")
    public ModelAndView getMailInfo(@PathVariable("mailId") String mailId,
                                    ModelAndView modelAndView) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Mail mail = mailService.findMailById(Long.valueOf(mailId));
        User user = userService.findByLogin(login);
        modelAndView.setViewName(MAIL_INFO_PAGE_VIEW_NAME);
        modelAndView.addObject("mail", mail);
        modelAndView.addObject("user_id", user.getId());
        modelAndView.addObject("recipients", mailMapper
                .convertRecipientsToString(mail.getRecipients()));
        modelAndView.addObject("sender", mailMapper
                .getName(userService.findById(mail.getSenderId())));
        log.info("User " + user.getLogin() + " has got info of mail with id: " + mailId);
        return modelAndView;
    }

    @PostMapping("/mails")
    public String deleteMails(MailDeleteDto mailDeleteDto) {
        if (Objects.isNull(mailDeleteDto.getIds())) {
            return REDIRECT_TO_MAIL_BOX_PAGE;
        }
        for (String mailId: mailDeleteDto.getIds()) {
            deleteMail(mailId);
        }
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User " + login + " has deleted mails with ids: "
                + String.join(",", mailDeleteDto.getIds()));
        return REDIRECT_TO_MAIL_BOX_PAGE;
    }

    @GetMapping("/mail")
    public String getCreateMailPage(MailCreateDto mailCreateDto) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User " + login + " has got create mail page");
        return "new_mail";
    }

    @GetMapping("/mail/{mailId}")
    public ModelAndView getAnswerMailPage(@PathVariable("mailId") String mailId,
                                          MailCreateDto mailCreateDto,
                                       ModelAndView modelAndView) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Mail mail = mailService.findMailById(Long.parseLong(mailId));
        User sender = userService.findById(mail.getSenderId());
        User currentUser = userService.findByLogin(login);
        modelAndView.setViewName(NEW_MAIL_PAGE_VIEW_NAME);
        mailCreateDto.setSubject(mail.getSubject());
        mailCreateDto.setRecipients(mailMapper
                .getRecipients(mailMapper.convertRecipientsToString(mail.getRecipients()),
                        sender, currentUser));
        modelAndView.addObject("mailCreateDto", mailCreateDto);
        log.info("User " + currentUser.getLogin() + " has got page for answer mail with id "
                + mailId);
        return modelAndView;
    }

    @PostMapping("/mail")
    public String createMail(@Valid @ModelAttribute("mailCreateDto") MailCreateDto mailCreateDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.error(RECIPIENTS_ERROR_MESSAGE);
            return NEW_MAIL_PAGE_VIEW_NAME;
        }
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByLogin(login);
        Mail mail = mailMapper.convertMailCreateDtoToMail(mailCreateDto, user.getId());
        mail.setRecipients(userService.getRecipients(mailCreateDto.getRecipients()));
        if (mail.getRecipients().contains(null)) {
            redirectAttributes.addFlashAttribute("error", NON_EXISTENT_RECIPIENT_ERROR_MESSAGE);
            log.error(NON_EXISTENT_RECIPIENT_ERROR_MESSAGE);
            return REDIRECT_TO_MAIL_PAGE;
        }
        Mail createdMail = mailService.createMail(mail);
        searchService.save(mailMapper.convertMailToMailSearchIndexDto(createdMail, user));
        return REDIRECT_TO_MAIL_BOX_PAGE;
    }
}
