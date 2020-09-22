package com.dev.post.controller;

import com.dev.post.model.dto.MailAnswerDto;
import com.dev.post.model.dto.MailCreateDto;
import com.dev.post.model.dto.MailResponseDto;
import com.dev.post.model.dto.MailSearchDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MailControllerTest {
    private static final String CREATE_MAIL_API_ENDPOINT = "/api/mail";
    private static final String DELETE_MAIL_API_ENDPOINT = "/api/mail/{mailId}";
    private static final String CREATE_MAIL_ENDPOINT = "/mail";
    private static final String MAILS_API_ENDPOINT = "/api/mails";
    private static final String MAILS_ENDPOINT = "/mails";
    private static final String GET_ANSWER_MAIL_PAGE_ENDPOINT = "/mail/{mailId}";
    private static final String GET_MAIL_INFO_ENDPOINT = "/mail/info/{mailId}";
    private static final String ANSWER_MAIL_API_ENDPOINT = "/api/mail/{mailId}";
    private static final String REDIRECT_TO_MAIL_PAGE = "redirect:/mail";
    private static final String RECIPIENTS_ERROR_MESSAGE = "Recipients can't be null or blank!";
    private static final String SUBJECT_ERROR_MESSAGE = "Max length mustn't be "
            + "greater than 150 symbols!";
    private static final String TEXT_ERROR_MESSAGE = "Max length mustn't be "
            + "greater than 1024 symbols!";
    private static final String REDIRECT_TO_MAIL_BOX = "redirect:/mail-box";
    private static final String NON_EXISTENT_MAIL_ERROR_MESSAGE = "Mail with id: 0 doesn't exists";
    private static final String NON_EXISTENT_RECIPIENT_ERROR_MESSAGE =
            "Non-existent recipient has been written!";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private MailCreateDto mailCreateDto;

    @BeforeEach
    public void setUp() {
        setUpMailCreateDto();
    }

    private void setUpMailCreateDto() {
        mailCreateDto = new MailCreateDto();
        mailCreateDto.setText("text");
        mailCreateDto.setSubject("subject");
    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "nick", password = "1234", roles = {"APICALL"})
    public void checkCreateMailApiIsOk() {
        mailCreateDto.setRecipients("john,mary");
        String json = objectMapper.writeValueAsString(mailCreateDto);
        MvcResult result = getResult(json, MockMvcRequestBuilders
                .post(CREATE_MAIL_API_ENDPOINT), MockMvcResultMatchers.status().isOk());
        String content = result.getResponse().getContentAsString();
        MailResponseDto expected = new MailResponseDto();
        expected.setId(3L);
        expected.setRecipients(mailCreateDto.getRecipients());
        expected.setSender("Arkhanhelskyi Mykyta Dmytrovych");
        expected.setSubject(mailCreateDto.getSubject());
        expected.setText(mailCreateDto.getText());
        MailResponseDto actual = objectMapper.readValue(content, MailResponseDto.class);
        expected.setDate(actual.getDate());
        Assertions.assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "nick", password = "1234", roles = {"APICALL"})
    public void checkCreateMailApiWithNonExistentRecipient() {
        mailCreateDto.setRecipients("joh");
        String json = objectMapper.writeValueAsString(mailCreateDto);
        MvcResult result = getResult(json, MockMvcRequestBuilders
                .post(CREATE_MAIL_API_ENDPOINT), MockMvcResultMatchers.status().isBadRequest());
        String content = result.getResponse().getContentAsString();
        Map<String, Object> map = objectMapper.readValue(content,
                new TypeReference<HashMap<String, Object>>() {});
        Assertions.assertEquals(NON_EXISTENT_RECIPIENT_ERROR_MESSAGE, map.get("error"));
    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "nick", password = "1234", roles = "APICALL")
    public void checkCreateMailApiFieldErrors() {
        char[] textArr = new char[1025];
        char[] subjectArr = new char[151];
        Arrays.fill(subjectArr, 'a');
        Arrays.fill(textArr, 'a');
        mailCreateDto.setText(new String(textArr));
        mailCreateDto.setSubject(new String(subjectArr));
        mailCreateDto.setRecipients("");
        String json = objectMapper.writeValueAsString(mailCreateDto);
        MvcResult result = getResult(json, MockMvcRequestBuilders
                .post(CREATE_MAIL_API_ENDPOINT), MockMvcResultMatchers.status().isBadRequest());
        String content = result.getResponse().getContentAsString();
        HashMap<String, Object> map = objectMapper.readValue(content,
                new TypeReference<HashMap<String, Object>>() {
                });
        List<String> errors = (List<String>) map.get("errors");
        Assertions.assertTrue(errors.contains(RECIPIENTS_ERROR_MESSAGE));
        Assertions.assertTrue(errors.contains(SUBJECT_ERROR_MESSAGE));
        Assertions.assertTrue(errors.contains(TEXT_ERROR_MESSAGE));
    }

    @Test
    @WithMockUser(username = "nick", password = "1234")
    public void checkCreateMailIsOk() {
        MvcResult result = getCreateMailResult("john,mary",
                MockMvcResultMatchers.status().is3xxRedirection());
        String actual = result.getModelAndView().getViewName();
        Assertions.assertEquals(REDIRECT_TO_MAIL_BOX, actual);
    }

    @Test
    @WithMockUser(username = "nick", password = "1234")
    public void checkCreateMailFieldValueFail() {
        MvcResult result = getCreateMailResult("", MockMvcResultMatchers.status().isOk());
        Assertions.assertEquals("new_mail", result.getModelAndView().getViewName());
        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.mailCreateDto");
        Assertions.assertEquals(1, bindingResult.getErrorCount());
        List<String> errorMessages = bindingResult.getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        Assertions.assertTrue(errorMessages.contains("Recipients can't be null or blank!"));
    }

    @Test
    @WithMockUser(username = "nick", password = "1234")
    public void checkCreateMailNotExistentRecipient() {
        MvcResult result = getCreateMailResult("joh",
                MockMvcResultMatchers.status().is3xxRedirection());
        ModelAndView modelAndView = result.getModelAndView();
        Assertions.assertEquals(REDIRECT_TO_MAIL_PAGE, modelAndView.getViewName());
    }

    @Test
    @WithMockUser(username = "nick", password = "1234")
    public void checkGetCreateMailPage() {
        testDelete(MockMvcRequestBuilders
                .get(CREATE_MAIL_ENDPOINT), MockMvcResultMatchers.status().isOk(), "new_mail");
    }

    @Test
    @WithMockUser(username = "nick", password = "1234", roles = "APICALL")
    public void deleteMailIsOk() {
        getDeleteMailsResult("1", MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "nick", password = "1234")
    public void deleteMailFromDbIsOk() {
        testDelete(MockMvcRequestBuilders
                .post(MAILS_ENDPOINT)
                .param("ids", "1,2"),
                MockMvcResultMatchers.status().is3xxRedirection(), REDIRECT_TO_MAIL_BOX);
        deleteMailsOfJohn();
    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "nick", password = "1234", roles = "APICALL")
    public void deleteMailWithWrongId() {
        MvcResult result = getDeleteMailsResult("0",
                MockMvcResultMatchers.status().isBadRequest());
        String content = result.getResponse().getContentAsString();
        Map<String, Object> map = objectMapper.readValue(content,
                new TypeReference<HashMap<String, Object>>() {});
        Assertions.assertEquals(NON_EXISTENT_MAIL_ERROR_MESSAGE, map.get("error"));
    }

    @Test
    @WithMockUser(username = "nick", password = "1234")
    public void deleteMarkedMailsIsOk() {
        testDelete(MockMvcRequestBuilders.post(MAILS_ENDPOINT)
                .param("ids", "1,2"),
                MockMvcResultMatchers.status().is3xxRedirection(), REDIRECT_TO_MAIL_BOX);
    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "nick", password = "1234", roles = {"USER", "APICALL"})
    public void checkGetMailsIsOk() {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .get(MAILS_API_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        MailResponseDto expectedMail = new MailResponseDto();
        expectedMail.setId(2L);
        expectedMail.setText("Second text");
        expectedMail.setSubject("New theme");
        expectedMail.setSender("john");
        expectedMail.setRecipients("Arkhanhelskyi Mykyta Dmytrovych");
        expectedMail.setDate("17 September");
        List<MailResponseDto> actualMails = objectMapper.readValue(content,
                new TypeReference<List<MailResponseDto>>() {
                });
        Assertions.assertEquals(2, actualMails.size());
        Assertions.assertEquals(expectedMail, actualMails.get(0));
    }

    @Test
    @WithMockUser(username = "nick", password = "1234", roles = {"USER", "APICALL"})
    public void checkGetMailInfoIsOk() {
        MvcResult result = getResultOfRequestWithPathVariable(GET_MAIL_INFO_ENDPOINT);
        ModelAndView modelAndView = result.getModelAndView();
        Map<String, Object> model = modelAndView.getModel();
        Assertions.assertEquals("mail_info", modelAndView.getViewName());
        Assertions.assertEquals(1L, model.get("user_id"));
        Assertions.assertEquals("Arkhanhelskyi Mykyta Dmytrovych", model.get("recipients"));
        Assertions.assertEquals("john", model.get("sender"));
    }

    @Test
    @WithMockUser(username = "nick", password = "1234")
    public void getAnswerMailPageIsOk() {
        MvcResult result = getResultOfRequestWithPathVariable(GET_ANSWER_MAIL_PAGE_ENDPOINT);
        ModelAndView modelAndView = result.getModelAndView();
        String expectedView = "new_mail";
        String actualView = modelAndView.getViewName();
        Assertions.assertEquals(expectedView, actualView);
        Map<String, Object> model = modelAndView.getModel();
        MailCreateDto mailCreateDto = (MailCreateDto) model.get("mailCreateDto");
        Assertions.assertNotNull(mailCreateDto);
    }

    @SneakyThrows
    @Test
    @WithMockUser(username = "nick", password = "1234", roles = "APICALL")
    public void checkAnswerMailApiIsOk() {
        MailAnswerDto mailAnswerDto = new MailAnswerDto();
        mailAnswerDto.setText("some text");
        String json = objectMapper.writeValueAsString(mailAnswerDto);
        MvcResult result = getResult(json, MockMvcRequestBuilders
                .post(ANSWER_MAIL_API_ENDPOINT, "2"), MockMvcResultMatchers.status().isOk());
        String content = result.getResponse().getContentAsString();
        MailResponseDto mailResponseDto = objectMapper.readValue(content,
                MailResponseDto.class);
        Assertions.assertEquals("Arkhanhelskyi Mykyta Dmytrovych", mailResponseDto.getSender());
        Assertions.assertEquals(mailAnswerDto.getText(), mailResponseDto.getText());
        Assertions.assertEquals("New theme", mailResponseDto.getSubject());
        Assertions.assertEquals("john", mailResponseDto.getRecipients());
    }

    @Test
    @WithMockUser(username = "nick", password = "1234", roles = "APICALL")
    public void getMailBySearchTextIsOk() {
        MailSearchDto mailSearchDto = new MailSearchDto();
        mailSearchDto.setSearchText("mary");
        testGetMailBySearchText(mailSearchDto, 1);
        mailSearchDto.setSearchText("nick");
        testGetMailBySearchText(mailSearchDto, 2);
        mailSearchDto.setSearchText("subject");
        testGetMailBySearchText(mailSearchDto, 0);
    }

    @SneakyThrows
    private MvcResult getResultOfRequestWithPathVariable(String endpoint) {
        return mockMvc.perform(MockMvcRequestBuilders
                .get(endpoint, "2")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    @WithMockUser(username = "john", password = "1234")
    private void deleteMailsOfJohn() {
        testDelete(MockMvcRequestBuilders
                        .post(MAILS_ENDPOINT)
                        .param("ids", "1,2"),
                MockMvcResultMatchers.status().is3xxRedirection(), REDIRECT_TO_MAIL_BOX);
    }

    @SneakyThrows
    private MvcResult getDeleteMailsResult(String val, ResultMatcher status) {
        return mockMvc.perform(MockMvcRequestBuilders
                .delete(DELETE_MAIL_API_ENDPOINT, val)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andReturn();
    }

    @SneakyThrows
    private void testGetMailBySearchText(MailSearchDto mailSearchDto, int size) {
        String json = objectMapper.writeValueAsString(mailSearchDto);
        mockMvc.perform(MockMvcRequestBuilders
                .post(MAILS_API_ENDPOINT)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(size)))
                .andReturn();
    }

    @SneakyThrows
    private void testDelete(MockHttpServletRequestBuilder builder,
                            ResultMatcher status, String redirection) {
        MvcResult result = mockMvc.perform(builder
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andReturn();
        Assertions.assertEquals(redirection, result.getModelAndView().getViewName());
    }

    @SneakyThrows
    private MvcResult getResult(String json, MockHttpServletRequestBuilder builder,
                                ResultMatcher ok) {
        return mockMvc.perform(builder
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(ok)
                .andReturn();
    }

    @SneakyThrows
    private MvcResult getCreateMailResult(String recipients, ResultMatcher status) {
        return mockMvc.perform(MockMvcRequestBuilders
                .post(CREATE_MAIL_ENDPOINT)
                .param("text", "text")
                .param("subject", "subject")
                .param("recipients", recipients)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andReturn();
    }
}
