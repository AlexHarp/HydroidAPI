CREATE SCHEMA IF NOT EXISTS hydroid AUTHORIZATION hydroid;

CREATE TABLE hydroid.documents (
    id bigserial PRIMARY KEY,
    origin varchar(500) NOT NULL,
    urn varchar(100) NULL,
    title varchar(200) NULL,
    type varchar(20) NOT NULL,
    status varchar(20) NOT NULL,
    status_reason varchar(500) NULL,
    process_date timestamp NOT NULL

);

CREATE UNIQUE INDEX documents_urn_idx ON hydroid.documents (urn);
CREATE UNIQUE INDEX documents_origin_idx ON hydroid.documents (origin);

CREATE TABLE hydroid.image_metadata (
    origin varchar(500) PRIMARY KEY,
    metadata varchar(1000) NOT NULL
);

