//
// Created by mrspaar on 3/24/23.
//

#ifndef POLYBOT_LEVELS_H
#define POLYBOT_LEVELS_H

#include <dpp/dpp.h>
#include "bot.h"


class Paginator {
public:
    static void create(Bot *bot, const dpp::message& message);
    explicit Paginator(Bot *bot, const dpp::message& message, int page_num = 0);

    static Paginator* get(dpp::snowflake id);
    int increment(const std::string &guild_id, const std::string &emoji);
    void update(const dpp::embed& embed, const dpp::snowflake &user, const dpp::emoji &emoji);

    static std::string to_progress_bar(int level, int xp, int length);
    static void processRows(SQLQuery &query, dpp::embed& embed);
private:
    Bot* bot;
    int pageNum;
    dpp::message message;

    static inline std::map<dpp::snowflake, Paginator*> CACHE{};
};


#endif //POLYBOT_LEVELS_H
