CREATE SEQUENCE url_mapping_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE url_mappings
(
    id           BIGINT                   PRIMARY KEY DEFAULT nextval('url_mapping_seq'),
    short_code   VARCHAR(10)              NOT NULL UNIQUE,
    original_url VARCHAR(2048)            NOT NULL,
    click_count  BIGINT                   NOT NULL DEFAULT 0,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    expires_at   TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_url_mappings_short_code ON url_mappings (short_code);
