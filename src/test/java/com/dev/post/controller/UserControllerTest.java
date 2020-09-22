package com.dev.post.controller;

import com.dev.post.model.User;
import com.dev.post.model.dto.UserResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.NestedServletException;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserControllerTest {
    private static final String GET_USERS_ENDPOINT = "/api/users";
    private static final String USER_SETTINGS_ENDPOINT = "/user/settings";
    private static final String REDIRECT_TO_MAIL_BOX = "redirect:/mail-box";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    @Test
    @WithMockUser(username = "nick", password = "1234", roles = {"USER", "APICALL"})
    public void checkGetUsersIsOk() {
        UserResponseDto nick = new UserResponseDto();
        nick.setLogin("nick");
        nick.setFullName("Arkhanhelskyi Mykyta Dmytrovych");
        UserResponseDto john = new UserResponseDto();
        john.setLogin("john");
        UserResponseDto mary = new UserResponseDto();
        mary.setLogin("mary");
        List<UserResponseDto> expectedUsers = new ArrayList<>();
        expectedUsers.add(nick);
        expectedUsers.add(john);
        expectedUsers.add(mary);
        MvcResult result = getResulActions(GET_USERS_ENDPOINT,
                MockMvcResultMatchers.status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        List<UserResponseDto> actualUsers = objectMapper.readValue(content,
                new TypeReference<List<UserResponseDto>>() {
                });
        Assertions.assertEquals(3, actualUsers.size());
        Assertions.assertEquals(expectedUsers, actualUsers);
    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "nick", password = "1234")
    public void checkGetUsersWithoutNeededRole() {
        getResulActions(GET_USERS_ENDPOINT, MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "nick", password = "1234")
    public void checkGetUserSettingsPageIsOk() throws Exception {
        MvcResult result = getResulActions(USER_SETTINGS_ENDPOINT,
                MockMvcResultMatchers.status().isOk())
                .andReturn();
        ModelAndView modelAndView = result.getModelAndView();
        Map<String, Object> model = modelAndView.getModel();
        Assertions.assertEquals("settings", modelAndView.getViewName());
        Assertions.assertEquals(true, model.get("apiRole"));
        User user = (User) model.get("user");
        Assertions.assertEquals("nick", user.getLogin());
        Assertions.assertEquals("Arkhanhelskyi Mykyta Dmytrovych", user.getFullName());
    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "john", password = "1234")
    public void checkUpdateUserAccountIsOk() {
        MvcResult result = getUpdateUserAccountResult("on");
        Assertions.assertEquals(REDIRECT_TO_MAIL_BOX,
                result.getModelAndView().getViewName());
    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "nick", password = "1234")
    public void checkUpdateUserAccountToRemoveApiRoleIsOk() {
        MvcResult result = getUpdateUserAccountResult("");
        Assertions.assertEquals(REDIRECT_TO_MAIL_BOX,
                result.getModelAndView().getViewName());
    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "john", password = "1234")
    public void checkUpdateUserWithNullMultipart() {
        Assertions.assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(MockMvcRequestBuilders
                    .multipart(USER_SETTINGS_ENDPOINT)
                    .param("fullName", "full name")
                    .param("apiRole", "on")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON));
        });
    }

    @SneakyThrows
    private ResultActions getResulActions(String getUsersEndpoint, ResultMatcher status) {
        return mockMvc.perform(MockMvcRequestBuilders
                .get(getUsersEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status);
    }

    @SneakyThrows
    private MvcResult getUpdateUserAccountResult(String apiRole) {
        return mockMvc.perform(MockMvcRequestBuilders
                .multipart(USER_SETTINGS_ENDPOINT)
                .file("photo", new byte[1000])
                .param("fullName", "full name")
                .param("apiRole", apiRole)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andReturn();
    }
}
