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


void Pages::update(const dpp::embed& embed, const dpp::snowflake &user, const dpp::emoji &emoji) {
    message.embeds.clear();
    message.add_embed(embed);

    Env::BOT.message_edit(message);
    Env::BOT.message_delete_reaction(message, user, emoji.name);
}


int Pages::increment(const std::string &guild_id, const std::string &emoji) {
    int total_entries;
    Env::SQL << "SELECT COUNT(*) FROM users WHERE guild = ?", soci::use(guild_id), soci::into(total_entries);

    int total_pages = total_entries / 10 + 1;
    page_num = (emoji == "➡️" ? page_num+1 : page_num-1) % total_pages;

    if (page_num < 0)
        page_num = total_pages-1;

    return page_num;
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


bool Pages::process_rows(const soci::rowset<soci::row>& rows, dpp::embed& embed) {
    std::string names, levels, progress;

    for (const auto& row: rows) {
        auto level = row.get<int>(1);

        names += "**" + row.get<std::string>(3) + ".** <@" + row.get<std::string>(0) + ">\n";
        levels += std::to_string(level) + "\n";
        progress += to_progress_bar(level, row.get<int>(2), 6) + "\n";
    }

    if (names.empty())
        return false;

    embed.add_field("Nom", names, true)
         .add_field("Niveau", levels, true)
         .add_field("Progression", progress, true);

    return true;
}
