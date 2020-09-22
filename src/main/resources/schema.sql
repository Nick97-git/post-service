drop table if exists delete_status_mails;
drop table if exists users_roles;
drop table if exists users_mails;
drop table if exists users;
drop table if exists roles;
drop table if exists mails;

create table users (
    id bigint(20) auto_increment primary key,
    login varchar(50) not null,
    password varchar(250) not null,
    full_name varchar(100),
    photo longblob
);

create table roles (
    id bigint(20) auto_increment primary key,
    role_name varchar(50) not null
);

create table users_roles (
    user_id   bigint not null,
    role_id bigint not null ,
    constraint users_roles_users_fk
        foreign key (user_id)
            references users (id)
            on delete no action
            on update no action,
    constraint users_roles_roles_fk
        foreign key (role_id)
            references roles (id)
            on delete no action
            on update no action
);

create table mails (
    id bigint(20) auto_increment primary key,
    subject varchar(150) not null,
    text varchar(1024) not null,
    sender_id bigint(20) not null,
    date varchar(150) not null
);

create table users_mails(
    user_id bigint not null,
    mail_id bigint not null,
    constraint users_mails_users_fk
        foreign key (user_id)
            references users (id)
            on delete no action
            on update no action,
    constraint users_mails_mails_fk
        foreign key (mail_id)
            references mails (id)
            on delete no action
            on update no action
);

create table delete_status_mails(
    user_id bigint not null,
    mail_id bigint not null,
    constraint delete_status_mails_users_fk
        foreign key (user_id)
        references users (id)
        on delete no action
        on update no action,
    constraint delete_status_mails_mails_fk
        foreign key (mail_id)
        references mails (id)
        on delete no action
        on update no action
);

insert into roles(role_name) values('USER');
insert into roles(role_name) values('APICALL');
insert into users(login, password, full_name) values('nick', '$2a$10$LsMM125mAajd8SswmVMNPuyNzniCaJWU4z0ShKkYiV7ZAzjXU2hJC',
                                                            'Arkhanhelskyi Mykyta Dmytrovych');
insert into users(login, password) values('john', '$2a$10$LsMM125mAajd8SswmVMNPuyNzniCaJWU4z0ShKkYiV7ZAzjXU2hJC');
insert into users(login, password) values('mary', '$2a$10$LsMM125mAajd8SswmVMNPuyNzniCaJWU4z0ShKkYiV7ZAzjXU2hJC');
insert into users_roles(user_id, role_id) values(1, 1);
insert into users_roles(user_id, role_id) values(1, 2);
insert into users_roles(user_id, role_id) values(2, 1);
insert into users_roles(user_id, role_id) values(3, 1);
