//int
// Created by mrspaar on 3/13/23.
//

#include "commands.h"


Listener<dpp::guild_member_add_t> mah(&Env::BOT.on_guild_member_add, [](const auto &event) {
    Env::SQL << "INSERT OR IGNORE INTO users(user_id, guild_id) VALUES(?, ?)",
            soci::use(uint64_t(event.added.user_id)), soci::use(uint64_t (event.added.guild_id));

    uint64_t role_id;
    uint64_t welcome_channel_id;
    std::string welcome_message;
    uint64_t log_channel_id;

    Env::SQL << "SELECT welcome_channel_id, welcome_message, log_channel_id, welcome_role_id FROM guilds WHERE id = ?",
            soci::use(uint64_t(event.added.guild_id)), soci::into(welcome_channel_id),
            soci::into(welcome_message), soci::into(log_channel_id), soci::into(role_id);

    if (role_id != 0)
        Env::BOT.guild_member_add_role(event.added.guild_id, event.added.user_id, role_id);

    if (welcome_channel_id != 0) {
        std::string content = welcome_message;
        content.replace(content.find("<mention>"), 9, event.added.get_mention());
        Env::BOT.message_create(dpp::message(welcome_channel_id, content));
    }

    if (log_channel_id == 0)
        return;

    Env::BOT.message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(colors::GREEN)
            .set_description(":inbox_tray:" + event.added.get_mention() + " a rejoint le serveur")));
});

Listener<dpp::guild_member_remove_t> mlh(&Env::BOT.on_guild_member_remove, [](const auto &event) {
    Env::SQL << "DELETE FROM users WHERE user_id = ? AND guild_id = ?",
            soci::use(uint64_t(event.removed->id)), soci::use(uint64_t(event.removing_guild->id));

    uint64_t log_channel_id;
    Env::SQL << "SELECT log_channel_id FROM guilds WHERE id = ?",
            soci::use(uint64_t(event.removing_guild->id)), soci::into(log_channel_id);

    if (log_channel_id == 0)
        return;

    Env::BOT.message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(colors::RED)
            .set_description(":outbox_tray:" + event.removed->get_mention() + " a quitté le serveur")));
});


Listener<dpp::guild_ban_add_t> mbh(&Env::BOT.on_guild_ban_add, [](const auto &event) {
    Env::SQL << "DELETE FROM users WHERE user_id = ? AND guild_id = ?",
            soci::use(uint64_t(event.banned.id)), soci::use(uint64_t(event.banning_guild->id));

    uint64_t log_channel_id;
    Env::SQL << "SELECT log_channel_id FROM guilds WHERE id = ?",
            soci::use(uint64_t(event.banning_guild->id)), soci::into(log_channel_id);

    if (log_channel_id == 0)
        return;

    Env::BOT.message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(colors::RED)
            .set_description(":no_entry_sign:" + event.banned.get_mention() + " a été banni du serveur")));
});


Listener<dpp::guild_ban_remove_t> muh(&Env::BOT.on_guild_ban_remove, [](const auto &event) {
    uint64_t log_channel_id;
    Env::SQL << "SELECT log_channel_id FROM guilds WHERE id = ?",
            soci::use(uint64_t(event.unbanning_guild->id)), soci::into(log_channel_id);

    if (log_channel_id == 0)
        return;

    Env::BOT.message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(colors::ORANGE)
            .set_description(":white_check_mark:" + event.unbanned.get_mention() + " a été débanni du serveur")));
});
