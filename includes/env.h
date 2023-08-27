//
// Created by mrspaar on 3/23/23.
//

#ifndef POLYBOT_ENV_H
#define POLYBOT_ENV_H

#include <map>
#include <string>
#include <dpp/dpp.h>

#include "sqlite.h"


class Env {
public:
    static void load(const std::string &path = "../.env") {
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

    static void init(const std::string &token, const std::string &dbPath) {
        BOT.token = token;
        SQL.init(dbPath);

        SQL << "CREATE TABLE IF NOT EXISTS guilds ("
                    "    id STRING PRIMARY KEY NOT NULL,"
                    "    announce_channel TEST,"
                    "    logs_channel TEXT,"
                    "    newcomer_role TEXT,"
                    "    welcome_channel TEXT,"
                    "    welcome_message TEXT"
                    ");";

        SQL << "CREATE TABLE IF NOT EXISTS users ("
                    "    id STRING PRIMARY KEY NOT NULL,"
                    "    guild STRING PRIMARY KEY NOT NULL,"
                    "    xp INTEGER NOT NULL DEFAULT 0,"
                    "    level INTEGER NOT NULL DEFAULT 0"
                    ");", sqlite::run;
    }

    static inline std::string& get(const std::string &key) {
        if (!env.contains(key))
            throw std::runtime_error("Environment variable not found: " + key);

        return env[key];
    }

    static inline SQLite SQL{};
    static inline dpp::cluster BOT{""};
    static inline std::vector<dpp::slashcommand> TO_BUILD{};
private:
    static inline std::map<std::string, std::string> env{};
};


namespace colors {
    const uint32_t GREEN = 0x2ECC71;
    const uint32_t ORANGE = 0xC27C0E;
    const uint32_t RED = 0xE74C3C;
    const uint32_t BLUE = 0x3498DB;
    const uint32_t GOLD = 0xF1C40F;
}


#endif //POLYBOT_ENV_H
