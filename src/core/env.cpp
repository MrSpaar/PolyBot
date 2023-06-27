//
// Created by mrspaar on 3/23/23.
//

#include "env.h"


void Env::load(const std::string &path) {
    std::ifstream file(path);

    if (!file.is_open())
        throw std::runtime_error("Could not open file: " + path);

    std::string line;
    unsigned long pos;
    bool is_string;

    while (std::getline(file, line)) {
        if (line.empty() || line[0] == '#')
            continue;

        if ((pos = line.find('=')) == std::string::npos)
            continue;

        if ((is_string = line[pos + 1] == '"') && line[line.size() - 1] != '"')
            throw std::runtime_error("Parsing error: " + line);

        if (is_string)
            env[line.substr(0, pos)] = line.substr(pos + 2, line.size() - pos - 3);
        else
            env[line.substr(0, pos)] = line.substr(pos + 1);
    }

    file.close();
}


void Env::init(const std::string &token, const std::string &db_path) {
    BOT.token = token;
    SQL.open(soci::sqlite3, db_path);

    SQL << "CREATE TABLE IF NOT EXISTS guilds ("
           "id STRING PRIMARY KEY, "
           "annonce_channel TEXT, "
           "logs_channel TEXT, "
           "newcomer_role TEXT, "
           "welcome_channel TEXT, "
           "welcome_message TEXT"
    ");";

    SQL << "CREATE TABLE IF NOT EXISTS users ("
           "id TEXT, "
           "guild TEXT, "
           "xp INTEGER DEFAULT 0, "
           "level INTEGER DEFAULT 0, "

           "PRIMARY KEY (id, guild),"
           "FOREIGN KEY (guild) REFERENCES guilds(id) ON DELETE CASCADE"
    ");";
}
