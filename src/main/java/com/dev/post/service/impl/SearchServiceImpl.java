package com.dev.post.service.impl;

import com.algolia.search.models.indexing.Query;
import com.dev.post.model.dto.MailSearchIndexDto;
import com.dev.post.service.MailService;
import com.dev.post.service.SearchService;
import com.dev.post.singleton.SearchIndexSingleton;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final SearchIndexSingleton searchIndexSingleton;
    private final MailService mailService;

    @Override
    public List<MailSearchIndexDto> findMails(String searchText, String login) {
        List<MailSearchIndexDto> searchIndexDtos = searchIndexSingleton
                .getSearchIndex().search(new Query(searchText)).getHits();
        return searchIndexDtos.stream()
                .filter(mailSearchDto -> mailSearchDto.getSenderNames().contains(login)
                        || mailSearchDto.getRecipientsNames().contains(login))
                .filter(mailSearchIndexDto ->
                        !mailService.hasReference(mailSearchIndexDto.getObjectID(), login))
                .sorted((first, second) -> second.getObjectID()
                        .compareTo(first.getObjectID()))
                .limit(20)
                .collect(Collectors.toList());
    }

    @Override
    public void save(MailSearchIndexDto mailSearchIndexDto) {
        searchIndexSingleton.getSearchIndex().saveObject(mailSearchIndexDto);
    }

    @Override
    public void delete(String mailId) {
        searchIndexSingleton.getSearchIndex().deleteObject(mailId);
    }
}
