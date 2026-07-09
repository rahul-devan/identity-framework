CREATE SCHEMA IF NOT EXISTS identity_framework;

SET search_path TO identity_framework;

CREATE TABLE IF NOT EXISTS departments (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    external_id     VARCHAR(255),
    external_source VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS job_titles (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255),
    external_source VARCHAR(255),
    CONSTRAINT uk_job_titles_name_source UNIQUE (name, external_source)
);

CREATE TABLE IF NOT EXISTS blueprints (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS applications (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(100) NOT NULL UNIQUE,
    description      VARCHAR(255),
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    app_url          VARCHAR(255),
    created_by       VARCHAR(50),
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,
    integration_name VARCHAR(255),
    essential        BOOLEAN
);

CREATE TABLE IF NOT EXISTS companies (
    id                       BIGSERIAL PRIMARY KEY,
    name                     VARCHAR(255),
    location                 VARCHAR(255),
    phone_number             VARCHAR(255),
    approver_id              BIGINT,
    created_at               TIMESTAMP,
    updated_at               TIMESTAMP,
    primary_contact_user_id  BIGINT,
    status                   VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    is_enabled               BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS users (
    id               BIGSERIAL PRIMARY KEY,
    username         VARCHAR(255),
    password         VARCHAR(255),
    email            VARCHAR(255) NOT NULL UNIQUE,
    first_name       VARCHAR(255),
    last_name        VARCHAR(255),
    phone_number     VARCHAR(255),
    dob              TIMESTAMP,
    ssn              VARCHAR(255) UNIQUE,
    azure_id         VARCHAR(255) UNIQUE,
    external_id      VARCHAR(255),
    external_source  VARCHAR(255),
    job_title_name   VARCHAR(255),
    department_id    BIGINT REFERENCES departments(id),
    source           VARCHAR(255) NOT NULL,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    last_synced_at   TIMESTAMP,
    blueprint_id     BIGINT REFERENCES blueprints(id),
    job_title_id     BIGINT REFERENCES job_titles(id),
    manager_id       BIGINT,
    company_id       BIGINT REFERENCES companies(id),
    country_code     VARCHAR(255)
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_manager
        FOREIGN KEY (manager_id) REFERENCES users(id);

ALTER TABLE companies
    ADD CONSTRAINT fk_companies_approver
        FOREIGN KEY (approver_id) REFERENCES users(id);

ALTER TABLE companies
    ADD CONSTRAINT fk_companies_primary_contact
        FOREIGN KEY (primary_contact_user_id) REFERENCES users(id);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id     BIGINT NOT NULL REFERENCES users(id),
    role_id     BIGINT NOT NULL REFERENCES roles(id),
    assigned_at TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS application_roles (
    id             BIGSERIAL PRIMARY KEY,
    role_name      VARCHAR(255),
    application_id BIGINT REFERENCES applications(id)
);

CREATE TABLE IF NOT EXISTS user_applications (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id),
    application_id BIGINT NOT NULL REFERENCES applications(id),
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    assigned_at    TIMESTAMP NOT NULL,
    removed_at     TIMESTAMP,
    CONSTRAINT uk_user_applications_user_app UNIQUE (user_id, application_id)
);

CREATE TABLE IF NOT EXISTS blueprint_job_titles (
    blueprint_id  BIGINT NOT NULL REFERENCES blueprints(id),
    job_title_id  BIGINT NOT NULL REFERENCES job_titles(id),
    PRIMARY KEY (blueprint_id, job_title_id)
);

CREATE TABLE IF NOT EXISTS blueprint_application_roles (
    id                  BIGSERIAL PRIMARY KEY,
    blueprint_id        BIGINT REFERENCES blueprints(id),
    application_id      BIGINT REFERENCES applications(id),
    application_role_id BIGINT REFERENCES application_roles(id),
    role_name           VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS company_contacts (
    id           BIGSERIAL PRIMARY KEY,
    first_name   VARCHAR(255),
    last_name    VARCHAR(255),
    email        VARCHAR(255),
    phone_number VARCHAR(255),
    dob          TIMESTAMP,
    ssn          VARCHAR(255),
    company_id   BIGINT REFERENCES companies(id)
);

CREATE TABLE IF NOT EXISTS company_approval_requests (
    id           BIGSERIAL PRIMARY KEY,
    company_id   BIGINT NOT NULL REFERENCES companies(id),
    approver_id  BIGINT NOT NULL REFERENCES users(id),
    requested_by BIGINT REFERENCES users(id),
    status       VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    comments     TEXT,
    requested_at TIMESTAMP NOT NULL,
    reviewed_at  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS delegate_requests (
    id                   BIGSERIAL PRIMARY KEY,
    requester_id         BIGINT NOT NULL REFERENCES users(id),
    target_department_id BIGINT NOT NULL REFERENCES departments(id),
    status               VARCHAR(255),
    actioned_by          BIGINT REFERENCES users(id),
    comments             TEXT,
    requested_at         TIMESTAMP,
    actioned_at          TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_department_access (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT REFERENCES users(id),
    department_id BIGINT REFERENCES departments(id),
    granted_at    TIMESTAMP
);

CREATE TABLE IF NOT EXISTS idf_settings (
    id            BIGSERIAL PRIMARY KEY,
    setting_key   VARCHAR(255) NOT NULL UNIQUE,
    setting_value VARCHAR(255),
    data_type     VARCHAR(255),
    description   VARCHAR(255),
    updated_at    TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_azure_id ON users(azure_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_department_id ON users(department_id);
CREATE INDEX IF NOT EXISTS idx_users_manager_id ON users(manager_id);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);
CREATE INDEX IF NOT EXISTS idx_user_applications_user_active ON user_applications(user_id, active);
