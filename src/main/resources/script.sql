-- =========================
-- ENUM TYPES
-- =========================

CREATE TYPE user_role AS ENUM (
    'SUPER_ADMIN',
	'ADMIN',
	'TEAM_LEADER',
    'SALES_REP'
);


CREATE TYPE user_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'SUSPENDED'
);

CREATE TYPE approval_status AS ENUM (
    'PENDING_APPROVAL',
    'ACTIVE',
    'REJECTED'
);

CREATE TYPE visit_type AS ENUM (
    'NEW',
    'FOLLOW_UP'
);

CREATE TYPE update_entity_type AS ENUM (
    'ORGANIZATION',
    'PHYSICIAN'
);

CREATE TYPE update_request_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED'
);

CREATE TYPE preferred_communication AS ENUM (
    'PHONE',
    'EMAIL',
    'FAX',
    'IN_PERSON'
);

CREATE TYPE visit_impression AS ENUM (
    'VERY_POSITIVE',
    'POSITIVE',
    'NEUTRAL',
    'NEGATIVE',
    'VERY_NEGATIVE'
);

-- =========================
-- LOOKUP TABLES
-- =========================

CREATE TABLE territories (
                             id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE,
                             description TEXT
);

CREATE TABLE parent_organizations (
                                      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      name VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE boroughs (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE ptoc_locations (
                                id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE specialties (
                             id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE permissions (
                             id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             code        VARCHAR(100) NOT NULL UNIQUE,
                             description VARCHAR(255),
                             created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =========================
-- USERS
-- =========================

CREATE TABLE users (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,

                       role user_role NOT NULL,
                       status user_status NOT NULL DEFAULT 'ACTIVE',

                       territory_id BIGINT REFERENCES territories(id),

                       created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =========================
-- ACCOUNTS
-- =========================

CREATE TABLE accounts (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                          account_id VARCHAR(30) UNIQUE,

                          organization_name VARCHAR(255) NOT NULL,
                          parent_organization_id BIGINT REFERENCES parent_organizations(id),
                          organization_type VARCHAR(100),

                          borough_id BIGINT NOT NULL REFERENCES boroughs(id),
                          ptoc_location_id BIGINT NOT NULL REFERENCES ptoc_locations(id),

                          address VARCHAR(255) NOT NULL,
                          floor_suite VARCHAR(50),
                          zip_code VARCHAR(10) NOT NULL,

                          phone VARCHAR(20) NOT NULL,
                          fax VARCHAR(20),
                          email VARCHAR(150),

                          provides_telehealth BOOLEAN NOT NULL DEFAULT FALSE,
                          same_day_walkins BOOLEAN NOT NULL DEFAULT FALSE,

                          gatekeeper_name VARCHAR(150),
                          gatekeeper_title VARCHAR(100),
                          gatekeeper_phone VARCHAR(20),
                          gatekeeper_email VARCHAR(150),

                          preferred_communication preferred_communication,

                          status approval_status NOT NULL DEFAULT 'PENDING_APPROVAL',

                          submitted_by BIGINT NOT NULL REFERENCES users(id),
                          approved_by BIGINT REFERENCES users(id),

                          created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                          CONSTRAINT uq_account_identity
                              UNIQUE (
                                      organization_name,
                                      zip_code,
                                      address,
                                      floor_suite
                                  )
);

-- =========================
-- PHYSICIANS
-- =========================

CREATE TABLE physicians (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                            npi VARCHAR(10) NOT NULL UNIQUE,
                            name VARCHAR(150) NOT NULL,

                            specialty_id BIGINT NOT NULL REFERENCES specialties(id),

                            email VARCHAR(150),
                            phone VARCHAR(20),

                            status approval_status NOT NULL DEFAULT 'PENDING_APPROVAL',

                            submitted_by BIGINT NOT NULL REFERENCES users(id),
                            approved_by BIGINT REFERENCES users(id),

                            created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =========================
-- ACCOUNT PHYSICIANS
-- =========================

CREATE TABLE account_physicians (
                                    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                    account_id BIGINT NOT NULL
                                        REFERENCES accounts(id)
                                            ON DELETE CASCADE,

                                    physician_id BIGINT NOT NULL
                                        REFERENCES physicians(id)
                                            ON DELETE CASCADE,

                                    date_first_seen DATE NOT NULL DEFAULT CURRENT_DATE,

                                    CONSTRAINT uq_account_physician
                                        UNIQUE (account_id, physician_id)
);

CREATE TABLE user_permissions (
                                  user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                  permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
                                  PRIMARY KEY (user_id, permission_id)
);

-- =========================
-- VISITS
-- =========================

CREATE TABLE visits (
                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                        account_id BIGINT NOT NULL REFERENCES accounts(id),

                        visit_date DATE NOT NULL,

                        visit_type visit_type NOT NULL,

                        visitor_id BIGINT NOT NULL REFERENCES users(id),

                        joined_visit BOOLEAN NOT NULL DEFAULT FALSE,

                        joined_visitor_id BIGINT REFERENCES users(id),

                        visit_impression visit_impression NOT NULL,

                        materials_shared TEXT[],

                        notes TEXT,

                        next_visit_date DATE,

                        status VARCHAR(30) NOT NULL DEFAULT 'RECORDED',

                        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                        CONSTRAINT chk_joined_visitor
                            CHECK (
                                (joined_visit = TRUE AND joined_visitor_id IS NOT NULL)
                                    OR
                                (joined_visit = FALSE AND joined_visitor_id IS NULL)
                                )
);
ALTER TABLE visits
DROP COLUMN status;

ALTER TABLE visits
    ADD COLUMN status approval_status NOT NULL DEFAULT 'PENDING_APPROVAL',
    ADD COLUMN approved_by BIGINT REFERENCES users(id),
    ADD COLUMN reviewed_at TIMESTAMPTZ

-- =========================
-- VISIT PHYSICIANS
-- =========================

CREATE TABLE visit_physicians (
                                  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                  visit_id BIGINT NOT NULL
                                      REFERENCES visits(id)
                                          ON DELETE CASCADE,

                                  physician_id BIGINT NOT NULL
                                      REFERENCES physicians(id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT uq_visit_physician
                                      UNIQUE (visit_id, physician_id)
);

-- =========================
-- UPDATE REQUESTS
-- =========================

CREATE TABLE update_requests (
                                 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                 entity_type update_entity_type NOT NULL,

                                 entity_id BIGINT NOT NULL,

                                 requested_by BIGINT NOT NULL
                                     REFERENCES users(id),

                                 what_needs_updating VARCHAR(500) NOT NULL,

                                 new_information VARCHAR(500),

                                 status update_request_status NOT NULL DEFAULT 'PENDING',

                                 reviewed_by BIGINT REFERENCES users(id),

                                 review_notes TEXT,

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO territories (name, description) VALUES
                                                ('Bronx Territory', 'Bronx coverage area'),
                                                ('Brooklyn Territory', 'Brooklyn coverage area');

INSERT INTO parent_organizations (name) VALUES
                                            ('NYC Health + Hospitals'),
                                            ('Mount Sinai Health System'),
                                            ('Northwell Health');


INSERT INTO boroughs (name) VALUES
                                ('Manhattan'), ('Brooklyn'), ('Queens'), ('Bronx'), ('Staten Island');

INSERT INTO ptoc_locations (name) VALUES
                                      ('PTOC North'), ('PTOC South'), ('PTOC East'), ('PTOC West');

INSERT INTO specialties (name) VALUES
                                   ('Internal Medicine'), ('Cardiology'), ('Orthopedics'),
                                   ('Pediatrics'), ('Family Medicine'), ('Dermatology');

INSERT INTO users (username, email, password_hash, role, status) VALUES
    ('admin1','admin1@example.com','CHANGE_ME_HASH', 'ADMIN','ACTIVE');

INSERT INTO accounts (
    account_id, organization_name, parent_organization_id, organization_type,
    borough_id, ptoc_location_id, address, floor_suite, zip_code, phone,
    email, provides_telehealth, same_day_walkins,
    gatekeeper_name, gatekeeper_title, gatekeeper_phone,
    preferred_communication, status, submitted_by, approved_by
) VALUES (
             'ACC-0001', 'Riverside Medical Group', 1, 'Outpatient Clinic',
             1, 1, '123 Main St', '4B', '10001', '212-555-0200',
             'info@riversidemed.example.com', TRUE, FALSE,
             'Carla Gomez', 'Office Manager', '212-555-0201',
             'EMAIL', 'ACTIVE', 1, 1
         );


INSERT INTO physicians (inpi, name, specialty_id, email, phone, status, submitted_by, approved_by) VALUES
    (1,'1234567890', 'Dr. Susan Lee', 2, 's.lee@riversidemed.example.com', '212-555-0210', 'ACTIVE', 1, 1);

INSERT INTO account_physicians (id,account_id, physician_id, date_first_seen) VALUES
    (1,1, 1, '2026-01-15');

INSERT INTO visits (
    id,account_id, visit_date, visit_type, visitor_id, joined_visit,
    visit_impression, materials_shared, notes, next_visit_date, status
) VALUES (
             1,1, '2026-01-15', 'NEW', 1, FALSE,
             'POSITIVE', ARRAY['BROCHURES','BUSINESS_CARDS'],
             'Good first meeting, gatekeeper was receptive.', '2026-04-15', 'RECORDED'
         );

INSERT INTO visit_physicians (id,visit_id, physician_id) VALUES (1,1, 1);

INSERT INTO accounts (
    id,organization_name, parent_organization_id, borough_id, ptoc_location_id,
    address, floor_suite, zip_code, phone, status, submitted_by
) VALUES (
             2,'Eastside Family Practice', 2, 1, 2,
             '456 Park Ave', '2A', '11201', '718-555-0300', 'PENDING_APPROVAL', 1
         );

INSERT INTO update_requests (id,entity_type, entity_id, requested_by, what_needs_updating, new_information, status) VALUES
    (1,'ORGANIZATION', 1, 1, 'Phone number is outdated', 'New phone: 212-555-9999', 'PENDING');
select * from users;
ALTER TABLE users ADD COLUMN team_leader_id BIGINT REFERENCES users(id);
ALTER TABLE users ADD COLUMN created_by BIGINT REFERENCES users(id);
CREATE TABLE user_permissions (
                                  user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                  permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
                                  PRIMARY KEY (user_id, permission_id)
);
INSERT INTO user_permissions (user_id, permission_id)
SELECT 1, id FROM permissions WHERE code IN
                                    ('VISIT_VIEW_ALL','ACCOUNT_VIEW','ACCOUNT_EDIT','ACCOUNT_APPROVE',
                                     'PHYSICIAN_VIEW','PHYSICIAN_APPROVE','UPDATE_REQUEST_APPROVE','REPORT_VISIT');

select * from permissions;
INSERT INTO permissions (code, description) VALUES
                                                ('VISIT_CREATE_NEW',        'Create new visit'),
                                                ('VISIT_CREATE_FOLLOWUP',   'Create follow-up visit'),
                                                ('VISIT_VIEW_OWN',          'View own visits'),
                                                ('VISIT_VIEW_TEAM',         'View visits of own team (team leader scope)'),
                                                ('VISIT_VIEW_ALL',          'View all visits'),
                                                ('ACCOUNT_VIEW',            'View accounts'),
                                                ('ACCOUNT_EDIT',            'Edit account'),
                                                ('ACCOUNT_APPROVE',         'Approve pending account'),
                                                ('ACCOUNT_DEACTIVATE',      'Deactivate account'),
                                                ('PHYSICIAN_VIEW',          'View physicians'),
                                                ('PHYSICIAN_APPROVE',       'Approve physician'),
                                                ('PHYSICIAN_MERGE',         'Merge duplicate physician'),
                                                ('UPDATE_REQUEST_VIEW',     'View update requests'),
                                                ('UPDATE_REQUEST_APPROVE',  'Approve/reject update request'),
                                                ('REPORT_SALES_REP',        'View sales rep performance report'),
                                                ('REPORT_ACCOUNT',          'View account activity report'),
                                                ('REPORT_PHYSICIAN',        'View physician engagement report'),
                                                ('REPORT_VISIT',            'View visit summary report'),
                                                ('REPORT_PENDING',          'View pending approvals report'),
                                                ('USER_MANAGE',             'Create/edit/deactivate users'),
                                                ('PERMISSION_ASSIGN',       'Assign permissions to users');

-- default templates per role (just a starting point new users get)
INSERT INTO role_default_permissions (role, permission_id)
SELECT 'ADMIN', id FROM permissions WHERE code IN
                                          ('VISIT_VIEW_ALL','ACCOUNT_VIEW','ACCOUNT_EDIT','ACCOUNT_APPROVE','PHYSICIAN_VIEW',
                                           'PHYSICIAN_APPROVE','UPDATE_REQUEST_VIEW','UPDATE_REQUEST_APPROVE',
                                           'REPORT_SALES_REP','REPORT_ACCOUNT','REPORT_PHYSICIAN','REPORT_VISIT','REPORT_PENDING');

INSERT INTO role_default_permissions (role, permission_id)
SELECT 'TEAM_LEADER', id FROM permissions WHERE code IN
                                                ('VISIT_VIEW_TEAM','ACCOUNT_VIEW','PHYSICIAN_VIEW','REPORT_VISIT');

INSERT INTO role_default_permissions (role, permission_id)
SELECT 'SALES_REP', id FROM permissions WHERE code IN
                                              ('VISIT_CREATE_NEW','VISIT_CREATE_FOLLOWUP','VISIT_VIEW_OWN','ACCOUNT_VIEW','PHYSICIAN_VIEW');