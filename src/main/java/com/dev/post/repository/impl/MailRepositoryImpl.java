package com.dev.post.repository.impl;

import com.dev.post.mapper.row.MailRowMapper;
import com.dev.post.model.Mail;
import com.dev.post.model.User;
import com.dev.post.repository.MailRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class MailRepositoryImpl implements MailRepository {
    private static final String COUNT_DELETE_STATUS_REFERENCES_FOR_MAIL_AND_USER =
            "select count(*) from delete_status_mails where mail_id = ? and user_id = ?;";
    private static final String COUNT_DELETE_STATUS_REFERENCES_FOR_MAIL = "select count(*) from "
            + "delete_status_mails where mail_id = ?;";
    private static final String DELETE_DELETE_STATUS_MAILS = "delete from delete_status_mails "
            + "where mail_id = ?;";
    private static final String DELETE_MAIL_BY_ID = "delete from mails where id = ?";
    private static final String DELETE_MAIL_RECIPIENTS = "delete from users_mails"
            + " where mail_id = ?;";
    private static final String INSERT_DELETE_MAIL_STATUS_FOR_USER =
            "insert into delete_status_mails (user_id, mail_id) values (?, ?)";
    private static final String INSERT_MAILS = "insert into mails (subject, text, sender_id, date) "
            + "values (?, ?, ?, ?)";
    private static final String INSERT_RECIPIENTS_OF_MAIL = "insert into users_mails(user_id, "
            + "mail_id) values(?, ?)";
    private static final String SELECT_MAIL_BY_ID = "select * from mails where id = ?;";
    private static final String SELECT_RECIPIENTS_OF_MAIL = "select users.id, users.login,"
            + " users.full_name from users_mails inner join users "
            + "on users_mails.user_id = users.id where mail_id = ?;";
    private static final String SELECT_USER_MAILS = "select * from mails "
            + "where (mails.sender_id = ? or exists(select * from users_mails "
            + "where users_mails.mail_id = mails.id and users_mails.user_id = ?)) "
            + "and not exists(select * from delete_status_mails "
            + "where delete_status_mails.mail_id = mails.id and user_id = ?)"
            + " order by id desc limit 20;";
    private static final int DATE_COLUMN_INDEX = 4;
    private static final int SENDER_ID_COLUMN_INDEX = 3;
    private static final int SUBJECT_COLUMN_INDEX = 1;
    private static final int TEXT_COLUMN_INDEX = 2;
    private final JdbcTemplate jdbcTemplate;
    private final MailRowMapper mailRowMapper;

    @Override
    public List<Mail> findUserMails(User user) {
        List<Mail> mails = jdbcTemplate
                .query(SELECT_USER_MAILS,
                        new Object[]{user.getId(), user.getId(),
                        user.getId()}, mailRowMapper);
        mails.forEach(mail -> mail.setRecipients(getRecipientsOfMail(mail)));
        return mails;
    }

    @Override
    public void deleteMailById(String mailId, User user) {
        jdbcTemplate.update(INSERT_DELETE_MAIL_STATUS_FOR_USER, user.getId(), mailId);
    }

    @Override
    public void deleteMailFully(String mailId) {
        jdbcTemplate.update(DELETE_DELETE_STATUS_MAILS, mailId);
        jdbcTemplate.update(DELETE_MAIL_RECIPIENTS, mailId);
        jdbcTemplate.update(DELETE_MAIL_BY_ID, mailId);
    }

    @Override
    public Integer getNumOfReferences(String mailId) {
        return jdbcTemplate.queryForObject(
                COUNT_DELETE_STATUS_REFERENCES_FOR_MAIL, new Object[] {mailId}, Integer.class);
    }

    @Override
    public Integer getNumOfReferences(Long mailId, Long userId) {
        return jdbcTemplate.queryForObject(
                COUNT_DELETE_STATUS_REFERENCES_FOR_MAIL_AND_USER,
                new Object[] {mailId, userId}, Integer.class);
    }

    @Override
    public Mail createMail(Mail mail) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> getPrepareStatement(connection, mail), keyHolder);
        mail.setId((Long) keyHolder.getKey());
        addRecipientsForMail(mail);
        mail.setRecipients(getRecipientsOfMail(mail));
        return mail;
    }

    @Override
    public Mail findMailById(Long mailId) {
        try {
            Mail mail = jdbcTemplate.queryForObject(SELECT_MAIL_BY_ID,
                    new Object[]{mailId}, mailRowMapper);
            mail.setRecipients(getRecipientsOfMail(mail));
            return mail;
        } catch (DataAccessException e) {
            return null;
        }
    }

    private PreparedStatement getPrepareStatement(Connection connection, Mail mail)
            throws SQLException {
        PreparedStatement preparedStatement = connection
                .prepareStatement(INSERT_MAILS, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(SUBJECT_COLUMN_INDEX, mail.getSubject());
        preparedStatement.setString(TEXT_COLUMN_INDEX, mail.getText());
        preparedStatement.setLong(SENDER_ID_COLUMN_INDEX, mail.getSenderId());
        preparedStatement.setString(DATE_COLUMN_INDEX, mail.getDate().toString());
        return preparedStatement;
    }

    private List<User> getRecipientsOfMail(Mail mail) {
        return jdbcTemplate.query(SELECT_RECIPIENTS_OF_MAIL, new Object[]{mail.getId()},
                (rs, rowNum) -> {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setLogin(rs.getString("login"));
                    user.setFullName(rs.getString("full_name"));
                    return user;
                });
    }

    private void addRecipientsForMail(Mail mail) {
        for (User recipient : mail.getRecipients()) {
            jdbcTemplate.update(INSERT_RECIPIENTS_OF_MAIL,
                    recipient.getId(), mail.getId());
        }
    }
}
