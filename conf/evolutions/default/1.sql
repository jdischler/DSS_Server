# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table account (
  email                     varchar(255) not null,
  password                  varchar(255),
  organization              varchar(255),
  password_salt             varchar(255),
  admin                     boolean,
  access_flags              integer,
  constraint pk_account primary key (email))
;

create table pending_registration (
  email                     varchar(255) not null,
  password                  varchar(255),
  organization              varchar(255),
  validation_code           varchar(255),
  password_salt             varchar(255),
  create_time               timestamp,
  constraint pk_pending_registration primary key (email))
;

create table session_id (
  session_id                bigint not null,
  create_time               timestamp,
  user_email                varchar(255),
  constraint pk_session_id primary key (session_id))
;

create sequence account_seq;

create sequence pending_registration_seq;

create sequence session_id_session_id_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists account;

drop table if exists pending_registration;

drop table if exists session_id;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists account_seq;

drop sequence if exists pending_registration_seq;

drop sequence if exists session_id_session_id_seq;

