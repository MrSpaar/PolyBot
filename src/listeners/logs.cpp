//int
// Created by mrspaar on 3/13/23.
//

#include "listeners.h"


void Listeners::onGuildMemberAdd(const dpp::guild_member_add_t &event) {
    std::string user_id = std::to_string(event.added.user_id);
    std::string guild_id = std::to_string(event.added.guild_id);
    Env::SQL << "INSERT OR IGNORE INTO users(id, guild) VALUES(?, ?)", soci::use(user_id), soci::use(guild_id);

    std::string role_id, welcome_channel_id, welcome_message, log_channel_id;
    Env::SQL << "SELECT welcome_channel, welcome_message, logs_channel, newcomer_role FROM guilds WHERE id = ?",
            soci::use(guild_id), soci::into(welcome_channel_id),
            soci::into(welcome_message), soci::into(log_channel_id), soci::into(role_id);

    if (!role_id.empty())
        Env::BOT.guild_member_add_role(event.added.guild_id, event.added.user_id, role_id);

    if (!welcome_channel_id.empty()) {
        std::string content = welcome_message;
        content.replace(content.find("<mention>"), 9, event.added.get_mention());
        Env::BOT.message_create(dpp::message(welcome_channel_id, content));
    }

    if (log_channel_id.empty())
        return;

    Env::BOT.message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(colors::GREEN)
            .set_description(":inbox_tray:" + event.added.get_mention() + " a rejoint le serveur")));
}


void Listeners::onGuildMemberRemove(const dpp::guild_member_remove_t &event) {
    std::string user_id = std::to_string(event.removed->id);
    std::string guild_id = std::to_string(event.removing_guild->id);
    Env::SQL << "DELETE FROM users WHERE id = ? AND guild = ?", soci::use(user_id), soci::use(guild_id);

    std::string log_channel_id;
    Env::SQL << "SELECT logs_channel FROM guilds WHERE id = ?", soci::use(guild_id), soci::into(log_channel_id);

    if (log_channel_id.empty())
        return;

    Env::BOT.message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(colors::RED)
            .set_description(":outbox_tray:" + event.removed->get_mention() + " a quitté le serveur")));
}


void Listeners::onGuildBanAdd(const dpp::guild_ban_add_t &event) {
    std::string user_id = std::to_string(event.banned.id);
    std::string guild_id = std::to_string(event.banning_guild->id);
    Env::SQL << "DELETE FROM users WHERE id = ? AND guild = ?", soci::use(user_id), soci::use(guild_id);

    std::string log_channel_id;
    Env::SQL << "SELECT logs_channel FROM guilds WHERE id = ?", soci::use(guild_id), soci::into(log_channel_id);

    if (log_channel_id.empty())
        return;

    Env::BOT.message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(colors::RED)
            .set_description(":no_entry_sign:" + event.banned.get_mention() + " a été banni du serveur")));
}


void Listeners::onGuildBanRemove(const dpp::guild_ban_remove_t &event) {
    std::string log_channel_id;
    std::string guild_id = std::to_string(event.unbanning_guild->id);
    Env::SQL << "SELECT logs_channel FROM guilds WHERE id = ?", soci::use(guild_id), soci::into(log_channel_id);

    if (log_channel_id.empty())
        return;

    Env::BOT.message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(colors::ORANGE)
            .set_description(":white_check_mark:" + event.unbanned.get_mention() + " a été débanni du serveur")));
}
