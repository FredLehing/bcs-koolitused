-- Näidisandmed arenduse ja testimise jaoks
-- Käivitatakse pärast 1_reset_database.sql ja 2_create.sql skripte

-- Table: role
INSERT INTO role (id, name) VALUES
    (1, 'admin'),
    (2, 'participant');

-- Table: user
INSERT INTO "user" (id, role_id, email, password, status, created_at) VALUES
    (1, 1, 'admin@vali-it.ee', 'parool123', 'A', '2026-01-01 00:00:00'),
    (2, 2, 'kasutaja@vali-it.ee', 'parool123', 'A', '2026-09-05 09:00:00'),
    (3, 1, 'admin', '123', 'A', '2026-09-28 00:00:00'); -- lihtne testkonto (admin)

-- Table: language
INSERT INTO language (id, code, name, is_main_language, requires_translation, flag_icon_code) VALUES
    (1, 'et', 'Eesti', true, true, 'fi-ee'),
    (2, 'en', 'English', false, true, 'fi-gb'),
    (3, 'ru', 'Русский', false, false, 'fi-ru');

-- Table: category
INSERT INTO category (id, created_at, updated_at, created_by) VALUES
    (1, '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (2, '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (3, '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1);

-- Table: category_translation
INSERT INTO category_translation (id, category_id, language_id, name, created_at, updated_at) VALUES
    (1, 1, 1, 'Programmeerimine', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (2, 1, 2, 'Programming', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (3, 2, 1, 'Disain', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (4, 2, 2, 'Design', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (5, 3, 1, 'Juhtimine', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (6, 3, 2, 'Management', '2026-07-15 09:00:00', '2026-07-15 09:00:00');

-- Table: location
INSERT INTO location (id, name, address, is_online, created_at, updated_at, created_by) VALUES
    (1, 'BCS Koolitus', 'Aia tn 7, Tallinn', false, '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (2, 'Veebiõpe', 'Veebipõhine koolitus (nt. Zoom, Teams)', true, '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (3, 'Hübriidõpe', 'Koha peal + veebiõpe', true, '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1);

-- Table: lecturer
INSERT INTO lecturer (id, full_name, photo, created_at, updated_at, created_by) VALUES
    (1, 'Mari Tamm', ''::bytea, '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (2, 'Jaan Kask', ''::bytea, '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1);

-- Table: lecturer_translation
INSERT INTO lecturer_translation (id, lecturer_id, language_id, bio, created_at, updated_at) VALUES
    (1, 1, 1, 'Üle 10 aasta kogemust tarkvaraarenduse koolitajana.', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (2, 1, 2, 'Over 10 years of experience as a software development trainer.', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (3, 2, 1, 'IT-projektijuht ja Scrum Master, koolitab meeskonnatöö teemadel.', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (4, 2, 2, 'IT project manager and Scrum Master, training on teamwork topics.', '2026-07-15 09:00:00', '2026-07-15 09:00:00');

-- Table: room
INSERT INTO room (id, name, status) VALUES
    (1, 'A101', 'VAB'),
    (2, 'A102', 'KIN');

-- Table: option
INSERT INTO option (id, type, created_at, updated_at) VALUES
    (1, 'FM', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (2, 'FM', '2026-07-15 09:00:00', '2026-07-15 09:00:00');

-- Table: option_translation
INSERT INTO option_translation (id, option_id, language_id, name, created_at, updated_at) VALUES
    (1, 1, 1, 'Auditoorne', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (2, 1, 2, 'In-person', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (3, 2, 1, 'Veebipõhine', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (4, 2, 2, 'Online', '2026-07-15 09:00:00', '2026-07-15 09:00:00');

-- Table: profile
INSERT INTO profile (id, first_name, last_name, phone, email, created_at, updated_at) VALUES
    (1, 'Anna', 'Saar', '+37256789012', 'anna.saar@example.com', '2026-09-05 09:00:00', '2026-09-05 09:00:00'),
    (2, 'Peeter', 'Mets', '+37251234567', 'peeter.mets@example.com', '2026-09-16 14:15:00', '2026-09-16 14:15:00');

-- Table: participant
INSERT INTO participant (id, user_id, name, profile_id, created_at) VALUES
    (1, 2, 'Anna Saar', 1, '2026-09-05 09:00:00');

-- Table: training
INSERT INTO training (id, user_id, default_lecturer_id, category_id, training_language_id, location_id, status, created_at, updated_at, is_orderable, is_promoted) VALUES
    (1, 1, 1, 1, 1, 1, 'P', '2026-08-01 09:00:00', '2026-08-01 09:00:00', true, true),
    (2, 1, 2, 3, 1, 2, 'P', '2026-08-05 10:00:00', '2026-08-05 10:00:00', false, false),
    (3, 1, 1, 1, 1, 2, 'P', '2026-08-10 09:00:00', '2026-08-10 09:00:00', true, false),
    (4, 1, 1, 1, 2, 2, 'P', '2026-08-12 09:00:00', '2026-08-12 09:00:00', true, true),
    (5, 1, NULL, 2, 1, 1, 'P', '2026-08-14 09:00:00', '2026-08-14 09:00:00', true, false),
    (6, 1, NULL, 2, 2, 3, 'P', '2026-08-16 09:00:00', '2026-08-16 09:00:00', false, false),
    (7, 1, 2, 3, 1, 3, 'P', '2026-08-18 09:00:00', '2026-08-18 09:00:00', true, false),
    (8, 1, 1, 1, 1, 1, 'P', '2026-08-20 09:00:00', '2026-08-20 09:00:00', true, false);

-- Table: funding_type
INSERT INTO funding_type (id, code, created_at, updated_at, created_by) VALUES
    (1, 'JOB_CENTRE', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (2, 'EU_FUNDED', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1);

-- Table: funding_type_translation
INSERT INTO funding_type_translation (id, funding_type_id, language_id, name, created_at, updated_at) VALUES
    (1, 1, 1, 'Töötukassa', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (2, 1, 2, 'Job Centre', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (3, 2, 1, 'EL rahastus', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (4, 2, 2, 'EU Funded', '2026-07-15 09:00:00', '2026-07-15 09:00:00');

-- Table: training_funding_type
INSERT INTO training_funding_type (id, training_id, funding_type_id) VALUES
    (1, 1, 1),
    (2, 3, 1),
    (3, 3, 2),
    (4, 4, 2),
    (5, 5, 1),
    (6, 7, 2),
    (7, 8, 1);

-- Table: training_translation
INSERT INTO training_translation (id, training_id, language_id, title, short_description, description, created_at, updated_at) VALUES
    (1, 1, 1, 'Java algkursus', 'Java programmeerimise alused algajatele.', 'Kursusel õpitakse Java süntaksit, objektorienteeritud programmeerimist ja põhilisi andmestruktuure.', '2026-08-01 09:00:00', '2026-08-01 09:00:00'),
    (2, 1, 2, 'Java Basics', 'Fundamentals of Java programming for beginners.', 'The course covers Java syntax, object-oriented programming, and basic data structures.', '2026-08-01 09:00:00', '2026-08-01 09:00:00'),
    (3, 2, 1, 'Projektijuhtimise põhitõed', 'Sissejuhatus IT-projektijuhtimisse.', 'Kursusel käsitletakse Scrumi, Kanbani ja projekti planeerimise põhimõtteid.', '2026-08-05 10:00:00', '2026-08-05 10:00:00'),
    (4, 2, 2, 'Project Management Fundamentals', 'An introduction to IT project management.', 'The course covers Scrum, Kanban, and the principles of project planning.', '2026-08-05 10:00:00', '2026-08-05 10:00:00'),
    (5, 3, 1, 'Spring Boot veebiarendus', 'REST API-de loomine Spring Booti abil.', 'Kursusel ehitatakse Spring Booti, JPA ja PostgreSQL-i abil töötav REST API.', '2026-08-10 09:00:00', '2026-08-10 09:00:00'),
    (6, 3, 2, 'Spring Boot Web Development', 'Building REST APIs with Spring Boot.', 'The course builds a working REST API using Spring Boot, JPA and PostgreSQL.', '2026-08-10 09:00:00', '2026-08-10 09:00:00'),
    (7, 4, 1, 'Vue.js esmaspetsialist', 'Kaasaegsete veebiliideste loomine Vue 3-ga.', 'Kursusel õpitakse komponente, marsruutimist ja olekuhaldust Vue 3 ja Pinia abil.', '2026-08-12 09:00:00', '2026-08-12 09:00:00'),
    (8, 4, 2, 'Vue.js Essentials', 'Building modern web interfaces with Vue 3.', 'The course covers components, routing and state management with Vue 3 and Pinia.', '2026-08-12 09:00:00', '2026-08-12 09:00:00'),
    (9, 5, 1, 'UX disaini alused', 'Kasutajakeskse disaini põhimõtted.', 'Kursusel käsitletakse kasutajauuringuid, prototüüpimist ja kasutatavuse testimist.', '2026-08-14 09:00:00', '2026-08-14 09:00:00'),
    (10, 5, 2, 'UX Design Basics', 'Principles of user-centred design.', 'The course covers user research, prototyping and usability testing.', '2026-08-14 09:00:00', '2026-08-14 09:00:00'),
    (11, 6, 1, 'Figma praktikum', 'Kasutajaliideste kujundamine Figmas.', 'Praktiline kursus disainisüsteemide ja interaktiivsete prototüüpide loomiseks Figmas.', '2026-08-16 09:00:00', '2026-08-16 09:00:00'),
    (12, 6, 2, 'Figma Workshop', 'Designing user interfaces in Figma.', 'A hands-on course on creating design systems and interactive prototypes in Figma.', '2026-08-16 09:00:00', '2026-08-16 09:00:00'),
    (13, 7, 1, 'Agiilne meeskonnajuhtimine', 'Scrumi meeskonna juhtimine praktikas.', 'Kursusel õpitakse agiilsete meeskondade juhtimist, retrospektiive ja tagasiside andmist.', '2026-08-18 09:00:00', '2026-08-18 09:00:00'),
    (14, 7, 2, 'Agile Team Leadership', 'Leading a Scrum team in practice.', 'The course covers leading agile teams, retrospectives and giving feedback.', '2026-08-18 09:00:00', '2026-08-18 09:00:00'),
    (15, 8, 1, 'SQL ja andmebaasid', 'Relatsiooniliste andmebaaside alused.', 'Kursusel õpitakse SQL päringuid, tabelite disaini ja PostgreSQL-i kasutamist.', '2026-08-20 09:00:00', '2026-08-20 09:00:00'),
    (16, 8, 2, 'SQL and Databases', 'Fundamentals of relational databases.', 'The course covers SQL queries, table design and working with PostgreSQL.', '2026-08-20 09:00:00', '2026-08-20 09:00:00');

-- Table: course
INSERT INTO course (id, training_id, lecturer_id, room_id, number_of_days, number_of_academic_hours, price, status, start_date, end_date, notes, meeting_link, created_at, updated_at, created_by) VALUES
    (1, 1, 1, 1, 5, 40, 490.0000, 'AVA', '2026-10-05', '2026-10-09', 'Kaasa sülearvuti.', NULL, '2026-08-02 10:00:00', '2026-08-02 10:00:00', 1),
    (2, 2, 2, NULL, 3, 24, 350.0000, 'AVA', '2026-11-02', '2026-11-04', NULL, 'https://meet.vali-it.ee/pm-kursus', '2026-08-06 11:00:00', '2026-08-06 11:00:00', 1);

-- Table: course_participant
INSERT INTO course_participant (id, course_id, participant_id, notes, has_paid, requires_laptop, status, created_at, updated_at) VALUES
    (1, 1, 1, 'Registreerus veebilehe kaudu.', true, true, 'REG', '2026-09-10 12:00:00', '2026-09-10 12:00:00');

-- Table: enquiry
INSERT INTO enquiry (id, training_id, profile_id, course_id, option_id, message, company_name, status, created_at, updated_at) VALUES
    (1, 1, 1, 1, 1, 'Huvitab, kas kursusele on veel vabu kohti.', NULL, 'U', '2026-09-15 08:30:00', '2026-09-15 08:30:00'),
    (2, 2, 2, NULL, 2, 'Kas koolitust on võimalik tellida ka ettevõttele?', 'OÜ Näidisfirma', 'U', '2026-09-16 14:20:00', '2026-09-16 14:20:00');

-- Table: certificate_template
INSERT INTO certificate_template (id, course_id, status, created_at, updated_at, created_by) VALUES
    (1, 1, 'AKT', '2026-08-02 10:30:00', '2026-08-02 10:30:00', 1);

-- Table: participant_certificate
INSERT INTO participant_certificate (id, file, participant_id, created_at) VALUES
    (1, ''::bytea, 1, '2026-10-10 12:00:00');

-- Table: newsletter
INSERT INTO newsletter (id, email, first_name, last_name, status) VALUES
    (1, 'uudiskiri1@example.com', 'Liis', 'Org', 'A'),
    (2, 'uudiskiri2@example.com', 'Toomas', 'Vaher', 'A');

-- Table: feedback
INSERT INTO feedback DEFAULT VALUES;
INSERT INTO feedback DEFAULT VALUES;

-- ID-jadade sünkroniseerimine käsitsi sisestatud väärtustega, et järgnevad
-- rakenduse tehtud INSERT laused (ilma id-d määramata) ei põrkaks olemasolevate ID-dega
SELECT setval(pg_get_serial_sequence('role', 'id'), (SELECT MAX(id) FROM role));
SELECT setval(pg_get_serial_sequence('"user"', 'id'), (SELECT MAX(id) FROM "user"));
SELECT setval(pg_get_serial_sequence('language', 'id'), (SELECT MAX(id) FROM language));
SELECT setval(pg_get_serial_sequence('category', 'id'), (SELECT MAX(id) FROM category));
SELECT setval(pg_get_serial_sequence('category_translation', 'id'), (SELECT MAX(id) FROM category_translation));
SELECT setval(pg_get_serial_sequence('funding_type', 'id'), (SELECT MAX(id) FROM funding_type));
SELECT setval(pg_get_serial_sequence('funding_type_translation', 'id'), (SELECT MAX(id) FROM funding_type_translation));
SELECT setval(pg_get_serial_sequence('location', 'id'), (SELECT MAX(id) FROM location));
SELECT setval(pg_get_serial_sequence('lecturer', 'id'), (SELECT MAX(id) FROM lecturer));
SELECT setval(pg_get_serial_sequence('lecturer_translation', 'id'), (SELECT MAX(id) FROM lecturer_translation));
SELECT setval(pg_get_serial_sequence('room', 'id'), (SELECT MAX(id) FROM room));
SELECT setval(pg_get_serial_sequence('option', 'id'), (SELECT MAX(id) FROM option));
SELECT setval(pg_get_serial_sequence('option_translation', 'id'), (SELECT MAX(id) FROM option_translation));
SELECT setval(pg_get_serial_sequence('profile', 'id'), (SELECT MAX(id) FROM profile));
SELECT setval(pg_get_serial_sequence('participant', 'id'), (SELECT MAX(id) FROM participant));
SELECT setval(pg_get_serial_sequence('training', 'id'), (SELECT MAX(id) FROM training));
SELECT setval(pg_get_serial_sequence('training_translation', 'id'), (SELECT MAX(id) FROM training_translation));
SELECT setval(pg_get_serial_sequence('training_funding_type', 'id'), (SELECT MAX(id) FROM training_funding_type));
SELECT setval(pg_get_serial_sequence('course', 'id'), (SELECT MAX(id) FROM course));
SELECT setval(pg_get_serial_sequence('course_participant', 'id'), (SELECT MAX(id) FROM course_participant));
SELECT setval(pg_get_serial_sequence('enquiry', 'id'), (SELECT MAX(id) FROM enquiry));
SELECT setval(pg_get_serial_sequence('certificate_template', 'id'), (SELECT MAX(id) FROM certificate_template));
SELECT setval(pg_get_serial_sequence('participant_certificate', 'id'), (SELECT MAX(id) FROM participant_certificate));
SELECT setval(pg_get_serial_sequence('newsletter', 'id'), (SELECT MAX(id) FROM newsletter));

-- End of file.
