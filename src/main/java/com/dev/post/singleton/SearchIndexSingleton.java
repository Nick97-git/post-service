package com.dev.post.singleton;

import com.algolia.search.DefaultSearchClient;
import com.algolia.search.SearchClient;
import com.algolia.search.SearchIndex;
import com.algolia.search.models.settings.IndexSettings;
import com.dev.post.model.dto.MailSearchIndexDto;
import java.util.Arrays;
import org.springframework.stereotype.Component;

@Component
public class SearchIndexSingleton {
    private static final String ADMIN_API_KEY = "02f0b23379c11a841df81d0715986f70";
    private static final String APP_ID = "NX5OZGSJAW";
    private static final String INDEX_NAME = "mail_index";
    private SearchIndex<MailSearchIndexDto> searchIndex;

    public SearchIndex<MailSearchIndexDto> getSearchIndex() {
        if (searchIndex == null) {
            initIndex();
        }
        return searchIndex;
    }

    private void initIndex() {
        SearchClient client = DefaultSearchClient.create(APP_ID, ADMIN_API_KEY);
        searchIndex = client
                .initIndex(INDEX_NAME, MailSearchIndexDto.class);
        searchIndex.setSettings(new IndexSettings().setSearchableAttributes(
                Arrays.asList("recipientsNames", "senderNames", "text", "subject")
        ));
    }
}
