package com.dev.post.model.dto;

import lombok.Data;

@Data
public class MailSearchIndexDto {
    private Long objectID;
    private String subject;
    private String text;
    private String senderNames;
    private String recipientsNames;
}
