-- ============================================================
-- V0__create_database.sql
-- Création de la base et de l'utilisateur
-- À exécuter UNE SEULE FOIS en tant que superuser (postgres)
-- psql -U postgres -f V0__create_database.sql
-- ============================================================

-- Supprimer la base de données si elle existe (décommentez pour recréer)
-- DROP DATABASE IF EXISTS visa_management;

-- Supprimer l'utilisateur s'il existe (décommentez pour recréer)
-- DROP USER IF EXISTS visa_user;

-- Créer l'utilisateur applicatif
CREATE USER visa_user WITH PASSWORD 'robot123';

-- Créer la base de données
CREATE DATABASE visa_management
    WITH OWNER = visa_user
         ENCODING = 'UTF8'
         LC_COLLATE = 'fr_FR.UTF-8'
         LC_CTYPE   = 'fr_FR.UTF-8'
         TEMPLATE  = template0;

-- Accorder tous les privilèges sur la base de données
GRANT ALL PRIVILEGES ON DATABASE visa_management TO visa_user;

-- Se connecter à la base de données
\c visa_management

-- Accorder tous les privilèges sur le schéma public
GRANT ALL ON SCHEMA public TO visa_user;

-- Définir les privilèges par défaut pour les futurs objets
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL ON TABLES    TO visa_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL ON SEQUENCES TO visa_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL ON FUNCTIONS TO visa_user;
