//int
// Created by mrspaar on 3/13/23.
//

#include "bot.h"


void Bot::memberJoinHandler(const dpp::guild_member_add_t &event) {
    SQLRow row;
    std::string guild_id = std::to_string(event.added.guild_id);

    int rc = db.prepare("SELECT welcome_channel, welcome_message, logs_channel, newcomer_role FROM guilds WHERE id = ?")
            .bind(guild_id)
            .step(row);

    if (rc != SQLITE_ROW)
        return;

    auto log_channel_id = row.get<std::string>("logs_channel");
    message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(GREEN)
            .set_description(":inbox_tray: " + event.added.get_mention() + " a rejoint le serveur")));

    if (event.added.get_user() == nullptr || event.added.get_user()->is_bot())
        return;

    auto role_id = row.get<std::string>("newcomer_role");
    auto welcome_channel_id = row.get<std::string>("welcome_channel");
    auto welcome_message = row.get<std::string>("welcome_message");

    if (!role_id.empty())
        guild_member_add_role(event.added.guild_id, event.added.user_id, role_id);

    if (!welcome_channel_id.empty()) {
        while (size_t pos = welcome_message.find("<mention>") != std::string::npos)
            welcome_message.replace(pos, 9, event.added.get_mention());

        message_create(dpp::message(welcome_channel_id, welcome_message));
    }

    db.prepare("INSERT OR IGNORE INTO users(id, guild) VALUES(?, ?);")
            .bind(std::to_string(event.added.user_id))
            .bind(guild_id)
            .step();
}


void Bot::memberLeaveHandler(const dpp::guild_member_remove_t &event) {
    SQLRow row;

    int rc = db.prepare("SELECT logs_channel FROM guilds WHERE id = ?")
            .bind(std::to_string(event.removing_guild->id))
            .step(row);

    if (rc != SQLITE_ROW)
        return;

    auto log_channel_id = row.get<std::string>("logs_channel");
    if (log_channel_id.empty())
        return;

    message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(RED)
            .set_description(":outbox_tray: " + event.removed->get_mention() + " a quitté le serveur")));
}


void Bot::banHandler(const dpp::guild_ban_add_t &event) {
    if (event.banned.is_bot())
        return;

    SQLRow row;
    std::string guild_id = std::to_string(event.banning_guild->id);

    db.prepare("DELETE FROM users WHERE id = ? AND guild = ?;")
      .bind(std::to_string(event.banned.id))
      .bind(guild_id)
      .step();

    int rc = db.prepare("SELECT logs_channel FROM guilds WHERE id = ?")
            .bind(std::to_string(event.banning_guild->id))
            .step(row);

    if (rc != SQLITE_ROW)
        return;

    auto log_channel_id = row.get<std::string>("logs_channel");
    if (log_channel_id.empty())
        return;

    message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(RED)
            .set_description(":no_entry_sign: " + event.banned.get_mention() + " a été banni du serveur")));
}


void Bot::unbanHandler(const dpp::guild_ban_remove_t &event) {
    SQLRow row;

    int rc = db.prepare("SELECT logs_channel FROM guilds WHERE id = ?")
            .bind(std::to_string(event.unbanning_guild->id))
            .step(row);

    if (rc != SQLITE_ROW)
        return;

    auto log_channel_id = row.get<std::string>("logs_channel");
    if (log_channel_id.empty())
        return;

    message_create(dpp::message(log_channel_id, dpp::embed()
            .set_color(ORANGE)
            .set_description(":white_check_mark: " + event.unbanned.get_mention() + " a été débanni du serveur")));
}
