//
// Created by mrspaar on 3/24/23.
//

#ifndef POLYBOT_LEVELS_H
#define POLYBOT_LEVELS_H

#include <dpp/dpp.h>
#include "env.h"


class Pages {
public:
    explicit Pages(const dpp::message& message, int page_num = 0) {
        this->page_num = page_num;
        this->message = message;

        Env::BOT.message_add_reaction(message, "⬅️");
        Env::BOT.message_add_reaction(message, "➡️");
    }

    static inline Pages* get(dpp::snowflake id) {
        if (CACHE.contains(id))
            return CACHE[id];
        return nullptr;
    }

    static inline void create(const dpp::message& message) {
        CACHE[message.id] = new Pages(message);
    }

    static std::string to_progress_bar(int level, int xp, int length) {
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

    static void process_rows(dpp::embed& embed) {
        std::string names, levels, progress;

        for (SQLRow& row: Env::SQL.get()) {
            auto level = row.get<int>("level");

            names += "**" + row.get<std::string>("rank") + ".** <@" + row.get<std::string>("id") + ">\n";
            levels += std::to_string(level) + "\n";
            progress += to_progress_bar(level, row.get<int>("xp"), 6) + "\n";
        }

        embed.add_field("Nom", names, true)
                .add_field("Niveau", levels, true)
                .add_field("Progression", progress, true);
    }

    int increment(const std::string &guild_id, const std::string &emoji) {
        Env::SQL << "SELECT COUNT(*) AS count FROM users WHERE guild = ?", guild_id, std::endl;
        if (!Env::SQL.good())
            return 0;

        int total_pages = Env::SQL.get<int>("count") / 10 + 1;
        page_num = (emoji == "➡️" ? page_num+1 : page_num-1) % total_pages;

        if (page_num < 0)
            page_num = total_pages-1;

        return page_num;
    }

    void update(const dpp::embed& embed, const dpp::snowflake &user, const dpp::emoji &emoji) {
        message.embeds.clear();
        message.add_embed(embed);

        Env::BOT.message_edit(message);
        Env::BOT.message_delete_reaction(message, user, emoji.name);
    }
private:
    int page_num;
    dpp::message message;
    static inline std::map<dpp::snowflake, Pages*> CACHE{};
};


#endif //POLYBOT_LEVELS_H
