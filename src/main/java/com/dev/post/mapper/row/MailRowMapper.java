package com.dev.post.mapper.row;

import com.dev.post.model.Mail;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class MailRowMapper implements RowMapper<Mail> {
    private static final String ID_COLUMN_NAME = "id";
    private static final String SUBJECT_COLUMN_NAME = "subject";
    private static final String TEXT_COLUMN_NAME = "text";
    private static final String DATE_COLUMN_NAME = "date";
    private static final String SENDER_ID_COLUMN_NAME = "sender_id";

    @Override
    public Mail mapRow(ResultSet resultSet, int i) throws SQLException {
        Mail mail = new Mail();
        mail.setId(resultSet.getLong(ID_COLUMN_NAME));
        mail.setSubject(resultSet.getString(SUBJECT_COLUMN_NAME));
        mail.setText(resultSet.getString(TEXT_COLUMN_NAME));
        mail.setDate(LocalDateTime.parse(resultSet.getString(DATE_COLUMN_NAME)));
        mail.setSenderId(resultSet.getLong(SENDER_ID_COLUMN_NAME));
        return mail;
    }
}
