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
    int filled = std::max(1, (int) (progress / next_cap * length));
    int percent = std::max(1, (int) (progress / next_cap * 100));

    for (int i = 0; i < filled; i++)
        bar += "🟩";

    for (int i = 0; i < length-filled; i++)
        bar += "⬛";

    bar += " " + std::to_string(percent) + "%";
    return bar;
}


void Pages::process_rows(const soci::rowset<soci::row>& rows, dpp::embed& embed) {
    std::string names, levels, progress;

    for (auto& row: rows) {
        auto level = row.get<int>(1);
        auto xp = row.get<int>(2);

        names += row.get<std::string>(3) + ". <@" + row.get<std::string>(0) + ">\n";
        levels += std::to_string(level) + "\n";
        progress += to_progress_bar(level, xp, 6) + "\n";
    }

    embed.add_field("Nom", names, true)
            .add_field("Niveau", levels, true)
            .add_field("Progression", progress, true);
}
