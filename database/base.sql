
-- Utilisateur applicatif
CREATE USER visa_user WITH PASSWORD 'robot123';

-- Base de données
CREATE DATABASE visa_management
    WITH OWNER     = visa_user
         ENCODING  = 'UTF8'
         LC_COLLATE = 'fr_FR.UTF-8'
         LC_CTYPE   = 'fr_FR.UTF-8'
         TEMPLATE  = template0;

-- Droits sur la base
GRANT ALL PRIVILEGES ON DATABASE visa_management TO visa_user;

-- Connexion à la base pour les droits sur le schéma
\connect visa_management

GRANT ALL ON SCHEMA public TO visa_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL ON TABLES    TO visa_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL ON SEQUENCES TO visa_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL ON FUNCTIONS TO visa_user;

