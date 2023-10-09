//
// Created by mrspaar on 08/10/23.
//

#include "paginator.h"


Paginator::Paginator(Bot *bot, dpp::message &message, int pageNum) {
    this->bot = bot;
    this->pageNum = pageNum;
    this->message = message;

    fill(message.embeds[0], std::to_string(message.guild_id));

    bot->message_edit(message);
    bot->message_add_reaction(message, "⬅️");
    bot->message_add_reaction(message, "➡️");
}

Paginator *Paginator::get(dpp::snowflake id) {
    if (CACHE.contains(id))
        return CACHE[id];
    return nullptr;
}

void Paginator::update(const dpp::embed &embed, const dpp::snowflake &user, const dpp::emoji &emoji) {
    message.embeds.clear();
    message.add_embed(embed);

    bot->message_edit(message);
    bot->message_delete_reaction(message, user, emoji.name);
}

int Paginator::increment(const std::string &guild_id, const std::string &emoji) {
    SQLRow row;

    int rc = SQLQuery(bot->getDB(), "SELECT COUNT(*) AS count FROM users WHERE guild = ?")
            .bind(guild_id)
            .step(row);

    if (rc != SQLITE_ROW)
        return 0;

    int total_pages = row.get<int>("count") / 10 + 1;
    pageNum = (emoji == "➡️" ? pageNum+1 : pageNum-1) % total_pages;

    if (pageNum < 0)
        pageNum = total_pages-1;

    return pageNum;
}

void Paginator::fill(dpp::embed &embed, const std::string &guild_id, int offset) {
    SQLRow row;
    std::string names, levels, progress;

    auto query = SQLQuery(bot->getDB(),
                    "SELECT id, level, xp, ROW_NUMBER() OVER (ORDER BY xp DESC) as rank "
                    "FROM users WHERE guild = ? LIMIT 10 OFFSET ?"
    );

    query.bind(guild_id).bind(offset);

    while (query.step(row) == SQLITE_ROW) {
        auto level = row.get<int>("level");

        names += "**" + row.get<std::string>("rank") + ".** <@" + row.get<std::string>("id") + ">\n";
        levels += std::to_string(level) + "\n";
        progress += to_progress_bar(level, row.get<int>("xp"), 6) + "\n";
    }

    std::cout << "Finished" << std::endl;

    embed.add_field("Nom", names.empty() ? "Aucun" : names, true)
            .add_field("Niveau", levels.empty() ? "Aucun" : names, true)
            .add_field("Progression", progress.empty() ? "Aucun" : progress, true);
}

std::string Paginator::to_progress_bar(int level, int xp, int length) {
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
