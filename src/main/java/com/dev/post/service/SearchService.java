package com.dev.post.service;

import com.dev.post.model.dto.MailSearchIndexDto;
import java.util.List;

public interface SearchService {

    List<MailSearchIndexDto> findMails(String searchText, String login);

    void save(MailSearchIndexDto mailSearchIndexDto);

    void delete(String mailId);
}
