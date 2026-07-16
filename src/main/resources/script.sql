-- =========================
-- ENUM TYPES (idempotent)
-- =========================

DO $$ BEGIN
CREATE TYPE UserRole AS ENUM (
        'SUPER_ADMIN',
        'ADMIN',
        'TEAM_LEADER',
        'SALES_REP'
    );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
CREATE TYPE UserStatus AS ENUM (
        'ACTIVE',
        'INACTIVE',
        'SUSPENDED'
    );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
CREATE TYPE approval_status AS ENUM (
        'PENDING_APPROVAL',
        'ACTIVE',
        'REJECTED'
    );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
CREATE TYPE visit_type AS ENUM (
        'NEW',
        'FOLLOW_UP'
    );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
CREATE TYPE update_request_status AS ENUM (
        'PENDING',
        'APPROVED',
        'REJECTED'
    );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
CREATE TYPE preferred_communication AS ENUM (
        'PHONE',
        'EMAIL',
        'FAX',
        'IN_PERSON'
    );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
CREATE TYPE visit_impression AS ENUM (
        'VERY_POSITIVE',
        'POSITIVE',
        'NEUTRAL',
        'NEGATIVE',
        'VERY_NEGATIVE'
    );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- =========================
-- LOOKUP TABLES
-- =========================

CREATE TABLE IF NOT EXISTS territories (
                                           id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                           name        VARCHAR(100) NOT NULL UNIQUE,
                                           description TEXT
    );

CREATE TABLE IF NOT EXISTS parent_organizations (
                                                    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                    name VARCHAR(150) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS boroughs (
                                        id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        name VARCHAR(50) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS ptoc_locations (
                                              id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                              name VARCHAR(100) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS specialties (
                                           id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                           name VARCHAR(100) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS permissions (
                                           id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                           code        VARCHAR(100) NOT NULL UNIQUE,
                                           description VARCHAR(255),
                                           created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
    );

-- =========================
-- USERS  (matches User entity: role, status, territory,
-- team_leader (self FK), created_by (self FK), permissions M2M)
-- =========================

CREATE TABLE IF NOT EXISTS users (
                                     id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     username       VARCHAR(50)  NOT NULL UNIQUE,
                                     email          VARCHAR(150) NOT NULL UNIQUE,
                                     password_hash  VARCHAR(255) NOT NULL,
                                     role           UserRole   NOT NULL,
                                     status         UserStatus NOT NULL DEFAULT 'ACTIVE',
                                     territory_id   BIGINT REFERENCES territories(id),
                                     team_leader_id BIGINT REFERENCES users(id),
                                     created_by     BIGINT REFERENCES users(id),
                                     created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                                     updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_users_territory_id   ON users(territory_id);
CREATE INDEX IF NOT EXISTS idx_users_team_leader_id  ON users(team_leader_id);

-- =========================
-- ACCOUNTS
-- =========================

CREATE TABLE IF NOT EXISTS accounts (
                                        id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        organization_name        VARCHAR(255) NOT NULL,
                                        parent_organization_id   BIGINT REFERENCES parent_organizations(id),
                                        organization_type        VARCHAR(100),
                                        borough_id               BIGINT NOT NULL REFERENCES boroughs(id),
                                        ptoc_location_id         BIGINT NOT NULL REFERENCES ptoc_locations(id),
                                        address                  VARCHAR(255) NOT NULL,
                                        floor_suite              VARCHAR(50),
                                        zip_code                 VARCHAR(10) NOT NULL,
                                        phone                    VARCHAR(20) NOT NULL,
                                        fax                      VARCHAR(20),
                                        email                    VARCHAR(150),
                                        provides_telehealth      BOOLEAN NOT NULL DEFAULT FALSE,
                                        same_day_walkins         BOOLEAN NOT NULL DEFAULT FALSE,
                                        gatekeeper_name          VARCHAR(150),
                                        gatekeeper_title         VARCHAR(100),
                                        gatekeeper_phone         VARCHAR(20),
                                        gatekeeper_email         VARCHAR(150),
                                        preferred_communication  preferred_communication,
                                        status                   approval_status NOT NULL DEFAULT 'PENDING_APPROVAL',
                                        submitted_by             BIGINT NOT NULL REFERENCES users(id),
                                        approved_by              BIGINT REFERENCES users(id),
                                        created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
                                        updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
                                        CONSTRAINT uq_account_identity
                                        UNIQUE (organization_name, zip_code, address, floor_suite)
    );

CREATE INDEX IF NOT EXISTS idx_accounts_borough_id       ON accounts(borough_id);
CREATE INDEX IF NOT EXISTS idx_accounts_ptoc_location_id ON accounts(ptoc_location_id);
CREATE INDEX IF NOT EXISTS idx_accounts_submitted_by     ON accounts(submitted_by);

-- =========================
-- PHYSICIANS
-- =========================

CREATE TABLE IF NOT EXISTS physicians (
                                          id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                          npi           VARCHAR(10) NOT NULL UNIQUE,
                                          name          VARCHAR(150) NOT NULL,
                                          specialty_id  BIGINT NOT NULL REFERENCES specialties(id),
                                          email         VARCHAR(150),
                                          phone         VARCHAR(20),
                                          status        approval_status NOT NULL DEFAULT 'PENDING_APPROVAL',
                                          submitted_by  BIGINT NOT NULL REFERENCES users(id),
                                          approved_by   BIGINT REFERENCES users(id),
                                          created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                          updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_physicians_specialty_id ON physicians(specialty_id);

-- =========================
-- ACCOUNT PHYSICIANS
-- =========================

CREATE TABLE IF NOT EXISTS account_physicians (
                                                  id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                  account_id       BIGINT NOT NULL REFERENCES accounts(id)   ON DELETE CASCADE,
                                                  physician_id     BIGINT NOT NULL REFERENCES physicians(id) ON DELETE CASCADE,
                                                  date_first_seen  DATE NOT NULL DEFAULT CURRENT_DATE,
                                                  CONSTRAINT uq_account_physician UNIQUE (account_id, physician_id)
    );

-- =========================
-- USER PERMISSIONS
-- =========================

CREATE TABLE IF NOT EXISTS user_permissions (
                                                user_id       BIGINT NOT NULL REFERENCES users(id)       ON DELETE CASCADE,
                                                permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
                                                PRIMARY KEY (user_id, permission_id)
    );

-- =========================
-- VISITS
-- =========================

CREATE TABLE IF NOT EXISTS visits (
                                      id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      account_id         BIGINT NOT NULL REFERENCES accounts(id),
                                      visit_date         DATE NOT NULL,
                                      visit_type         visit_type NOT NULL,
                                      visitor_id         BIGINT NOT NULL REFERENCES users(id),
                                      joined_visit       BOOLEAN NOT NULL DEFAULT FALSE,
                                      joined_visitor_id  BIGINT REFERENCES users(id),
                                      visit_impression   visit_impression NOT NULL,
                                      materials_shared   TEXT[],
                                      notes              TEXT,
                                      next_visit_date    DATE,
                                      status             approval_status NOT NULL DEFAULT 'PENDING_APPROVAL',
                                      rejection_reason   TEXT,
                                      approved_by        BIGINT REFERENCES users(id),
                                      reviewed_at        TIMESTAMPTZ,
                                      created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
                                      CONSTRAINT chk_joined_visitor
                                      CHECK (
                                        (joined_visit = TRUE  AND joined_visitor_id IS NOT NULL) OR
                                        (joined_visit = FALSE AND joined_visitor_id IS NULL)
                                      )
    );

CREATE INDEX IF NOT EXISTS idx_visits_account_id ON visits(account_id);
CREATE INDEX IF NOT EXISTS idx_visits_visitor_id ON visits(visitor_id);

-- =========================
-- VISIT PHYSICIANS
-- =========================

CREATE TABLE IF NOT EXISTS visit_physicians (
                                                id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                visit_id      BIGINT NOT NULL REFERENCES visits(id)     ON DELETE CASCADE,
                                                physician_id  BIGINT NOT NULL REFERENCES physicians(id) ON DELETE CASCADE,
                                                CONSTRAINT uq_visit_physician UNIQUE (visit_id, physician_id)
    );

-- BOROUGHS
INSERT INTO boroughs (id, name) OVERRIDING SYSTEM VALUE VALUES
                                                            (1, 'Manhattan'),
                                                            (2, 'Brooklyn'),
                                                            (3, 'Queens'),
                                                            (4, 'Bronx'),
                                                            (5, 'Staten Island')
    ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

SELECT setval(pg_get_serial_sequence('boroughs', 'id'), (SELECT MAX(id) FROM boroughs));

-- PTOC_LOCATIONS
INSERT INTO ptoc_locations (id, name) OVERRIDING SYSTEM VALUE VALUES
                                                                  (1, 'Upper East Side Clinic'),
                                                                  (2, 'Downtown Brooklyn Clinic'),
                                                                  (3, 'Flushing Clinic')
    ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

SELECT setval(pg_get_serial_sequence('ptoc_locations', 'id'), (SELECT MAX(id) FROM ptoc_locations));

-- SPECIALTIES
INSERT INTO specialties (id, name) OVERRIDING SYSTEM VALUE VALUES
                                                               (1, 'Internal Medicine'),
                                                               (2, 'Cardiology'),
                                                               (3, 'Orthopedics'),
                                                               (4, 'Pediatrics'),
                                                               (5, 'Family Medicine'),
                                                               (6, 'Dermatology')
    ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

SELECT setval(pg_get_serial_sequence('specialties', 'id'), (SELECT MAX(id) FROM specialties));

-- PARENT_ORGANIZATIONS
INSERT INTO parent_organizations (id, name) OVERRIDING SYSTEM VALUE VALUES
                                                                        (1, 'NYC Health + Hospitals'),
                                                                        (2, 'Mount Sinai Health System'),
                                                                        (3, 'Wellness Partners'),
                                                                        (4, 'Northwell Health')
    ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

SELECT setval(pg_get_serial_sequence('parent_organizations', 'id'), (SELECT MAX(id) FROM parent_organizations));

-- PERMISSIONS (only the codes referenced by PERMISSION_CODES)
INSERT INTO permissions (code, description) VALUES
                                                ('VISIT_CREATE_NEW',       'Create new visit'),
                                                ('VISIT_CREATE_FOLLOWUP',  'Create follow-up visit'),
                                                ('VISIT_VIEW_OWN',         'View own visits'),
                                                ('VISIT_VIEW_TEAM',        'View visits of own team (team leader scope)'),
                                                ('VISIT_VIEW_ALL',         'View all visits'),
                                                ('ACCOUNT_EDIT',           'Edit account'),
                                                ('UPDATE_REQUEST_APPROVE', 'Approve/reject update request'),
                                                ('USER_MANAGE',            'Create/edit/deactivate users'),
                                                ('PERMISSION_ASSIGN',      'Assign permissions to users')
    ON CONFLICT (code) DO NOTHING;