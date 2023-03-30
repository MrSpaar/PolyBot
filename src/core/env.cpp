//
// Created by mrspaar on 3/23/23.
//

#include "env.h"


void Env::load(const std::string &path) {
    std::ifstream file(path);

    if (!file.is_open())
        throw std::runtime_error("Could not open file: " + path);

    std::string line;
    while (std::getline(file, line)) {
        if (line.empty() || line[0] == '#')
            continue;

        auto pos = line.find('=');
        if (pos == std::string::npos)
            continue;

        if (line[pos + 1] == '"' && line[line.size() - 1] != '"')
            throw std::runtime_error("Parsing error: " + line);

        if (line[pos + 1] == '"')
            env[line.substr(0, pos)] = line.substr(pos + 2, line.size() - pos - 3);
        else
            env[line.substr(0, pos)] = line.substr(pos + 1);
    }

    file.close();
}


void Env::init(const std::string &token, const std::string &db_path) {
    BOT.token = token;
    SQL.open(soci::sqlite3, db_path);
}


std::string &Env::get(const std::string &key) {
    if (!env.contains(key))
        throw std::runtime_error("Key not found: " + key);

    return env[key];
}
