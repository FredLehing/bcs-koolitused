-- Kustutab minu_projekt schema (mis põhimõtteliselt kustutab kõik tabelid)
DROP SCHEMA IF EXISTS bcs_koolitused CASCADE;
-- Loob uue minu_projekt schema vajalikud õigused
CREATE SCHEMA bcs_koolitused
-- taastab vajalikud andmebaasi õigused
    GRANT ALL ON SCHEMA bcs_koolitused TO postgres;
GRANT ALL ON SCHEMA bcs_koolitused TO PUBLIC;