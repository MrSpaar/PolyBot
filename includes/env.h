//
// Created by mrspaar on 3/23/23.
//

#ifndef POLYBOT_ENV_H
#define POLYBOT_ENV_H

#include <map>
#include <string>
#include <fstream>
#include <dpp/dpp.h>
#include <soci/soci.h>
#include <soci/sqlite3/soci-sqlite3.h>


class Env {
public:
    static void load(const std::string &path = "../.env");
    static void init(const std::string &token, const std::string &db_path);
    static std::string& get(const std::string &key);

    static inline dpp::cluster BOT{""};
    static inline soci::session SQL;
private:
    static inline std::map<std::string, std::string> env{};
};


#endif //POLYBOT_ENV_H
