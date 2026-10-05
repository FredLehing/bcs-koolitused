-- Olemasoleva andmebaasi kommentaariveeru laiendamine.
-- Käivita eraldi, kui andmebaas loodi vana varchar(255) skeemiga.
-- Uuel andmebaasil on sama muudatus juba 2_create.sql failis.
BEGIN;
ALTER TABLE bcs_koolitused.course_participant_feedback
    ALTER COLUMN feedback_text TYPE text;
COMMIT;
-- Rakenduse sisendivalideerimise piir on 10 000 märki ühe kommentaari kohta.
