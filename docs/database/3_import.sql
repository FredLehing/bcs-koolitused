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
    (3, 1, 'admin', '123', 'A', '2026-09-28 00:00:00'), -- lihtne testkonto (admin)
    -- osalejate kasutajad (igal osalejal oma kasutaja, participant.user_id on unikaalne); kasutaja 2 = Anna Saar
    (4, 2, 'liis.kuusk@example.com', 'parool123', 'A', '2026-09-12 10:00:00'),
    (5, 2, 'jaan.org@example.com', 'parool123', 'A', '2026-09-14 15:30:00'),
    (6, 2, 'mari.lepp@example.com', 'parool123', 'A', '2026-09-18 09:10:00'),
    (7, 2, 'toomas.rebane@example.com', 'parool123', 'A', '2026-09-22 13:45:00');

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
INSERT INTO lecturer (id, full_name, status, created_at, updated_at, created_by) VALUES
    (1, 'Rain Tüür', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (2, 'Merje Vaide', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (3, 'Kersti Laidvee', 'A', '2026-09-20 10:00:00', '2026-09-20 10:00:00', 1),
    (4, 'Virve Räni', 'D', '2026-07-20 09:00:00', '2026-09-01 16:00:00', 1),
    (5, 'Tarmo Rosenfeldt', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (6, 'Margus Sakk', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (7, 'Andres Liitmaa', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (8, 'Meelis Teern', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (9, 'Tarmo Kallas', 'A', '2026-09-22 11:00:00', '2026-09-22 11:00:00', 1);

-- Table: lecturer_translation (title = amet, short_description = peamised spetsialiseerumised)
INSERT INTO lecturer_translation (id, lecturer_id, language_id, title, short_description, description, created_at, updated_at) VALUES
    (1, 1, 1, 'Lektor/konsultant', 'Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programm.', '<p>Tarkvaraarendus, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programm.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (2, 1, 2, 'Lecturer/consultant', 'Software development, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programme.', '<p>Software development, Java, HTML, CSS, JavaScript, SQL, Git, Spring Boot, REST API, PostgreSQL, JPA (Hibernate), MapStruct, JUnit, Swagger, Gradle, Confluence, Jira. Vali Tarkvaraarendus! programme.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (3, 2, 1, 'Projektijuht/lektor', 'Microsoft Office rakendused, andmeanalüüs, tarkvaraarendus, veebiarendus. Täiskasvanute koolitaja tase 6.', '<p>Microsoft Office rakendused, andmeanalüüs, tarkvaraarendus, veebiarendus. Täiskasvanute koolitaja tase 6.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (4, 2, 2, 'Project manager/lecturer', 'Microsoft Office applications, data analysis, software development, web development. Adult educator, level 6.', '<p>Microsoft Office applications, data analysis, software development, web development. Adult educator, level 6.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (5, 3, 1, 'Lektor/konsultant', 'Adobe Photoshop, Illustrator, InDesign, Acrobat, Canva, Figma, e-turundus, e-õppe disain, Office rakendused.', '<p>Adobe Photoshop, Illustrator, InDesign, Acrobat, Canva, Figma, e-turundus, e-õppe disain, Office rakendused.</p>', '2026-09-20 10:00:00', '2026-09-20 10:00:00'),
    (6, 4, 1, 'Lektor/konsultant', 'Andmeanalüüs, Power BI, SQL, Tableau, SAP BO, Excel, Vali Andmetarkus! programm.', '<p>Andmeanalüüs, Power BI, SQL, Tableau, SAP BO, Excel, Vali Andmetarkus! programm.</p>', '2026-07-20 09:00:00', '2026-07-20 09:00:00'),
    (7, 5, 1, 'Lektor/konsultant', 'Microsoft 365, MS Teams, SharePoint, Planner, Viva Goals, Office rakendused, küberturvalisus. Täiskasvanute koolitaja tase 6.', '<p>Microsoft 365, MS Teams, SharePoint, Planner, Viva Goals, Office rakendused, küberturvalisus. Täiskasvanute koolitaja tase 6.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (8, 5, 2, 'Lecturer/consultant', 'Microsoft 365, MS Teams, SharePoint, Planner, Viva Goals, Office applications, cybersecurity. Adult educator, level 6.', '<p>Microsoft 365, MS Teams, SharePoint, Planner, Viva Goals, Office applications, cybersecurity. Adult educator, level 6.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (9, 6, 1, 'Lektor/konsultant', 'Power BI, Bizagi, Power Automate, Access, Visio, Office rakendused, kasutajakoolitused ja andmeanalüüs.', '<p>Power BI, Bizagi, Power Automate, Access, Visio, Office rakendused, kasutajakoolitused ja andmeanalüüs.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (10, 6, 2, 'Lecturer/consultant', 'Power BI, Bizagi, Power Automate, Access, Visio, Office applications, user training and data analysis.', '<p>Power BI, Bizagi, Power Automate, Access, Visio, Office applications, user training and data analysis.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (11, 7, 1, 'Spetsialisti valdkonna lektor/konsultant', 'Microsofti ametlikud sertifitseerimiskoolitused (MOC), serverid, pilvetehnoloogiad, Microsoft Certified Trainer.', '<p>Microsofti ametlikud sertifitseerimiskoolitused (MOC), serverid, pilvetehnoloogiad, Microsoft Certified Trainer.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (12, 7, 2, 'Specialist lecturer/consultant', 'Official Microsoft certification courses (MOC), servers, cloud technologies, Microsoft Certified Trainer.', '<p>Official Microsoft certification courses (MOC), servers, cloud technologies, Microsoft Certified Trainer.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (13, 8, 1, 'Lektor/konsultant', 'Tarkvaraarendus, Java, JavaScript, HTML, CSS, Vue, Angular, Vali Tarkvaraarendus! programm.', '<p>Tarkvaraarendus, Java, JavaScript, HTML, CSS, Vue, Angular, Vali Tarkvaraarendus! programm.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (14, 8, 2, 'Lecturer/consultant', 'Software development, Java, JavaScript, HTML, CSS, Vue, Angular, Vali Tarkvaraarendus! programme.', '<p>Software development, Java, JavaScript, HTML, CSS, Vue, Angular, Vali Tarkvaraarendus! programme.</p>', '2026-07-15 09:00:00', '2026-07-15 09:00:00'),
    (15, 9, 1, 'Lektor/konsultant', 'SharePoint, Power Platform, veebiarendus, UX (kasutajakogemus), veebiliideste arendus.', '<p>SharePoint, Power Platform, veebiarendus, UX (kasutajakogemus), veebiliideste arendus.</p>', '2026-09-22 11:00:00', '2026-09-22 11:00:00');

-- Table: lecturer_photo (Rain Tüüri foto docs/mock-wireframe/lecturer-photos/rain-tuur.jpg, 200×200 JPEG; teistel pilti pole)
INSERT INTO lecturer_photo (id, lecturer_id, photo, content_type, created_at, updated_at) VALUES
    (1, 1, decode('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', 'base64'), 'image/jpeg', '2026-07-15 09:00:00', '2026-07-15 09:00:00');

-- Table: room (Bremeni kustutatud — näide "Näita kustutatud" lüliti jaoks; toimumiskorrad ainult minevikus)
INSERT INTO room (id, name, status, created_at, updated_at, created_by) VALUES
    (1, 'Assauwe', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (2, 'Bremeni', 'D', '2026-07-15 09:00:00', '2026-09-01 10:00:00', 1),
    (3, 'Eppingi', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (4, 'Hellemanni', 'A', '2026-07-15 09:00:00', '2026-09-20 10:00:00', 1),
    (5, 'Landskrone', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1),
    (6, 'Megede', 'A', '2026-07-15 09:00:00', '2026-07-15 09:00:00', 1);

-- Table: profile
INSERT INTO profile (id, first_name, last_name, phone, email, created_at, updated_at) VALUES
    (1, 'Anna', 'Saar', '+37256789012', 'anna.saar@example.com', '2026-09-05 09:00:00', '2026-09-05 09:00:00'),
    (2, 'Peeter', 'Mets', '+37251234567', 'peeter.mets@example.com', '2026-09-16 14:15:00', '2026-09-16 14:15:00'),
    (3, 'Kadri', 'Tamm', '+37255512345', 'kadri.tamm@example.com', '2026-09-20 11:05:00', '2026-09-20 11:05:00'),
    (4, 'Martin', 'Kask', '+37253344556', 'martin.kask@example.com', '2026-09-28 16:40:00', '2026-09-28 16:40:00'),
    (5, 'Liis', 'Kuusk', '+37255001122', 'liis.kuusk@example.com', '2026-09-12 10:00:00', '2026-09-12 10:00:00'),
    (6, 'Jaan', 'Org', '+37255003344', 'jaan.org@example.com', '2026-09-14 15:30:00', '2026-09-14 15:30:00'),
    (7, 'Mari', 'Lepp', '+37255005566', 'mari.lepp@example.com', '2026-09-18 09:10:00', '2026-09-18 09:10:00'),
    (8, 'Toomas', 'Rebane', '+37255007788', 'toomas.rebane@example.com', '2026-09-22 13:45:00', '2026-09-22 13:45:00');

-- Table: participant
INSERT INTO participant (id, user_id, name, profile_id, created_at) VALUES
    (1, 2, 'Anna Saar', 1, '2026-09-05 09:00:00'),
    (2, 4, 'Liis Kuusk', 5, '2026-09-12 10:00:00'),
    (3, 5, 'Jaan Org', 6, '2026-09-14 15:30:00'),
    (4, 6, 'Mari Lepp', 7, '2026-09-18 09:10:00'),
    (5, 7, 'Toomas Rebane', 8, '2026-09-22 13:45:00');

-- Table: training
INSERT INTO training (id, user_id, category_id, training_language_id, location_id, status, created_at, updated_at, is_orderable, is_promoted) VALUES
    (1, 1, 1, 1, 1, 'P', '2026-08-01 09:00:00', '2026-08-01 09:00:00', true, true),
    (2, 1, 3, 1, 2, 'P', '2026-08-05 10:00:00', '2026-08-05 10:00:00', false, false),
    (3, 1, 1, 1, 2, 'P', '2026-08-10 09:00:00', '2026-09-21 14:30:00', true, false),
    (4, 1, 1, 2, 2, 'P', '2026-08-12 09:00:00', '2026-08-12 09:00:00', true, true),
    (5, 1, 2, 1, 1, 'P', '2026-08-14 09:00:00', '2026-08-14 09:00:00', true, false),
    (6, 1, 2, 2, 3, 'P', '2026-08-16 09:00:00', '2026-08-16 09:00:00', false, false),
    (7, 1, 3, 1, 3, 'P', '2026-08-18 09:00:00', '2026-08-18 09:00:00', true, false),
    (8, 1, 1, 1, 1, 'P', '2026-08-20 09:00:00', '2026-08-20 09:00:00', true, false),
    -- AdminTrainingsView näidised: mustandid (U), kustutatud (D), puuduvad tõlked, õppekeel ru, teine lehekülg
    (9, 1, 1, 3, 2, 'U', '2026-09-02 11:00:00', '2026-09-02 11:00:00', true, false),
    (10, 1, 1, 1, 2, 'U', '2026-09-10 13:20:00', '2026-09-10 13:20:00', true, false),
    (11, 1, 1, 1, 2, 'P', '2026-09-14 09:30:00', '2026-09-14 09:30:00', true, false),
    (12, 1, 1, 2, 2, 'U', '2026-09-18 10:00:00', '2026-09-18 10:00:00', false, false),
    (13, 1, 1, 1, 3, 'P', '2026-09-25 15:10:00', '2026-09-26 09:00:00', true, true),
    (14, 1, 2, 1, 1, 'D', '2026-08-25 12:00:00', '2026-09-08 17:40:00', false, false);

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
    (7, 8, 1),
    (8, 10, 2),
    (9, 11, 1),
    (10, 13, 1),
    (11, 13, 2);

-- Table: training_lecturer (koolituse koolitajad; sort_order = kuvamise järjekord)
INSERT INTO training_lecturer (id, training_id, lecturer_id, sort_order) VALUES
    (1, 1, 1, 1),
    (2, 1, 8, 2),
    (3, 2, 2, 1),
    (4, 3, 1, 1),
    (5, 4, 8, 1),
    (6, 5, 9, 1),
    (7, 6, 3, 1),
    (8, 7, 2, 1),
    (9, 8, 1, 1),
    (10, 9, 6, 1),
    (11, 9, 2, 2),
    (12, 10, 7, 1),
    (13, 11, 8, 1),
    (14, 11, 1, 2),
    (15, 12, 6, 1),
    (16, 13, 5, 1),
    (17, 14, 3, 1);

-- Table: training_translation
INSERT INTO training_translation (id, training_id, language_id, title, short_description, description, created_at, updated_at) VALUES
    (1, 1, 1, 'Java algkursus', 'Java programmeerimise alused algajatele.', 'Kursusel õpitakse Java süntaksit, objektorienteeritud programmeerimist ja põhilisi andmestruktuure.', '2026-08-01 09:00:00', '2026-09-29 10:15:00'),
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
    (16, 8, 2, 'SQL and Databases', 'Fundamentals of relational databases.', 'The course covers SQL queries, table design and working with PostgreSQL.', '2026-08-20 09:00:00', '2026-08-20 09:00:00'),
    (17, 9, 1, 'Exceli algkursus', 'Tabelid, valemid ja diagrammid algajatele.', 'Kursusel õpitakse töötama tabelitega, kasutama põhilisi valemeid ja looma diagramme.', '2026-09-02 11:00:00', '2026-09-02 11:00:00'),
    (18, 10, 1, 'Docker ja konteinerid', 'Rakenduste pakkimine ja käivitamine konteinerites.', 'Kursusel õpitakse looma Dockerfile''e, haldama konteinereid ja kasutama Docker Compose''i.', '2026-09-10 13:20:00', '2026-09-12 08:45:00'),
    (19, 11, 1, 'Git ja GitHub', 'Versioonihaldus meeskonnatöös.', 'Kursusel õpitakse Giti põhikäske, harude kasutamist ja koostööd GitHubis.', '2026-09-14 09:30:00', '2026-09-14 09:30:00'),
    (20, 11, 2, 'Git and GitHub', 'Version control for teamwork.', 'The course covers basic Git commands, working with branches and collaborating on GitHub.', '2026-09-14 09:30:00', '2026-09-15 16:00:00'),
    (21, 12, 1, 'Python andmeanalüüsiks', 'Andmete töötlemine ja visualiseerimine Pythoniga.', 'Kursusel õpitakse kasutama pandas ja matplotlib teeke andmete analüüsimiseks.', '2026-09-18 10:00:00', '2026-09-18 10:00:00'),
    (22, 12, 2, 'Python for Data Analysis', 'Processing and visualising data with Python.', 'The course covers using the pandas and matplotlib libraries for data analysis.', '2026-09-18 10:00:00', '2026-09-18 10:00:00'),
    (23, 13, 1, 'Tehisaru töövahendid arendajale', 'AI-abilised igapäevases tarkvaraarenduses.', 'Kursusel õpitakse kasutama AI-abilisi koodi kirjutamisel, testimisel ja dokumenteerimisel.', '2026-09-25 15:10:00', '2026-09-25 15:10:00'),
    (24, 14, 1, 'Photoshopi algkursus', 'Pilditöötluse alused Photoshopis.', 'Kursusel õpitakse kihtide, maskide ja põhiliste pilditöötlusvahendite kasutamist.', '2026-08-25 12:00:00', '2026-08-25 12:00:00'),
    (25, 14, 2, 'Photoshop Basics', 'Fundamentals of image editing in Photoshop.', 'The course covers layers, masks and the basic image editing tools.', '2026-08-25 12:00:00', '2026-08-25 12:00:00');

-- Table: course
INSERT INTO course (id, training_id, room_id, number_of_days, number_of_academic_hours, price, status, start_date, end_date, notes, meeting_link, is_promoted, created_at, updated_at, created_by) VALUES
    (1, 1, 1, 5, 40, 490.0000, 'O', '2026-10-05', '2026-10-09', 'Kaasa sülearvuti.', NULL, true, '2026-08-02 10:00:00', '2026-08-02 10:00:00', 1),
    (2, 2, NULL, 3, 24, 350.0000, 'O', '2026-11-02', '2026-11-04', NULL, 'https://meet.vali-it.ee/pm-kursus', false, '2026-08-06 11:00:00', '2026-08-06 11:00:00', 1),
    -- Koolituse kalendri näidised (koolitus 1): möödunud (3, 7), tühistatud (4), täis ja veebis (5), mustand ilma koolitajata (6), kustutatud (8)
    (3, 1, 1, 5, 40, 490.0000, 'O', '2026-09-07', '2026-09-11', 'Grupp oli täis, järgmine kord suurem ruum.', NULL, false, '2026-08-15 10:00:00', '2026-08-15 10:00:00', 1),
    (4, 1, 2, 5, 40, 490.0000, 'X', '2026-10-19', '2026-10-23', 'Tühistatud koolitaja haiguse tõttu.', NULL, false, '2026-08-15 10:00:00', '2026-08-15 10:00:00', 1),
    (5, 1, NULL, 5, 40, 520.0000, 'F', '2026-11-16', '2026-11-20', NULL, 'https://meet.vali-it.ee/java-nov', false, '2026-08-15 10:00:00', '2026-08-15 10:00:00', 1),
    (6, 1, 1, 5, 40, 520.0000, 'U', '2026-12-07', '2026-12-11', NULL, NULL, false, '2026-08-15 10:00:00', '2026-08-15 10:00:00', 1),
    (7, 1, 2, 5, 40, 450.0000, 'O', '2026-06-08', '2026-06-12', NULL, 'https://meet.vali-it.ee/java-jun', false, '2026-05-15 10:00:00', '2026-05-15 10:00:00', 1),
    (8, 1, 1, 5, 40, 490.0000, 'D', '2026-11-30', '2026-12-04', NULL, NULL, false, '2026-08-15 10:00:00', '2026-08-15 10:00:00', 1),
    -- Toimumiskordade kalendri näidised: esile tõstetud kohapeal (9), veebis õppekeel en (10), hübriid esile tõstetud (11), täis (12), mustand (13)
    (9, 3, 3, 4, 32, 560.0000, 'O', '2026-10-12', '2026-10-15', NULL, NULL, true, '2026-09-01 10:00:00', '2026-09-01 10:00:00', 1),
    (10, 4, NULL, 3, 24, 420.0000, 'O', '2026-10-26', '2026-10-28', NULL, 'https://meet.vali-it.ee/vue-okt', false, '2026-09-01 10:00:00', '2026-09-01 10:00:00', 1),
    (11, 5, 4, 2, 16, 300.0000, 'O', '2026-11-09', '2026-11-10', NULL, 'https://meet.vali-it.ee/ux-nov', true, '2026-09-01 10:00:00', '2026-09-01 10:00:00', 1),
    (12, 7, 5, 2, 16, 280.0000, 'F', '2026-10-14', '2026-10-15', NULL, NULL, false, '2026-09-01 10:00:00', '2026-09-01 10:00:00', 1),
    (13, 2, NULL, 3, 24, 350.0000, 'U', '2027-01-11', '2027-01-13', NULL, NULL, false, '2026-09-01 10:00:00', '2026-09-01 10:00:00', 1);

-- Table: course_lecturer (toimumiskorra koolitajad; sort_order = kuvamise järjekord)
INSERT INTO course_lecturer (id, course_id, lecturer_id, sort_order) VALUES
    (1, 1, 1, 1),
    (2, 1, 8, 2),
    (3, 2, 2, 1),
    (4, 3, 1, 1),
    (5, 4, 8, 1),
    (6, 5, 1, 1),
    (7, 7, 8, 1),
    (8, 8, 1, 1),
    (9, 9, 1, 1),
    (10, 10, 8, 1),
    (11, 11, 9, 1),
    (12, 12, 2, 1);

-- Table: course_participant (status: R = registreerunud, C = loobunud)
INSERT INTO course_participant (id, course_id, participant_id, notes, admin_notes, has_paid, requires_laptop, status, created_at, updated_at) VALUES
    (1, 1, 1, 'Registreerus veebilehe kaudu.', NULL, true, true, 'R', '2026-09-10 12:00:00', '2026-09-10 12:00:00'),
    (2, 3, 1, 'Osales septembris.', NULL, true, true, 'R', '2026-09-01 12:00:00', '2026-09-01 12:00:00'),
    (3, 1, 2, '', NULL, false, false, 'R', '2026-09-12 10:05:00', '2026-09-12 10:05:00'),
    (4, 1, 3, 'Arve ettevõttele.', 'Arve saadetud 15.09.', true, true, 'R', '2026-09-14 15:35:00', '2026-09-14 15:35:00'),
    (5, 1, 4, 'Loobus haiguse tõttu.', 'Teatas telefoni teel 25.09.', false, true, 'C', '2026-09-18 09:15:00', '2026-09-25 11:00:00'),
    (6, 12, 2, '', NULL, true, false, 'R', '2026-09-20 08:00:00', '2026-09-20 08:00:00'),
    (7, 12, 5, '', NULL, false, false, 'R', '2026-09-22 13:50:00', '2026-09-22 13:50:00'),
    (8, 5, 3, '', NULL, false, true, 'R', '2026-09-26 17:00:00', '2026-09-26 17:00:00'),
    (9, 9, 5, '', NULL, true, true, 'R', '2026-09-28 09:00:00', '2026-09-28 09:00:00'),
    -- Anna Saar osales juunis Java algkursusel (toimumiskord 7, veebis); tagasiside olemas (feedback 1)
    (10, 7, 1, '', NULL, true, false, 'R', '2026-05-20 10:00:00', '2026-05-20 10:00:00');

-- Table: enquiry (status: U = uus, H = käsitletud)
INSERT INTO enquiry (id, training_id, profile_id, course_id, message, company_name, status, created_at, updated_at) VALUES
    (1, 1, 1, 1, 'Huvitab, kas kursusele on veel vabu kohti.', NULL, 'U', '2026-09-15 08:30:00', '2026-09-15 08:30:00'),
    (2, 2, 2, NULL, 'Kas koolitust on võimalik tellida ka ettevõttele?', 'OÜ Näidisfirma', 'U', '2026-09-16 14:20:00', '2026-09-16 14:20:00'),
    (3, 3, 3, NULL, 'Soovime koolitust kaheksale arendajale meie kontoris, eelistatavalt novembris.', 'AS Tarkvaramaja', 'H', '2026-09-20 11:05:00', '2026-09-22 09:00:00'),
    (4, 1, 4, 5, 'Kas veebis osalejad saavad hiljem ka salvestust vaadata?', NULL, 'U', '2026-09-28 16:40:00', '2026-09-28 16:40:00'),
    (5, 7, 7, 12, 'Kas järgmisele korrale saab juba registreeruda?', NULL, 'H', '2026-09-24 10:20:00', '2026-09-25 09:00:00');

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

-- Table: feedback_criteria (status: A = aktiivne, D = kustutatud)
INSERT INTO feedback_criteria (id, sequence, status, created_at, updated_at) VALUES
    (1, 1, 'A', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    (2, 2, 'A', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    (3, 3, 'A', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    (4, 4, 'A', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    (5, 5, 'A', '2026-05-01 09:00:00', '2026-05-01 09:00:00');

-- Table: feedback_criteria_translation (language_id: 1 = et, 2 = en)
INSERT INTO feedback_criteria_translation (id, feedback_criteria_id, language_id, title, description, created_at, updated_at) VALUES
    (1, 1, 1, 'Koolitus vastas ootustele', 'Koolituse sisu, tase ja maht vastasid koolituse kirjelduse põhjal tekkinud ootustele.', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    (2, 1, 2, 'The training met my expectations', 'The content, level and scope matched the expectations set by the training description.', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    (3, 2, 1, 'Koolitaja oli pädev', 'Koolitaja valdas teemat põhjalikult ning selgitas seda arusaadavalt ja näidetega.', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    (4, 2, 2, 'The trainer was competent', 'The trainer had a thorough command of the subject and explained it clearly with examples.', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    (5, 3, 1, 'Õppematerjalid olid asjakohased', 'Õppematerjalid ja harjutused toetasid teema omandamist ning on kasutatavad ka pärast koolitust.', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    (6, 3, 2, 'The materials were relevant', 'The materials and exercises supported learning and remain useful after the training.', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    (7, 4, 1, 'Õpikeskkond ja korraldus olid sobivad', 'Koolitusruum või veebikeskkond, ajakava ja info edastamine toetasid õppimist.', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    (8, 4, 2, 'Venue and organisation were suitable', 'The training room or online environment, schedule and communication supported learning.', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    (9, 5, 1, 'Soovitaksin koolitust kolleegidele', 'Julgeksin seda koolitust soovitada kolleegidele või teistele samade vajadustega inimestele.', '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    (10, 5, 2, 'I would recommend this training to colleagues', 'I would recommend this training to colleagues or others with similar needs.', '2026-05-01 09:00:00', '2026-05-01 09:00:00');

-- Table: feedback (status: N = uus, U = muudetud pärast ülevaatust, H = üle vaadatud)
INSERT INTO feedback (id, course_participant_id, status, created_at, updated_at) VALUES
    (1, 10, 'H', '2026-06-12 16:40:00', '2026-06-15 09:00:00');

-- Table: course_participant_feedback
INSERT INTO course_participant_feedback (id, feedback_id, feedback_criteria_id, score, feedback_text, created_at, updated_at) VALUES
    (1, 1, 1, 9, NULL, '2026-06-12 16:40:00', '2026-06-12 16:40:00'),
    (2, 1, 2, 10, 'Selged selgitused ja palju praktilisi näiteid.', '2026-06-12 16:40:00', '2026-06-12 16:40:00'),
    (3, 1, 3, 8, NULL, '2026-06-12 16:40:00', '2026-06-12 16:40:00'),
    (4, 1, 4, 7, 'Veebikeskkonnas oli esimesel päeval helikvaliteet kõikuv.', '2026-06-12 16:40:00', '2026-06-12 16:40:00'),
    (5, 1, 5, 9, NULL, '2026-06-12 16:40:00', '2026-06-12 16:40:00');

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
SELECT setval(pg_get_serial_sequence('lecturer_photo', 'id'), (SELECT MAX(id) FROM lecturer_photo));
SELECT setval(pg_get_serial_sequence('room', 'id'), (SELECT MAX(id) FROM room));
SELECT setval(pg_get_serial_sequence('profile', 'id'), (SELECT MAX(id) FROM profile));
SELECT setval(pg_get_serial_sequence('participant', 'id'), (SELECT MAX(id) FROM participant));
SELECT setval(pg_get_serial_sequence('training', 'id'), (SELECT MAX(id) FROM training));
SELECT setval(pg_get_serial_sequence('training_translation', 'id'), (SELECT MAX(id) FROM training_translation));
SELECT setval(pg_get_serial_sequence('training_funding_type', 'id'), (SELECT MAX(id) FROM training_funding_type));
SELECT setval(pg_get_serial_sequence('training_lecturer', 'id'), (SELECT MAX(id) FROM training_lecturer));
SELECT setval(pg_get_serial_sequence('course', 'id'), (SELECT MAX(id) FROM course));
SELECT setval(pg_get_serial_sequence('course_lecturer', 'id'), (SELECT MAX(id) FROM course_lecturer));
SELECT setval(pg_get_serial_sequence('course_participant', 'id'), (SELECT MAX(id) FROM course_participant));
SELECT setval(pg_get_serial_sequence('feedback_criteria', 'id'), (SELECT MAX(id) FROM feedback_criteria));
SELECT setval(pg_get_serial_sequence('feedback_criteria_translation', 'id'), (SELECT MAX(id) FROM feedback_criteria_translation));
SELECT setval(pg_get_serial_sequence('feedback', 'id'), (SELECT MAX(id) FROM feedback));
SELECT setval(pg_get_serial_sequence('course_participant_feedback', 'id'), (SELECT MAX(id) FROM course_participant_feedback));
SELECT setval(pg_get_serial_sequence('enquiry', 'id'), (SELECT MAX(id) FROM enquiry));
SELECT setval(pg_get_serial_sequence('certificate_template', 'id'), (SELECT MAX(id) FROM certificate_template));
SELECT setval(pg_get_serial_sequence('participant_certificate', 'id'), (SELECT MAX(id) FROM participant_certificate));
SELECT setval(pg_get_serial_sequence('newsletter', 'id'), (SELECT MAX(id) FROM newsletter));

-- End of file.
