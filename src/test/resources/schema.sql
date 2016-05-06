DROP TABLE IF EXISTS documents;
CREATE TABLE documents (
    id bigint auto_increment PRIMARY KEY,
    origin varchar(500) NOT NULL,
    urn varchar(100) NULL,
    title varchar(500) NULL,
    type varchar(20) NOT NULL,
    status varchar(20) NOT NULL,
    status_reason varchar(1000) NULL,
    process_date timestamp NOT NULL,
    parser_name varchar(50) NULL
);

CREATE UNIQUE INDEX documents_urn_idx ON documents (urn);
CREATE UNIQUE INDEX documents_origin_idx ON documents (origin);

DROP TABLE IF EXISTS image_metadata;
CREATE TABLE image_metadata (
    origin varchar(500) PRIMARY KEY,
    metadata varchar(1000) NOT NULL
);
