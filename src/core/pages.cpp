//
// Created by mrspaar on 3/24/23.
//

#include "env.h"
#include "pages.h"


Pages::Pages(const dpp::message& message, int page_num) {
    this->page_num = page_num;
    this->message = message;

    Env::BOT.message_add_reaction(message, "⬅️");
    Env::BOT.message_add_reaction(message, "➡️");
}


void Pages::create(const dpp::message &message) {
    CACHE[message.id] = new Pages(message);
}


void Pages::update(const dpp::embed& embed) {
    message.embeds.clear();
    message.add_embed(embed);
    Env::BOT.message_edit(message);
}


int Pages::increment(int total_pages, bool plus) {
    page_num = (plus ? page_num+1 : page_num-1) % total_pages;

    if (page_num < 0)
        page_num = total_pages-1;

    return page_num;
}


Pages *Pages::get(dpp::snowflake id) {
    if (CACHE.contains(id))
        return CACHE[id];
    return nullptr;
}


std::string Pages::to_progress_bar(int level, int xp, int length) {
    std::string bar;

    if (xp == 0) {
        for (int i = 0; i < length; i++)
            bar += "⬛";
        return bar;
    }

    double next_cap = 5 * level*level + 50*level + 100;
    double next_cumulative = 5.0/6 * (level+1) * (2*(level+1)*(level+1) + 27*(level+1) + 91);

    double progress = next_cap - next_cumulative + xp;
    int filled = (int) (progress / next_cap * length);
    int percent = (int) (progress / next_cap * 100);

    for (int i = 0; i < filled; i++)
        bar += "\\uD83D\\uDFE9";

    for (int i = 0; i < length-filled; i++)
        bar += "⬛";

    bar += " " + std::to_string(percent) + "%";
    return bar;
}


std::vector<std::string> Pages::process_rows(const soci::rowset<soci::row> &rows, const dpp::snowflake &guild_id) {
    std::vector<std::string> values;

    for(auto &row : rows) {
        auto user_id = row.get<uint64_t>(0);
        auto level = row.get<int>(1);
        auto xp = row.get<int>(2);
        auto rank = row.get<int>(3);

        Env::BOT.guild_get_member(guild_id, user_id, [&](const dpp::confirmation_callback_t &callback) {
            if (callback.is_error())
                return;

            dpp::guild_member member = std::get<dpp::guild_member>(callback.value);
            std::string effective_name = member.nickname.empty() ? member.get_user()->username : member.nickname;

            values.push_back(std::to_string(rank) + ". " + effective_name);
            values.push_back(std::to_string(level));
            values.push_back(to_progress_bar(level, xp, 6));
        });
    }

    return values;
}
