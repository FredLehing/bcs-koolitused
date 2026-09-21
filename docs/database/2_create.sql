-- Created by Redgate Data Modeler (https://datamodeler.redgate-platform.com)
-- Last modification date: 2026-09-21 08:58:27.055

-- tables
-- Table: category
CREATE TABLE category (
                          id serial  NOT NULL,
                          name varchar(255)  NOT NULL,
                          CONSTRAINT category_pk PRIMARY KEY (id)
);

-- Table: certificate_template
CREATE TABLE certificate_template (
                                      id serial  NOT NULL,
                                      course_id int  NOT NULL,
                                      status varchar(3)  NOT NULL,
                                      CONSTRAINT certificate_template_pk PRIMARY KEY (id)
);

-- Table: course
CREATE TABLE course (
                        id serial  NOT NULL,
                        training_id int  NOT NULL,
                        lecturer_id int  NULL,
                        room_id int  NULL,
                        number_of_days int  NOT NULL,
                        number_of_academic_hours int  NOT NULL,
                        price decimal(9,4)  NOT NULL,
                        status varchar(3)  NOT NULL,
                        start_date date  NOT NULL,
                        end_date date  NOT NULL,
                        notes text  NULL,
                        meeting_link varchar(255)  NULL,
                        CONSTRAINT course_session_pk PRIMARY KEY (id)
);

-- Table: course_participant
CREATE TABLE course_participant (
                                    id serial  NOT NULL,
                                    course_id int  NOT NULL,
                                    participant_id int  NOT NULL,
                                    notes text  NOT NULL,
                                    has_paid boolean  NOT NULL,
                                    requires_laptop boolean  NOT NULL,
                                    status varchar(3)  NOT NULL,
                                    created_at timestamp  NOT NULL,
                                    updated_at timestamp  NOT NULL,
                                    CONSTRAINT course_participant_pk PRIMARY KEY (id)
);

-- Table: enquiry
CREATE TABLE enquiry (
                         id serial  NOT NULL,
                         training_id int  NOT NULL,
                         profile_id int  NOT NULL,
                         course_id int  NULL,
                         option_id int  NOT NULL,
                         message varchar(255)  NOT NULL,
                         company_name varchar(255)  NULL,
                         status char(1)  NOT NULL,
                         created_at timestamp  NOT NULL,
                         updated_at timestamp  NOT NULL,
                         CONSTRAINT applicant_pk PRIMARY KEY (id)
);

-- Table: feedback
CREATE TABLE feedback (
                          id serial  NOT NULL
);

-- Table: lecturer
CREATE TABLE lecturer (
                          id serial  NOT NULL,
                          full_name varchar(255)  NOT NULL,
                          bio text  NOT NULL,
                          photo bytea  NOT NULL,
                          CONSTRAINT lecturer_pk PRIMARY KEY (id)
);

-- Table: location
CREATE TABLE location (
                          id serial  NOT NULL,
                          name varchar(255)  NOT NULL,
                          address text  NOT NULL,
                          is_online boolean  NOT NULL,
                          CONSTRAINT location_pk PRIMARY KEY (id)
);

-- Table: newsletter
CREATE TABLE newsletter (
                            id serial  NOT NULL,
                            email varchar(255)  NOT NULL,
                            first_name varchar(255)  NOT NULL,
                            last_name varchar(255)  NOT NULL,
                            status varchar(1)  NOT NULL,
                            CONSTRAINT newsletter_pk PRIMARY KEY (id)
);

-- Table: option
CREATE TABLE option (
                        id serial  NOT NULL,
                        name varchar(20)  NOT NULL,
                        type varchar(2)  NOT NULL,
                        CONSTRAINT option_pk PRIMARY KEY (id)
);

-- Table: participant
CREATE TABLE participant (
                             id serial  NOT NULL,
                             user_id int  NOT NULL,
                             name varchar(255)  NOT NULL,
                             profile_id int  NOT NULL,
                             CONSTRAINT participant_pk PRIMARY KEY (id)
);

-- Table: participant_certificate
CREATE TABLE participant_certificate (
                                         id serial  NOT NULL,
                                         file bytea  NOT NULL,
                                         participant_id int  NOT NULL,
                                         CONSTRAINT participant_certificate_pk PRIMARY KEY (id)
);

-- Table: profile
CREATE TABLE profile (
                         id serial  NOT NULL,
                         first_name varchar(255)  NOT NULL,
                         last_name varchar(255)  NOT NULL,
                         phone varchar(20)  NOT NULL,
                         email varchar(255)  NOT NULL,
                         CONSTRAINT person_pk PRIMARY KEY (id)
);

-- Table: role
CREATE TABLE role (
                      id serial  NOT NULL,
                      name varchar(255)  NOT NULL,
                      CONSTRAINT role_pk PRIMARY KEY (id)
);

-- Table: room
CREATE TABLE room (
                      id serial  NOT NULL,
                      name varchar(255)  NOT NULL,
                      status varchar(3)  NOT NULL,
                      CONSTRAINT room_pk PRIMARY KEY (id)
);

-- Table: training
CREATE TABLE training (
                          id serial  NOT NULL,
                          user_id int  NOT NULL,
                          default_lecturer_id int  NULL,
                          category_id int  NOT NULL,
                          location_id int  NOT NULL,
                          title varchar(255)  NOT NULL,
                          short_description varchar(255)  NOT NULL,
                          description text  NOT NULL,
                          language char(3)  NOT NULL,
                          status int  NOT NULL,
                          created_at timestamp  NOT NULL,
                          updated_at timestamp  NOT NULL,
                          order_only boolean  NOT NULL,
                          CONSTRAINT course_pk PRIMARY KEY (id)
);

-- Table: user
CREATE TABLE "user" (
                        id serial  NOT NULL,
                        role_id int  NOT NULL,
                        email varchar(255)  NOT NULL,
                        password varchar(255)  NOT NULL,
                        status char(1)  NOT NULL,
                        CONSTRAINT user_pk PRIMARY KEY (id)
);

CREATE INDEX user_role_idx_1 on "user" (role_id ASC);

-- foreign keys
-- Reference: application_course (table: enquiry)
ALTER TABLE enquiry ADD CONSTRAINT application_course
    FOREIGN KEY (course_id)
        REFERENCES course (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: application_option (table: enquiry)
ALTER TABLE enquiry ADD CONSTRAINT application_option
    FOREIGN KEY (option_id)
        REFERENCES option (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: application_training (table: enquiry)
ALTER TABLE enquiry ADD CONSTRAINT application_training
    FOREIGN KEY (training_id)
        REFERENCES training (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: certificate_participant (table: participant_certificate)
ALTER TABLE participant_certificate ADD CONSTRAINT certificate_participant
    FOREIGN KEY (participant_id)
        REFERENCES participant (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: certificate_template_course (table: certificate_template)
ALTER TABLE certificate_template ADD CONSTRAINT certificate_template_course
    FOREIGN KEY (course_id)
        REFERENCES course (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: course_category (table: training)
ALTER TABLE training ADD CONSTRAINT course_category
    FOREIGN KEY (category_id)
        REFERENCES category (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: course_location (table: training)
ALTER TABLE training ADD CONSTRAINT course_location
    FOREIGN KEY (location_id)
        REFERENCES location (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: course_participant_course (table: course_participant)
ALTER TABLE course_participant ADD CONSTRAINT course_participant_course
    FOREIGN KEY (course_id)
        REFERENCES course (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: course_participant_participant (table: course_participant)
ALTER TABLE course_participant ADD CONSTRAINT course_participant_participant
    FOREIGN KEY (participant_id)
        REFERENCES participant (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: course_room (table: course)
ALTER TABLE course ADD CONSTRAINT course_room
    FOREIGN KEY (room_id)
        REFERENCES room (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: course_session_training (table: course)
ALTER TABLE course ADD CONSTRAINT course_session_training
    FOREIGN KEY (training_id)
        REFERENCES training (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: course_timetable_lecturer (table: course)
ALTER TABLE course ADD CONSTRAINT course_timetable_lecturer
    FOREIGN KEY (lecturer_id)
        REFERENCES lecturer (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: course_user (table: training)
ALTER TABLE training ADD CONSTRAINT course_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: enquiry_profile (table: enquiry)
ALTER TABLE enquiry ADD CONSTRAINT enquiry_profile
    FOREIGN KEY (profile_id)
        REFERENCES profile (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: participant_profile (table: participant)
ALTER TABLE participant ADD CONSTRAINT participant_profile
    FOREIGN KEY (profile_id)
        REFERENCES profile (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: participant_user (table: participant)
ALTER TABLE participant ADD CONSTRAINT participant_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: training_lecturer (table: training)
ALTER TABLE training ADD CONSTRAINT training_lecturer
    FOREIGN KEY (default_lecturer_id)
        REFERENCES lecturer (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: user_role (table: user)
ALTER TABLE "user" ADD CONSTRAINT user_role
    FOREIGN KEY (role_id)
        REFERENCES role (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- End of file.