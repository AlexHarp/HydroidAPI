CREATE SCHEMA IF NOT EXISTS hydroid AUTHORIZATION hydroid;

CREATE TABLE hydroid.documents (
    id bigserial PRIMARY KEY,
    urn varchar(100) NOT NULL,
    content bytea NOT NULL
);

CREATE UNIQUE INDEX documents_urn_idx ON hydroid.documents (urn);