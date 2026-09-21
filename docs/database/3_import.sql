-- Näidisandmed arenduse ja testimise jaoks
-- Käivitatakse pärast 1_reset_database.sql ja 2_create.sql skripte

-- Table: role
INSERT INTO role (id, name) VALUES
    (1, 'admin'),
    (2, 'participant');

-- Table: category
INSERT INTO category (id, name) VALUES
    (1, 'Programmeerimine'),
    (2, 'Disain'),
    (3, 'Juhtimine');

-- Table: location
INSERT INTO location (id, name, address, is_online) VALUES
    (1, 'Tallinna õppekeskus', 'Sõpruse pst 145, Tallinn', false),
    (2, 'Veebiõpe', 'Veebipõhine koolitus (Zoom)', true);

-- Table: lecturer
INSERT INTO lecturer (id, full_name, bio, photo) VALUES
    (1, 'Mari Tamm', 'Üle 10 aasta kogemust tarkvaraarenduse koolitajana.', ''::bytea),
    (2, 'Jaan Kask', 'IT-projektijuht ja Scrum Master, koolitab meeskonnatöö teemadel.', ''::bytea);

-- Table: room
INSERT INTO room (id, name, status) VALUES
    (1, 'A101', 'VAB'),
    (2, 'A102', 'KIN');

-- Table: option
INSERT INTO option (id, name, type) VALUES
    (1, 'Auditoorne', 'FM'),
    (2, 'Veebipõhine', 'FM');

-- Table: user
INSERT INTO "user" (id, role_id, email, password, status) VALUES
    (1, 1, 'admin@vali-it.ee', 'parool123', 'A'),
    (2, 2, 'kasutaja@vali-it.ee', 'parool123', 'A');

-- Table: profile
INSERT INTO profile (id, first_name, last_name, phone, email) VALUES
    (1, 'Anna', 'Saar', '+37256789012', 'anna.saar@example.com'),
    (2, 'Peeter', 'Mets', '+37251234567', 'peeter.mets@example.com');

-- Table: participant
INSERT INTO participant (id, user_id, name, profile_id) VALUES
    (1, 2, 'Anna Saar', 1);

-- Table: training
INSERT INTO training (id, user_id, default_lecturer_id, category_id, location_id, title, short_description, description, language, status, created_at, updated_at, order_only) VALUES
    (1, 1, 1, 1, 1, 'Java algkursus', 'Java programmeerimise alused algajatele.', 'Kursusel õpitakse Java süntaksit, objektorienteeritud programmeerimist ja põhilisi andmestruktuure.', 'EST', 1, '2026-08-01 09:00:00', '2026-08-01 09:00:00', false),
    (2, 1, 2, 3, 2, 'Projektijuhtimise põhitõed', 'Sissejuhatus IT-projektijuhtimisse.', 'Kursusel käsitletakse Scrumi, Kanbani ja projekti planeerimise põhimõtteid.', 'EST', 1, '2026-08-05 10:00:00', '2026-08-05 10:00:00', false);

-- Table: course
INSERT INTO course (id, training_id, lecturer_id, room_id, number_of_days, number_of_academic_hours, price, status, start_date, end_date, notes, meeting_link) VALUES
    (1, 1, 1, 1, 5, 40, 490.0000, 'AVA', '2026-10-05', '2026-10-09', 'Kaasa sülearvuti.', NULL),
    (2, 2, 2, NULL, 3, 24, 350.0000, 'AVA', '2026-11-02', '2026-11-04', NULL, 'https://meet.vali-it.ee/pm-kursus');

-- Table: course_participant
INSERT INTO course_participant (id, course_id, participant_id, notes, has_paid, requires_laptop, status, created_at, updated_at) VALUES
    (1, 1, 1, 'Registreerus veebilehe kaudu.', true, true, 'REG', '2026-09-10 12:00:00', '2026-09-10 12:00:00');

-- Table: enquiry
INSERT INTO enquiry (id, training_id, profile_id, course_id, option_id, message, company_name, status, created_at, updated_at) VALUES
    (1, 1, 1, 1, 1, 'Huvitab, kas kursusele on veel vabu kohti.', NULL, 'U', '2026-09-15 08:30:00', '2026-09-15 08:30:00'),
    (2, 2, 2, NULL, 2, 'Kas koolitust on võimalik tellida ka ettevõttele?', 'OÜ Näidisfirma', 'U', '2026-09-16 14:20:00', '2026-09-16 14:20:00');

-- Table: certificate_template
INSERT INTO certificate_template (id, course_id, status) VALUES
    (1, 1, 'AKT');

-- Table: participant_certificate
INSERT INTO participant_certificate (id, file, participant_id) VALUES
    (1, ''::bytea, 1);

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
SELECT setval(pg_get_serial_sequence('category', 'id'), (SELECT MAX(id) FROM category));
SELECT setval(pg_get_serial_sequence('location', 'id'), (SELECT MAX(id) FROM location));
SELECT setval(pg_get_serial_sequence('lecturer', 'id'), (SELECT MAX(id) FROM lecturer));
SELECT setval(pg_get_serial_sequence('room', 'id'), (SELECT MAX(id) FROM room));
SELECT setval(pg_get_serial_sequence('option', 'id'), (SELECT MAX(id) FROM option));
SELECT setval(pg_get_serial_sequence('"user"', 'id'), (SELECT MAX(id) FROM "user"));
SELECT setval(pg_get_serial_sequence('profile', 'id'), (SELECT MAX(id) FROM profile));
SELECT setval(pg_get_serial_sequence('participant', 'id'), (SELECT MAX(id) FROM participant));
SELECT setval(pg_get_serial_sequence('training', 'id'), (SELECT MAX(id) FROM training));
SELECT setval(pg_get_serial_sequence('course', 'id'), (SELECT MAX(id) FROM course));
SELECT setval(pg_get_serial_sequence('course_participant', 'id'), (SELECT MAX(id) FROM course_participant));
SELECT setval(pg_get_serial_sequence('enquiry', 'id'), (SELECT MAX(id) FROM enquiry));
SELECT setval(pg_get_serial_sequence('certificate_template', 'id'), (SELECT MAX(id) FROM certificate_template));
SELECT setval(pg_get_serial_sequence('participant_certificate', 'id'), (SELECT MAX(id) FROM participant_certificate));
SELECT setval(pg_get_serial_sequence('newsletter', 'id'), (SELECT MAX(id) FROM newsletter));

-- End of file.
