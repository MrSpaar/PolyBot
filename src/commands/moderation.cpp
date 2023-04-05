//
// Created by mrspaar on 3/3/23.
//

#include "commands.h"


void kick_handler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];

    std::string reason = "Pas de raison spécifiée";
    dpp::snowflake member_id = subcommand.get_value<dpp::snowflake>(0);

    if (subcommand.options.size() > 1)
        reason = subcommand.get_value<std::string>(1);

    Env::BOT.guild_member_delete(event.command.guild_id, member_id, [&](const dpp::confirmation_callback_t &callback) {
            if (callback.is_error())
                return Command::reply(event, dpp::embed()
                        .set_description("❌ Impossible d'expulser le membre")
                        .set_color(colors::RED), true
                );

            Command::reply(event, dpp::embed()
                    .set_color(colors::GREEN)
                    .set_description("📤 <@"+std::to_string(member_id)+"> a été expulsé\n❔ Raison : "+reason)
            );
    });
}


void ban_handler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];

    std::string reason = "Pas de raison spécifiée";
    dpp::snowflake member_id = subcommand.get_value<dpp::snowflake>(0);

    if (subcommand.options.size() > 1)
        reason = subcommand.get_value<std::string>(1);

    Env::BOT.guild_ban_add(event.command.guild_id, member_id, 0, [&](const dpp::confirmation_callback_t &callback) {
            if (callback.is_error())
                return Command::reply(event, dpp::embed()
                        .set_description("❌ Impossible de bannir le membre")
                        .set_color(colors::RED), true
                );

            Command::reply(event, dpp::embed()
                    .set_color(colors::GREEN)
                    .set_description("🔨 <@"+std::to_string(member_id)+"> a été banni\n❔ Raison : "+reason)
            );
    });
}


void unban_handler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];

    std::string reason = "Pas de raison spécifiée";
    dpp::snowflake member_id = subcommand.get_value<dpp::snowflake>(0);

    if (subcommand.options.size() > 1)
        reason = subcommand.get_value<std::string>(1);

    Env::BOT.guild_ban_delete(event.command.guild_id, member_id, [&](const dpp::confirmation_callback_t &callback) {
            if (callback.is_error())
                return Command::reply(event, dpp::embed()
                        .set_description("❌ Impossible de débannir le membre")
                        .set_color(colors::RED), true
                );

            Command::reply(event, dpp::embed()
                    .set_color(colors::GREEN)
                    .set_description("📜 <@" + std::to_string(member_id) + "> a été débanni\n❔ Raison : " + reason)
            );
    });
}


void clear_handler(const dpp::slashcommand_t &event) {
    int64_t count = std::get<int64_t>(event.get_parameter("nombre"));
    dpp::snowflake channel_id = event.command.channel_id;

    Env::BOT.messages_get(channel_id, 0, 0, 0, count, [event, channel_id](const dpp::confirmation_callback_t &callback) {
        if (callback.is_error())
            return Command::reply(event, dpp::embed()
                    .set_description("❌ Impossible de supprimer les messages")
                    .set_color(colors::RED), true
            );

        std::vector<dpp::snowflake> to_delete{};
        dpp::message_map messages = get<dpp::message_map>(callback.value);

        for (auto &[snowflake, message]: messages)
            to_delete.push_back(snowflake);

        Env::BOT.message_delete_bulk(to_delete, channel_id, [&](const dpp::confirmation_callback_t &callback) {
            if (callback.is_error())
                return Command::reply(event, dpp::embed()
                        .set_description("❌ Impossible de supprimer les messages")
                        .set_color(colors::RED), true
                );

            Command::reply(event, dpp::embed()
                    .set_color(colors::GREEN)
                    .set_description("🗑 "+std::to_string(to_delete.size())+" messages ont été supprimés")
            );
        });
    });
}


Command kick = Command("kick", "Expulser un membre du serveur", kick_handler)
        .add_option(dpp::co_user, "membre", "Le membre à expulser", true)
        .add_option(dpp::co_string, "raison", "La raison de l'expulsion");

Command ban = Command("ban", "Bannir un membre du serveur", ban_handler)
        .add_option(dpp::co_user, "membre", "Le membre à bannir", true)
        .add_option(dpp::co_string, "raison", "La raison du bannissement");

Command unban = Command("unban", "Débannir un membre du serveur", unban_handler)
        .add_option(dpp::co_user, "membre", "Le membre à débannir", true)
        .add_option(dpp::co_string, "raison", "La raison du débannissement");

Command clear = Command("clear", "Supprimer un nombre de messages", clear_handler)
        .add_option(dpp::co_integer, "nombre", "Le nombre de messages à supprimer", true);
