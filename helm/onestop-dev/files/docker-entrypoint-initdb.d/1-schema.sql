DROP TABLE IF EXISTS savesearch;
CREATE TABLE savesearch
(
    id VARCHAR(255),
    user_id VARCHAR(255) PRIMARY KEY not null ,
    name VARCHAR(255) not null,
    value VARCHAR(255),
    created_on DATE ,
    last_updated_on DATE
);