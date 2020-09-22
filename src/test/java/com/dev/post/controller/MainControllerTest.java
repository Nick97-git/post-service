package com.dev.post.controller;

import com.dev.post.model.User;
import com.dev.post.model.dto.MailResponseDto;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class MainControllerTest {
    private static final String MAIL_BOX_ENDPOINT = "/mail-box";
    private static final String MAIL_BOX_SEARCH_ENDPOINT = "/mail-box/search";
    @Autowired
    private MockMvc mockMvc;

    @SneakyThrows
    @Test
    @WithMockUser(username = "nick", password = "1234")
    public void getMainPageIsOk() {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .get(MAIL_BOX_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        Map<String, Object> model = result.getModelAndView().getModel();
        String expected = "mail-box";
        String actual = result.getModelAndView().getViewName();
        Assertions.assertEquals(expected, actual);
        User user = (User) model.get("user");
        Assertions.assertEquals("nick", user.getLogin());
        Assertions.assertEquals("Arkhanhelskyi Mykyta Dmytrovych",
                user.getFullName());
        List<MailResponseDto> mails = (List<MailResponseDto>) model.get("mails");
        Assertions.assertEquals("New theme", mails.get(0).getSubject());
        Assertions.assertEquals("Theme", mails.get(1).getSubject());
    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "nick", password = "1234")
    public void getMailBySearchTextIsOk() {
        testGetMailBySearchText("Second", 1);
        testGetMailBySearchText("nick", 2);
        testGetMailBySearchText("subject", 0);
    }

    private void testGetMailBySearchText(String searchText, int size) throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post(MAIL_BOX_SEARCH_ENDPOINT)
                .param("searchText", searchText)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        List<MailResponseDto> mails = (List<MailResponseDto>) result.getModelAndView()
                .getModel().get("mails");
        Assertions.assertEquals(size, mails.size());
    }
}
