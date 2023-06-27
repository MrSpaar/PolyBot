//
// Created by mrspaar on 3/24/23.
//

#ifndef POLYBOT_LEVELS_H
#define POLYBOT_LEVELS_H

#include <dpp/dpp.h>
#include <soci/soci.h>
#include <soci/sqlite3/soci-sqlite3.h>


class Pages {
public:
    explicit Pages(const dpp::message& message, int page_num = 0);

    static inline Pages* get(dpp::snowflake id) {
        if (CACHE.contains(id))
            return CACHE[id];
        return nullptr;
    }

    static inline void create(const dpp::message& message) {
        CACHE[message.id] = new Pages(message);
    }

    static std::string to_progress_bar(int level, int xp, int length);
    static bool process_rows(const soci::rowset<soci::row> &rows, dpp::embed& embed);

    int increment(const std::string &guild_id, const std::string &emoji);
    void update(const dpp::embed& embed, const dpp::snowflake &user, const dpp::emoji &emoji);
private:
    int page_num;
    dpp::message message;
    static inline std::map<dpp::snowflake, Pages*> CACHE{};
};


#endif //POLYBOT_LEVELS_H
