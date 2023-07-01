//
// Created by mrspaar on 3/19/23.
//

#include <random>

#include "pages.h"
#include "listeners.h"


std::map<dpp::snowflake, time_t> cooldowns;

std::random_device rd;
std::default_random_engine generator(rd());
std::uniform_int_distribution<int> distribution(15, 25);


void Listeners::onMessageCreate(const dpp::message_create_t &event) {
    if (event.msg.author.is_bot() || event.msg.author.is_system())
        return;

    if (cooldowns.find(event.msg.author.id) != cooldowns.end() && cooldowns[event.msg.author.id] > time(nullptr))
        return;

    cooldowns[event.msg.author.id] = time(nullptr) + 60;

    std::string guild_id = std::to_string(event.msg.guild_id);
    std::string user_id = std::to_string(event.msg.author.id);

    Env::SQL << "SELECT level, xp FROM users WHERE id = ? AND guild = ?", user_id, guild_id, sqlite::run;
    if (!Env::SQL.good())
        return;

    auto xp = Env::SQL.get<int>("xp");
    auto level = Env::SQL.get<int>("level");

    xp += distribution(generator);
    double next_cap = 5.0/6 * (level+1) * (2*(level+1)*(level+1) + 27*(level+1) + 91);

    if (xp >= next_cap) {
        level++;

        Env::SQL << "SELECT announce_channel FROM guilds WHERE id = ?", guild_id, sqlite::run;
        if (!Env::SQL.good())
            return;

        auto channel_id = Env::SQL.get<std::string>("announce_channel");
        Env::BOT.message_create(dpp::message(channel_id, dpp::embed()
                .set_color(colors::GOLD)
                .set_description(
                        ":tada: " + event.msg.author.get_mention() + " vient de passer au niveau " + std::to_string(level) + " !"
                )
        ));
    }

    Env::SQL << "UPDATE users SET level = ?, xp = ? WHERE id = ? AND guild = ?",
        level, xp, user_id, guild_id, sqlite::run;
}


void Listeners::onReactionAdd(const dpp::message_reaction_add_t &event) {
    if (event.reacting_user.is_bot() || event.reacting_user.is_system())
        return;

    Pages *page = Pages::get(event.message_id);
    if (page == nullptr)
        return;

    std::string guild_id = std::to_string(event.reacting_guild->id);
    int next_page = page->increment(guild_id, event.reacting_emoji.name);

    Env::SQL << "SELECT id, level, xp, ROW_NUMBER() OVER (ORDER BY xp DESC) as rank "
                "FROM users WHERE guild = ? LIMIT 10 OFFSET ?", guild_id, next_page*10, sqlite::run;

    if (!Env::SQL.good())
        return;

    dpp::embed embed = dpp::embed()
            .set_color(colors::BLUE)
            .set_footer("Page " + std::to_string(next_page+1), "");

    Pages::process_rows(embed);
    page->update(embed, event.reacting_user.id, event.reacting_emoji);
}
