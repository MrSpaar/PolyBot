//
// Created by mrspaar on 3/19/23.
//

#include <random>

#include "pages.h"
#include "commands.h"


std::map<dpp::snowflake, time_t> cooldowns;

std::random_device rd;
std::default_random_engine generator(rd());
std::uniform_int_distribution<int> distribution(15, 25);


Listener<dpp::message_create_t> mch(&Env::BOT.on_message_create, [](const dpp::message_create_t &event) {
    if (event.msg.author.is_bot() || event.msg.author.is_system())
        return;

    if (cooldowns.find(event.msg.author.id) != cooldowns.end() && cooldowns[event.msg.author.id] > time(nullptr))
        return;

    cooldowns[event.msg.author.id] = time(nullptr) + 60;

    int level, xp;
    Env::SQL << "SELECT level, xp FROM users WHERE user_id = ? AND guild_id = ?",
            soci::use(uint64_t(event.msg.author.id)), soci::use(uint64_t(event.msg.guild_id)),
            soci::into(level), soci::into(xp);

    xp += distribution(generator);
    double next_cap = 5.0/6 * (level+1) * (2*(level+1)*(level+1) + 27*(level+1) + 91);

    if (xp >= next_cap) {
        level++;

        uint64_t channel_id;
        Env::SQL << "SELECT channel_id FROM guilds WHERE guild_id = ?",
                soci::use(uint64_t(event.msg.guild_id)), soci::into(channel_id);

        if (channel_id != 0)
            Env::BOT.message_create(dpp::message(channel_id, dpp::embed()
                    .set_color(colors::GOLD)
                    .set_description("\\uD83C\\uDD99 " + event.msg.author.get_mention() + " vient de passer au niveau " + std::to_string(level) + " !")
            ));
    }

    Env::SQL << "UPDATE users SET level = ?, xp = ? WHERE user_id = ? AND guild_id = ?",
            soci::use(level), soci::use(xp), soci::use(uint64_t(event.msg.author.id)), soci::use(uint64_t(event.msg.guild_id));
});


Listener<dpp::message_reaction_add_t> mrah(&Env::BOT.on_message_reaction_add, [](const dpp::message_reaction_add_t &event) {
    if (event.reacting_user.is_bot() || event.reacting_user.is_system())
        return;

    Pages *page = Pages::get(event.message_id);
    if (page == nullptr)
        return;

    int total_entries;
    Env::SQL << "SELECT COUNT(*) FROM users WHERE guild_id = ?",
            soci::use(uint64_t(event.reacting_guild->id)), soci::into(total_entries);

    int next_page = page->increment(total_entries / 10 + 1, event.reacting_emoji.name == "➡️");

    soci::rowset<soci::row> rows = (
            Env::SQL.prepare << "SELECT ROW_NUMBER() OVER (ORDER BY xp DESC) as rank, user_id, level, xp"
                                   "FROM users WHERE guild_id=:id LIMIT 10 OFFSET :offser",
                    soci::use(uint64_t(event.reacting_guild->id), "id"), soci::use(next_page * 10, "offset")
    );

    std::vector<std::string> values = Pages::process_rows(rows, event.reacting_guild->id);

    page->update(dpp::embed()
             .set_color(colors::BLUE)
             .set_footer("Page " + std::to_string(next_page), "")
             .add_field("Nom", values[0], true)
             .add_field("Niveau", values[1], true)
             .add_field("Progression", values[2], true)
    );
});
