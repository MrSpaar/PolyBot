//
// Created by mrspaar on 3/9/23.
//

#include "bot.h"


void Bot::logsHandler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];
    std::string guild_id = std::to_string(event.command.guild_id);

    SQLQuery(db, "INSERT OR IGNORE INTO guilds (id) VALUES (?);")
            .bind(guild_id)
            .step();

    if (subcommand.options.empty()) {
        SQLQuery(db, "UPDATE guilds SET logs_channel = 0 WHERE id = ?;")
                .bind(guild_id)
                .step();

        return Bot::reply(event, dpp::embed()
                .set_description("✒️ Les logs ont été désactivés")
                .set_color(GREEN)
        );
    }

    std::string channel_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));

    SQLQuery(db, "UPDATE guilds SET logs_channel = ? WHERE id = ?")
            .bind(channel_id)
            .bind(guild_id)
            .step();

    Bot::reply(event, dpp::embed()
            .set_description("✒️ Les logs seront envoyés dans <#" + channel_id + ">")
            .set_color(GREEN)
    );
}


void Bot::welcomeHandler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];
    std::string guild_id = std::to_string(event.command.guild_id);

    SQLQuery(db, "INSERT OR IGNORE INTO guilds (id) VALUES (?);")
            .bind(guild_id)
            .step();

    if (subcommand.options.empty()) {
        SQLQuery(db, "UPDATE guilds SET welcome_channel = 0, welcome_message = '' WHERE id = ?")
                .bind(guild_id)
                .step();

        return Bot::reply(event, dpp::embed()
                .set_description("✒️ Les messages de bienvenue ont été désactivés")
                .set_color(GREEN)
        );
    }

    std::string channel_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));

    SQLQuery(db, "UPDATE guilds SET welcome_channel = ?, welcome_message = ? WHERE id = ?")
            .bind(channel_id)
            .bind(subcommand.get_value<std::string>(1))
            .bind(guild_id)
            .step();

    Bot::reply(event, dpp::embed()
            .set_description("✒️ Le message de bienvenue sera envoyé dans <#" + channel_id + ">")
            .set_color(GREEN)
    );
}


void Bot::newcomerHandler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];
    std::string guild_id = std::to_string(event.command.guild_id);

    SQLQuery(db, "INSERT OR IGNORE INTO guilds (id) VALUES (?);")
            .bind(guild_id)
            .step();

    if (subcommand.options.empty()) {
        SQLQuery(db, "UPDATE guilds SET newcomer_role = 0 WHERE id = ?")
                .bind(guild_id)
                .step();

        return Bot::reply(event, dpp::embed()
                .set_description("✒️ Les nouveaux ne recevront plus de rôle")
                .set_color(GREEN)
        );
    }

    std::string role_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));

    SQLQuery(db, "UPDATE guilds SET newcomer_role = ? WHERE id = ?")
            .bind(role_id)
            .bind(guild_id)
            .step();

    Bot::reply(event, dpp::embed()
            .set_description("✒️ Les nouveaux recevront le rôle <@&" + role_id + ">")
            .set_color(GREEN)
    );
}


void Bot::announceHandler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];
    std::string guild_id = std::to_string(event.command.guild_id);

    SQLQuery(db, "INSERT OR IGNORE INTO guilds (id) VALUES (?);")
            .bind(guild_id)
            .step();

    if (subcommand.options.empty()) {
        SQLQuery(db, "UPDATE guilds SET announce_channel = 0 WHERE id = ?")
                .bind(guild_id)
                .step();

        return Bot::reply(event, dpp::embed()
                .set_description("✒️ Les annonces de niveaux ont été désactivées")
                .set_color(GREEN)
        );
    }

    std::string channel_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));

    SQLQuery(db, "UPDATE guilds SET announce_channel = ? WHERE id = ?")
            .bind(channel_id)
            .bind(guild_id)
            .step();

    Bot::reply(event, dpp::embed()
            .set_description("✒️ Les annonces de niveaux seront envoyées dans <#" + channel_id + ">")
            .set_color(GREEN)
    );
}
