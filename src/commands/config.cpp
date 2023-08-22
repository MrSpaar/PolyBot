//
// Created by mrspaar on 3/9/23.
//

#include "commands.h"


void Commands::logsHandler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];

    std::string guild_id = std::to_string(event.command.guild_id);
    Env::SQL << "INSERT OR IGNORE INTO guilds (id) VALUES (?);", guild_id;

    if (subcommand.options.empty()) {
        Env::SQL << "UPDATE guilds SET logs_channel = 0 WHERE id = ?", guild_id, sqlite::run;

        return Commands::reply(event, dpp::embed()
                .set_description("✒️ Les logs ont été désactivés")
                .set_color(colors::GREEN)
        );
    }

    std::string channel_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));
    Env::SQL << "UPDATE guilds SET logs_channel = ? WHERE id = ?", channel_id, guild_id, sqlite::run;

    Commands::reply(event, dpp::embed()
            .set_description("✒️ Les logs seront envoyés dans <#" + channel_id + ">")
            .set_color(colors::GREEN)
    );
}


void Commands::welcomeHandler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];

    std::string guild_id = std::to_string(event.command.guild_id);
    Env::SQL << "INSERT OR IGNORE INTO guilds (id) VALUES (?);", guild_id;

    if (subcommand.options.empty()) {
        Env::SQL << "UPDATE guilds SET welcome_channel = 0, welcome_message = '' WHERE id = ?", guild_id, sqlite::run;

        return Commands::reply(event, dpp::embed()
                .set_description("✒️ Les messages de bienvenue ont été désactivés")
                .set_color(colors::GREEN)
        );
    }

    std::string channel_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));
    Env::SQL << "UPDATE guilds SET welcome_channel = ?, welcome_message = ? WHERE id = ?",
                channel_id, subcommand.get_value<std::string>(1), guild_id, sqlite::run;

    Commands::reply(event, dpp::embed()
            .set_description("✒️ Le message de bienvenue sera envoyé dans <#" + channel_id + ">")
            .set_color(colors::GREEN)
    );
}


void Commands::newcomerHandler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];

    std::string guild_id = std::to_string(event.command.guild_id);
    Env::SQL << "INSERT OR IGNORE INTO guilds (id) VALUES (?);", guild_id;

    if (subcommand.options.empty()) {
        Env::SQL << "UPDATE guilds SET newcomer_role = 0 WHERE id = ?", guild_id, sqlite::run;

        return Commands::reply(event, dpp::embed()
                .set_description("✒️ Les nouveaux ne recevront plus de rôle")
                .set_color(colors::GREEN)
        );
    }

    std::string role_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));
    Env::SQL << "UPDATE guilds SET newcomer_role = ? WHERE id = ?", role_id, guild_id, sqlite::run;

    Commands::reply(event, dpp::embed()
            .set_description("✒️ Les nouveaux recevront le rôle <@&" + role_id + ">")
            .set_color(colors::GREEN)
    );
}


void Commands::announceHandler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];

    std::string guild_id = std::to_string(event.command.guild_id);
    Env::SQL << "INSERT OR IGNORE INTO guilds (id) VALUES (?);", guild_id;

    if (subcommand.options.empty()) {
        Env::SQL << "UPDATE guilds SET announce_channel = 0 WHERE id = ?", guild_id, sqlite::run;

        return Commands::reply(event, dpp::embed()
                .set_description("✒️ Les annonces de niveaux ont été désactivées")
                .set_color(colors::GREEN)
        );
    }

    std::string channel_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));
    Env::SQL << "UPDATE guilds SET announce_channel = ? WHERE id = ?", channel_id, guild_id, sqlite::run;

    Commands::reply(event, dpp::embed()
            .set_description("✒️ Les annonces de niveaux seront envoyées dans <#" + channel_id + ">")
            .set_color(colors::GREEN)
    );
}
