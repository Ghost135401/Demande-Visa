-- ============================================================
-- V1__init_schema.sql
-- Schéma initial - Gestion des visas transformables
-- Ministère de l'Intérieur - Madagascar
-- ============================================================
-- ============================================================
-- V0__create_database.sql
-- Création de la base et de l'utilisateur
-- À exécuter UNE SEULE FOIS en tant que superuser (postgres)
-- psql -U postgres -f V0__create_database.sql
-- ============================================================

-- ============================================================
-- TABLE : passeport
-- ============================================================
CREATE TABLE passeport (
    id                 BIGSERIAL PRIMARY KEY,
    numero             VARCHAR(50)  NOT NULL UNIQUE,
    date_delivrance    DATE         NOT NULL,
    date_expiration    DATE         NOT NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============================================================
-- TABLE : visa_transformable
-- ============================================================
CREATE TABLE visa_transformable (
    id          BIGSERIAL PRIMARY KEY,
    ref_visa    VARCHAR(100) NOT NULL,
    date_entree DATE         NOT NULL,
    lieu_entree VARCHAR(200) NOT NULL,
    date_sortie DATE         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============================================================
-- TABLE : carte_resident
-- ============================================================
CREATE TABLE carte_resident (
    id               BIGSERIAL PRIMARY KEY,
    numero_carte     VARCHAR(100) NOT NULL UNIQUE,
    date_delivrance  DATE,
    date_expiration  DATE,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- TABLE : demande
-- ============================================================
CREATE TABLE demande (
    id                         BIGSERIAL PRIMARY KEY,
    numero_demande             VARCHAR(50)  NOT NULL UNIQUE,

    -- Catégorie et type
    categorie                  VARCHAR(20)  NOT NULL CHECK (categorie IN ('TRAVAILLEUR','INVESTISSEUR')),
    statut                     VARCHAR(30)  NOT NULL DEFAULT 'DOSSIER_CREE'
                                            CHECK (statut IN ('DOSSIER_CREE','SCAN_TERMINE','VISA_ACCEPTE')),
    type_demande               VARCHAR(30)  NOT NULL CHECK (type_demande IN ('NOUVEAU_TITRE','DUPLICATA','TRANSFERT_VISA')),
    sans_donnees_anterieures   BOOLEAN      NOT NULL DEFAULT FALSE,

    -- État civil (embedded)
    nom                        VARCHAR(100) NOT NULL,
    prenoms                    VARCHAR(200) NOT NULL,
    nom_jeune_fille            VARCHAR(100),
    situation_familiale        VARCHAR(30)  NOT NULL CHECK (situation_familiale IN
                                            ('CELIBATAIRE','MARIE','DIVORCE','VEUF','SEPARE')),
    nationalite                VARCHAR(100) NOT NULL,
    profession                 VARCHAR(100) NOT NULL,
    adresse_madagascar         TEXT         NOT NULL,
    email                      VARCHAR(150) NOT NULL,
    telephone                  VARCHAR(30)  NOT NULL,
    date_naissance             DATE         NOT NULL,
    lieu_naissance             VARCHAR(200),

    -- Relations
    passeport_id               BIGINT       REFERENCES passeport(id),
    visa_transformable_id      BIGINT       REFERENCES visa_transformable(id),
    carte_resident_id          BIGINT       REFERENCES carte_resident(id),

    -- Demande parente (pour duplicata et transfert)
    demande_parente_id         BIGINT       REFERENCES demande(id),

    created_at                 TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at                 TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============================================================
-- TABLE : piece_justificative
-- ============================================================
CREATE TABLE piece_justificative (
    id              BIGSERIAL PRIMARY KEY,
    demande_id      BIGINT       NOT NULL REFERENCES demande(id) ON DELETE CASCADE,
    type_piece      VARCHAR(80)  NOT NULL,
    libelle         VARCHAR(300) NOT NULL,
    est_cochee      BOOLEAN      NOT NULL DEFAULT FALSE,
    est_uploadee    BOOLEAN      NOT NULL DEFAULT FALSE,
    nom_fichier     VARCHAR(300),
    chemin_fichier  VARCHAR(500),
    date_upload     TIMESTAMP,
    obligatoire     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============================================================
-- INDEX
-- ============================================================
CREATE INDEX idx_demande_statut         ON demande(statut);
CREATE INDEX idx_demande_categorie      ON demande(categorie);
CREATE INDEX idx_demande_type           ON demande(type_demande);
CREATE INDEX idx_demande_numero         ON demande(numero_demande);
CREATE INDEX idx_demande_passeport      ON demande(passeport_id);
CREATE INDEX idx_piece_demande          ON piece_justificative(demande_id);
CREATE INDEX idx_piece_type             ON piece_justificative(type_piece);

-- ============================================================
-- FONCTION : mise à jour automatique de updated_at
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_demande_updated_at
    BEFORE UPDATE ON demande
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_passeport_updated_at
    BEFORE UPDATE ON passeport
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_visa_updated_at
    BEFORE UPDATE ON visa_transformable
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_piece_updated_at
    BEFORE UPDATE ON piece_justificative
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_carte_updated_at
    BEFORE UPDATE ON carte_resident
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- SEQUENCE pour numéro de demande (DEM-2024-00001)
-- ============================================================
CREATE SEQUENCE seq_numero_demande START 1 INCREMENT 1;

-- ============================================================
-- TABLE : app_user (utilisateurs pour l'authentification back-office)
-- ============================================================
CREATE TABLE app_user (
    id               BIGSERIAL PRIMARY KEY,
    username         VARCHAR(100) NOT NULL UNIQUE,
    password         VARCHAR(255) NOT NULL,
    nom              VARCHAR(150) NOT NULL,
    prenom           VARCHAR(150),
    role             VARCHAR(30) NOT NULL,
    actif            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_app_user_username ON app_user(username);

CREATE TRIGGER trg_app_user_updated_at
    BEFORE UPDATE ON app_user
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- ADD COLUMN : contenu_fichier pour stocker les fichiers uploadés
-- ============================================================
ALTER TABLE piece_justificative
    ADD COLUMN IF NOT EXISTS contenu_fichier bytea;

-- Ajout de la colonne resident
ALTER TABLE demande
    ADD COLUMN IF NOT EXISTS resident BOOLEAN NOT NULL DEFAULT FALSE;

