-- Kustutab minu_projekt schema (mis põhimõtteliselt kustutab kõik tabelid)
DROP SCHEMA IF EXISTS minu_projekt CASCADE;
-- Loob uue minu_projekt schema vajalikud õigused
CREATE SCHEMA minu_projekt
-- taastab vajalikud andmebaasi õigused
    GRANT ALL ON SCHEMA minu_projekt TO postgres;
GRANT ALL ON SCHEMA minu_projekt TO PUBLIC;