//
// Created by mrspaar on 08/10/23.
//

#include "paginator.h"


void Paginator::create(Bot *bot, const dpp::message &message) {
    CACHE[message.id] = new Paginator(bot, message);
}

Paginator::Paginator(Bot *bot, const dpp::message &message, int pageNum) {
    this->bot = bot;
    this->pageNum = pageNum;
    this->message = message;

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

    int rc = bot->prepare("SELECT COUNT(*) AS count FROM users WHERE guild = ?")
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

void Paginator::processRows(SQLQuery &query, dpp::embed &embed) {
    SQLRow row;
    std::string names, levels, progress;

    while (query.step(row) == SQLITE_ROW) {
        auto level = row.get<int>("level");

        names += "**" + row.get<std::string>("rank") + ".** <@" + row.get<std::string>("id") + ">\n";
        levels += std::to_string(level) + "\n";
        progress += to_progress_bar(level, row.get<int>("xp"), 6) + "\n";
    }

    if (names.empty()) {
        names = "Aucun";
        levels = "Aucun";
        progress = "Aucun";
    }

    embed.add_field("Nom", names, true)
            .add_field("Niveau", levels, true)
            .add_field("Progression", progress, true);
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
