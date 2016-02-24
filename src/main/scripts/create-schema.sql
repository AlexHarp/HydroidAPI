CREATE SCHEMA IF NOT EXISTS hydroid AUTHORIZATION hydroid;

CREATE TABLE hydroid.documents (
    id bigserial PRIMARY KEY,
    origin varchar(100) NULL,
    urn varchar(100) NULL,
    title varchar(100) NULL,
    type varchar(20) NULL,
    content bytea NOT NULL,
    status varchar(20) NULL,
    status_reason varchar(500) NULL,
    process_date timestamp NOT NULL

);

CREATE UNIQUE INDEX documents_urn_idx ON hydroid.documents (urn);