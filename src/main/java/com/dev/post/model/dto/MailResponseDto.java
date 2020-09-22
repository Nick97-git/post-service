package com.dev.post.model.dto;

import lombok.Data;

@Data
public class MailResponseDto {
    private Long id;
    private String subject;
    private String text;
    private String sender;
    private String recipients;
    private String date;
}
