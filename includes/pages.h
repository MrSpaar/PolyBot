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

    static Pages* get(dpp::snowflake id);
    static void create(const dpp::message& message);
    static std::string to_progress_bar(int level, int xp, int length);
    static std::vector<std::string> process_rows(const soci::rowset<soci::row> &rows, const dpp::snowflake &guild_id);

    void update(const dpp::embed& embed);
    int increment(int total_pages, bool plus);
private:
    int page_num;
    dpp::message message;
    static inline std::map<dpp::snowflake, Pages*> CACHE{};
};


#endif //POLYBOT_LEVELS_H
