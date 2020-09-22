package com.dev.post.controller;

import com.dev.post.mapper.UserMapper;
import com.dev.post.model.User;
import com.dev.post.model.dto.UserResponseDto;
import com.dev.post.model.dto.UserUpdateDto;
import com.dev.post.service.UserService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Slf4j
@AllArgsConstructor
public class UserController {
    private static final String REDIRECT_TO_MAIL_BOX_PAGE = "redirect:/mail-box";
    private static final String SETTINGS_PAGE_VIEW_NAME = "settings";
    private final UserMapper userMapper;
    private final UserService userService;

    @GetMapping("/api/users")
    @ResponseBody
    public List<UserResponseDto> getUser() {
        List<UserResponseDto> users = userService.findAll().stream()
                .map(userMapper::convertUserToUserResponseDto)
                .collect(Collectors.toList());
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User " + login + " has got list of registered users");
        return users;
    }

    @GetMapping("/user/settings")
    public ModelAndView getSettingsPage(ModelAndView modelAndView) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByLogin(login);
        modelAndView.setViewName(SETTINGS_PAGE_VIEW_NAME);
        modelAndView.addObject("user", user);
        modelAndView.addObject("apiRole", userService.hasApiRole(user));
        log.info("User " + login + " has got settings page");
        return modelAndView;
    }

    @SneakyThrows
    @PostMapping("/user/settings")
    public String updateSettings(@RequestParam(value = "photo", required = false)
                                             MultipartFile file,
                                UserUpdateDto userUpdateDto) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByLogin(login);
        User updatedUser = userMapper.convertUserUpdateDtoToUser(userUpdateDto, user, file);
        userService.update(updatedUser, userUpdateDto.getApiRole());
        log.info("User " + login + " has updated his settings");
        return REDIRECT_TO_MAIL_BOX_PAGE;
    }
}
