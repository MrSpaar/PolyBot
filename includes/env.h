//
// Created by mrspaar on 3/23/23.
//

#ifndef POLYBOT_ENV_H
#define POLYBOT_ENV_H

#include <map>
#include <string>
#include <dpp/dpp.h>
#include <soci/soci.h>
#include <soci/sqlite3/soci-sqlite3.h>


class Env {
public:
    static void load(const std::string &path = "../.env");
    static void init(const std::string &token, const std::string &db_path);

    static inline std::string& get(const std::string &key) {
        if (!env.contains(key))
            throw std::runtime_error("Environment variable not found: " + key);

        return env[key];
    }

    static inline dpp::cluster BOT{""};
    static inline soci::session SQL;
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
