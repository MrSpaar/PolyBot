//
// Created by mrspaar on 3/19/23.
//

#include <random>
#include "bot.h"
#include "paginator.h"


std::map<dpp::snowflake, time_t> cooldowns;

std::random_device rd;
std::default_random_engine generator(rd());
std::uniform_int_distribution<int> distribution(15, 25);


void Bot::messageHandler(const dpp::message_create_t &event) {
    if (event.msg.author.is_bot() || event.msg.author.is_system())
        return;

    if (cooldowns.find(event.msg.author.id) != cooldowns.end() && cooldowns[event.msg.author.id] > time(nullptr))
        return;

    cooldowns[event.msg.author.id] = time(nullptr) + 60;

    SQLRow row;
    std::string guild_id = std::to_string(event.msg.guild_id);
    std::string user_id = std::to_string(event.msg.author.id);

    int rc = prepare("SELECT level, xp FROM users WHERE id = ? AND guild = ?")
           .bind(user_id)
           .bind(guild_id)
           .step(row);

    if (rc != SQLITE_ROW)
        return;

    auto xp = row.get<int>("xp");
    auto level = row.get<int>("level");

    xp += distribution(generator);
    double next_cap = 5.0/6 * (level+1) * (2*(level+1)*(level+1) + 27*(level+1) + 91);

    if (xp >= next_cap) {
        level++;

        rc = prepare("SELECT announce_channel FROM guilds WHERE id = ?")
               .bind(guild_id)
               .step(row);

        if (rc != SQLITE_ROW)
            return;

        auto channel_id = row.get<std::string>("announce_channel");
        message_create(dpp::message(channel_id, dpp::embed()
                .set_color(GOLD)
                .set_description(
                        ":tada: " + event.msg.author.get_mention() + " vient de passer au niveau " + std::to_string(level) + " !"
                )
        ));
    }

    db.prepare("UPDATE users SET level = ?, xp = ? WHERE id = ? AND guild = ?")
          .bind(level)
          .bind(xp)
          .bind(user_id)
          .bind(guild_id)
          .step();
}


void Bot::reactionHandler(const dpp::message_reaction_add_t &event) {
    if (event.reacting_user.is_bot() || event.reacting_user.is_system())
        return;

    Paginator *page = Paginator::get(event.message_id);
    if (page == nullptr)
        return;

    SQLRow row;
    std::string guild_id = std::to_string(event.reacting_guild->id);
    int next_page = page->increment(guild_id, event.reacting_emoji.name);

    SQLQuery query = db.prepare(
            "SELECT id, level, xp, ROW_NUMBER() OVER (ORDER BY xp DESC) as rank "
            "FROM users WHERE guild = ? LIMIT 10 OFFSET ?"
    ).bind(guild_id)
     .bind(next_page*10);

    dpp::embed embed = dpp::embed()
            .set_color(BLUE)
            .set_footer("Page " + std::to_string(next_page+1), "");

    Paginator::processRows(query, embed);
    page->update(embed, event.reacting_user.id, event.reacting_emoji);
}
