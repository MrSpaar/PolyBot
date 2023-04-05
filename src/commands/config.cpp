//
// Created by mrspaar on 3/9/23.
//

#include "commands.h"


void logs_handler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];

    std::string guild_id = std::to_string(event.command.guild_id);
    Env::SQL << "INSERT OR IGNORE INTO guilds (id) VALUES (?);", soci::use(guild_id);

    if (subcommand.options.empty()) {
        Env::SQL << "UPDATE guilds SET logs_channel = 0 WHERE id = ?", soci::use(guild_id);

        return Command::reply(event, dpp::embed()
                .set_description("✒️ Les logs ont été désactivés")
                .set_color(colors::GREEN)
        );
    }

    std::string channel_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));
    Env::SQL << "UPDATE guilds SET logs_channel = ? WHERE id = ?", soci::use(channel_id), soci::use(guild_id);

    Command::reply(event, dpp::embed()
            .set_description("✒️ Les logs seront envoyés dans <#" + channel_id + ">")
            .set_color(colors::GREEN)
    );
}


void welcome_handler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];

    std::string guild_id = std::to_string(event.command.guild_id);
    Env::SQL << "INSERT OR IGNORE INTO guilds (id) VALUES (?);", soci::use(guild_id);

    if (subcommand.options.empty()) {
        Env::SQL << "UPDATE guilds SET welcome_channel = 0, welcome_message = '' WHERE id = ?", soci::use(guild_id);

        return Command::reply(event, dpp::embed()
                .set_description("✒️ Les messages de bienvenue ont été désactivés")
                .set_color(colors::GREEN)
        );
    }

    std::string channel_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));
    std::string message = subcommand.get_value<std::string>(1);

    Env::SQL << "UPDATE guilds SET welcome_channel = ?, welcome_message = ? WHERE id = ?",
                soci::use(channel_id), soci::use(message), soci::use(guild_id);

    Command::reply(event, dpp::embed()
            .set_description("✒️ Le message de bienvenue sera envoyé dans <#" + channel_id + ">")
            .set_color(colors::GREEN)
    );
}


void newcomer_handler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];

    std::string guild_id = std::to_string(event.command.guild_id);
    Env::SQL << "INSERT OR IGNORE INTO guilds (id) VALUES (?);", soci::use(guild_id);

    if (subcommand.options.empty()) {
        Env::SQL << "UPDATE guilds SET newcomer_role = 0 WHERE id = ?", soci::use(guild_id);

        return Command::reply(event, dpp::embed()
                .set_description("✒️ Les nouveaux ne recevront plus de rôle")
                .set_color(colors::GREEN)
        );
    }

    std::string role_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));
    Env::SQL << "UPDATE guilds SET newcomer_role = ? WHERE id = ?",
                soci::use(role_id), soci::use(guild_id);

    Command::reply(event, dpp::embed()
            .set_description("✒️ Les nouveaux recevront le rôle <@&" + role_id + ">")
            .set_color(colors::GREEN)
    );
}


void announce_handler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];

    std::string guild_id = std::to_string(event.command.guild_id);
    Env::SQL << "INSERT OR IGNORE INTO guilds (id) VALUES (?);", soci::use(guild_id);

    if (subcommand.options.empty()) {
        Env::SQL << "UPDATE guilds SET announce_channel = 0 WHERE id = ?", soci::use(guild_id);

        return Command::reply(event, dpp::embed()
                .set_description("✒️ Les annonces de niveaux ont été désactivées")
                .set_color(colors::GREEN)
        );
    }

    std::string channel_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));
    Env::SQL << "UPDATE guilds SET announce_channel = ? WHERE id = ?", soci::use(channel_id), soci::use(guild_id);

    Command::reply(event, dpp::embed()
            .set_description("✒️ Les annonces de niveaux seront envoyées dans <#" + channel_id + ">")
            .set_color(colors::GREEN)
    );
}


Command config = Command("config", "Base des commandes de configuration")
        .add_subcommand(
                Subcommand("logs", "Définir le salon des logs", logs_handler)
                .add_option(dpp::co_channel, "salon", "Le salon où envoyer les logs")
        )
        .add_subcommand(
                Subcommand("bienvenue", "Définir le salon de bienvenue", welcome_handler)
                .add_option(dpp::co_channel, "salon", "Le salon où envoyer les messages de bienvenue")
                .add_option(dpp::co_string, "message", "Le message à envoyer")
        )
        .add_subcommand(
                Subcommand("nouveau", "Définir le rôle des nouveaux", newcomer_handler)
                .add_option(dpp::co_role, "role", "Le rôle reçu par les nouveaux")
        )
        .add_subcommand(
                Subcommand("annonce", "Définir le salon d'annonce de niveaux", announce_handler)
                .add_option(dpp::co_channel, "salon", "Le salon où envoyer les annonces de niveaux")
        );
