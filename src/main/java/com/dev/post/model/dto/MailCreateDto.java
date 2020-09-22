package com.dev.post.model.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class MailCreateDto {
    @NotBlank(message = "Recipients can't be null or blank!")
    String recipients;
    @Length(max = 150, message = "Max length mustn't be greater than 150 symbols!")
    String subject;
    @Length(max = 1024, message = "Max length mustn't be greater than 1024 symbols!")
    String text;
}
