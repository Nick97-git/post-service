package com.dev.post.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Mail {
    private Long id;
    private String text;
    private String subject;
    private Long senderId;
    private List<User> recipients;
    private LocalDateTime date;
}
