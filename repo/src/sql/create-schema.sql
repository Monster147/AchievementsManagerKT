DROP SCHEMA IF EXISTS dbo CASCADE;
CREATE SCHEMA IF NOT EXISTS dbo;

CREATE TABLE dbo.users (
    id          SERIAL                  PRIMARY KEY,
    name        VARCHAR(255)            NOT NULL,
    email       VARCHAR(255)            NOT NULL UNIQUE,
    password_validation VARCHAR(255)    NOT NULL
);

create table dbo.tokens
(
    token_validation VARCHAR(256) primary key,
    user_id          int references dbo.users (id),
    created_at       bigint not null,
    last_used_at     bigint not null
);

CREATE TABLE dbo.games (
    id                                  SERIAL              PRIMARY KEY,
    external_game_id                    VARCHAR(255)        NOT NULL,
    name                                VARCHAR(255)        NOT NULL,
    genre                               VARCHAR(255)[]      NOT NULL DEFAULT '{}',
    platform                            VARCHAR(50)         NOT NULL DEFAULT 'UNDEFINED',
    release_year                        VARCHAR(4)          NOT NULL DEFAULT '',
    source                              VARCHAR(50)         NOT NULL DEFAULT 'UNDEFINED',
    cover                               VARCHAR(512)        NOT NULL DEFAULT '',
    UNIQUE (external_game_id, source)
);

CREATE TABLE dbo.user_games (
    id                          SERIAL      PRIMARY KEY,
    user_id                     INT         NOT NULL REFERENCES dbo.users(id) ON DELETE CASCADE,
    game_id                     INT         NOT NULL REFERENCES dbo.games(id) ON DELETE CASCADE,
    synchronize                 BOOLEAN     NOT NULL DEFAULT FALSE,
    UNIQUE (user_id, game_id)
);

CREATE TABLE dbo.achievements (
    id                          SERIAL          PRIMARY KEY,
    api_name                    VARCHAR(255)    NOT NULL,
    name                        VARCHAR(255)    NOT NULL,
    icon                        VARCHAR(512)    NOT NULL,
    description                 TEXT            NOT NULL,
    game_id                     INT             NOT NULL REFERENCES dbo.games(id) ON DELETE CASCADE,
    UNIQUE (api_name, game_id)
);

CREATE TABLE dbo.game_progress (
   id                           SERIAL      PRIMARY KEY,
   user_id                      INT         NOT NULL REFERENCES dbo.users(id) ON DELETE CASCADE,
   game_id                      INT         NOT NULL REFERENCES dbo.games(id) ON DELETE CASCADE,
   completed_achievements       INT[]       NOT NULL DEFAULT '{}',
   UNIQUE (user_id, game_id)
);