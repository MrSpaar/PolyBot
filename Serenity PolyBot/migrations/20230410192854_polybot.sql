-- Add migration script here
CREATE TABLE IF NOT EXISTS guilds (
    id TEXT NOT NULL PRIMARY KEY,
    announce_channel TEXT,
    logs_channel TEXT,
    newcomer_role TEXT,
    welcome_channel TEXT,
    welcome_message TEXT
);

CREATE TABLE IF NOT EXISTS users (
    id TEXT NOT NULL,
    guild TEXT NOT NULL,
    level INTEGER DEFAULT 0,
    xp INTEGER DEFAULT 0,

    PRIMARY KEY (id, guild),
    FOREIGN KEY (guild) REFERENCES guilds(id)
);

CREATE TABLE IF NOT EXISTS temp (
    guild TEXT NOT NULL,
    user TEXT NOT NULL,
    voice_channel TEXT NOT NULL,
    text_channel TEXT NOT NULL,

    PRIMARY KEY (guild, user)
);