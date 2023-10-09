//
// Created by mrspaar on 3/24/23.
//

#ifndef POLYBOT_LEVELS_H
#define POLYBOT_LEVELS_H

#include "bot.h"


class Paginator {
public:
    static inline std::map<dpp::snowflake, Paginator*> CACHE{};
    explicit Paginator(Bot *bot, dpp::message& message, int page_num = 0);

    static Paginator* get(dpp::snowflake id);
    int increment(const std::string &guild_id, const std::string &emoji);
    void update(const dpp::embed& embed, const dpp::snowflake &user, const dpp::emoji &emoji);
    void fill(dpp::embed& embed, const std::string &guild_id, int offset = 0);

    static std::string to_progress_bar(int level, int xp, int length);
private:
    Bot* bot;
    int pageNum;
    dpp::message message;
};


#endif //POLYBOT_LEVELS_H
