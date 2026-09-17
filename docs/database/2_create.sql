-- Created by Vertabelo (http://vertabelo.com)
-- Last modification date: 2024-03-28 12:01:59.87

-- tables
-- Table: city
CREATE TABLE city (
                      id serial  NOT NULL,
                      name varchar(255)  NOT NULL,
                      CONSTRAINT city_pk PRIMARY KEY (id)
);

-- Table: location
CREATE TABLE location (
                          id serial  NOT NULL,
                          city_id int  NOT NULL,
                          name varchar(255)  NOT NULL,
                          number_of_atms int  NOT NULL,
                          status char(1)  NOT NULL,
                          lng numeric(10,7),
                          lat numeric(10,7),
                          CONSTRAINT location_pk PRIMARY KEY (id)
);

-- Table: profile
CREATE TABLE profile (
                                id serial  NOT NULL,
                                user_id int  NOT NULL,
                                address varchar(255)  NOT NULL,
                                phone_number varchar(255)  NOT NULL,
                                CONSTRAINT profile_pk PRIMARY KEY (id)
);


-- Table: location_image
CREATE TABLE location_image (
                                id serial  NOT NULL,
                                location_id int  NOT NULL,
                                data bytea  NOT NULL,
                                CONSTRAINT location_image_pk PRIMARY KEY (id)
);

-- Table: location_transaction_type
CREATE TABLE location_transaction_type (
                                           id serial  NOT NULL,
                                           location_id int  NOT NULL,
                                           transaction_type_id int  NOT NULL,
                                           CONSTRAINT location_transaction_type_pk PRIMARY KEY (id)
);

-- Table: role
CREATE TABLE role (
                      id serial  NOT NULL,
                      name varchar(255)  NOT NULL,
                      CONSTRAINT role_ak_1 UNIQUE (name) NOT DEFERRABLE  INITIALLY IMMEDIATE,
                      CONSTRAINT role_pk PRIMARY KEY (id)
);

-- Table: transaction_type
CREATE TABLE transaction_type (
                                  id serial  NOT NULL,
                                  name varchar(255)  NOT NULL,
                                  CONSTRAINT transaction_type_pk PRIMARY KEY (id)
);

-- Table: user
CREATE TABLE "user" (
                        id serial  NOT NULL,
                        role_id int  NOT NULL,
                        username varchar(255)  NOT NULL,
                        password varchar(255)  NOT NULL,
                        status char(1)  NOT NULL,
                        CONSTRAINT user_ak_1 UNIQUE (username) NOT DEFERRABLE  INITIALLY IMMEDIATE,
                        CONSTRAINT user_pk PRIMARY KEY (id)
);

-- Table: user_image
CREATE TABLE user_image (
                            id serial  NOT NULL,
                            user_id int  NOT NULL,
                            data bytea  NOT NULL,
                            CONSTRAINT user_image_pk PRIMARY KEY (id)
);

-- foreign keys
-- Reference: location_city (table: location)
ALTER TABLE location ADD CONSTRAINT location_city
    FOREIGN KEY (city_id)
        REFERENCES city (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: location_image_location (table: location_image)
ALTER TABLE location_image ADD CONSTRAINT location_image_location
    FOREIGN KEY (location_id)
        REFERENCES location (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: location_transaction_type_location (table: location_transaction_type)
ALTER TABLE location_transaction_type ADD CONSTRAINT location_transaction_type_location
    FOREIGN KEY (location_id)
        REFERENCES location (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: location_transaction_type_transaction_type (table: location_transaction_type)
ALTER TABLE location_transaction_type ADD CONSTRAINT location_transaction_type_transaction_type
    FOREIGN KEY (transaction_type_id)
        REFERENCES transaction_type (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: user_image_user (table: user_image)
ALTER TABLE user_image ADD CONSTRAINT user_image_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: profile_user (table: profile)
ALTER TABLE profile ADD CONSTRAINT profile_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
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

-- views
-- View: location_transaction_type_view
-- Kasutusel /api/v2/atm/locations teenuses (õppise eesmärgil) - kombineerib city, location ja
-- transaction_type andmed ühte lamedasse ritta, näidates ka tehingutüübid, mida asukohas ei pakuta
-- (is_available = false).
CREATE VIEW location_transaction_type_view AS
SELECT c.id       AS city_id,
       c.name     AS city_name,
       l.id       AS location_id,
       l.name     AS location_name,
       l.status   AS location_status,
       l.lng      AS lng,
       l.lat      AS lat,
       tt.id      AS transaction_type_id,
       tt.name    AS transaction_type_name,
       (ltt.id IS NOT NULL) AS is_available
FROM location l
         JOIN city c ON c.id = l.city_id
         CROSS JOIN transaction_type tt
         LEFT JOIN location_transaction_type ltt
                   ON ltt.location_id = l.id AND ltt.transaction_type_id = tt.id;

-- End of file.

