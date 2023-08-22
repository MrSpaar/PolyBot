//int
// Created by mrspaar on 3/13/23.
//

#include "listeners.h"


void Listeners::onGuildMemberAdd(const dpp::guild_member_add_t &event) {
    std::string guild_id = std::to_string(event.added.guild_id);
    Env::SQL << "SELECT welcome_channel, welcome_message, logs_channel, newcomer_role FROM guilds WHERE id = ?",
            guild_id, sqlite::run;

    if (Env::SQL.empty())
        return;

    auto log_channel_id = Env::SQL.get<std::string>("logs_channel");
    Env::BOT.message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(colors::GREEN)
            .set_description(":inbox_tray: " + event.added.get_mention() + " a rejoint le serveur")));

    if (event.added.get_user()->is_bot())
        return;

    auto role_id = Env::SQL.get<std::string>("newcomer_role");
    auto welcome_channel_id = Env::SQL.get<std::string>("welcome_channel");
    auto welcome_message = Env::SQL.get<std::string>("welcome_message");

    if (!role_id.empty())
        Env::BOT.guild_member_add_role(event.added.guild_id, event.added.user_id, role_id);

    if (!welcome_channel_id.empty()) {
        while (size_t pos = welcome_message.find("<mention>") != std::string::npos)
            welcome_message.replace(pos, 9, event.added.get_mention());

        Env::BOT.message_create(dpp::message(welcome_channel_id, welcome_message));
    }

    Env::SQL << "INSERT OR IGNORE INTO users(id, guild) VALUES(?, ?);", std::to_string(event.added.user_id), guild_id;
}


void Listeners::onGuildMemberRemove(const dpp::guild_member_remove_t &event) {
    Env::SQL << "SELECT logs_channel FROM guilds WHERE id = ?", std::to_string(event.removing_guild->id), sqlite::run;
    if (Env::SQL.empty())
        return;

    auto log_channel_id = Env::SQL.get<std::string>("logs_channel");
    if (log_channel_id.empty())
        return;

    Env::BOT.message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(colors::RED)
            .set_description(":outbox_tray: " + event.removed->get_mention() + " a quitté le serveur")));
}


void Listeners::onGuildBanAdd(const dpp::guild_ban_add_t &event) {
    if (event.banned.is_bot())
        return;

    std::string guild_id = std::to_string(event.banning_guild->id);

    Env::SQL << "DELETE FROM users WHERE id = ? AND guild = ?;", std::to_string(event.banned.id), guild_id;
    Env::SQL << "SELECT logs_channel FROM guilds WHERE id = ?", std::to_string(event.banning_guild->id), sqlite::run;

    if (Env::SQL.empty())
        return;

    auto log_channel_id = Env::SQL.get<std::string>("logs_channel");
    if (log_channel_id.empty())
        return;

    Env::BOT.message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(colors::RED)
            .set_description(":no_entry_sign: " + event.banned.get_mention() + " a été banni du serveur")));
}


void Listeners::onGuildBanRemove(const dpp::guild_ban_remove_t &event) {
    Env::SQL << "SELECT logs_channel FROM guilds WHERE id = ?", std::to_string(event.unbanning_guild->id), sqlite::run;
    if (Env::SQL.empty())
        return;

    auto log_channel_id = Env::SQL.get<std::string>("logs_channel");
    if (log_channel_id.empty())
        return;

    Env::BOT.message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(colors::ORANGE)
            .set_description(":white_check_mark: " + event.unbanned.get_mention() + " a été débanni du serveur")));
}
