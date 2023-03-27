//
// Created by mrspaar on 3/9/23.
//

#include "commands.h"


void logs_handler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];

    auto guild_id = uint64_t(event.command.guild_id);
    dpp::snowflake channel_id = subcommand.get_value<dpp::snowflake>(0);

    if (channel_id.empty()) {
        Env::SQL << "UPDATE guilds SET logs_chan_id = 0 WHERE id = ?", soci::use(guild_id);

        return Command::reply(event, dpp::embed()
                .set_description("✒️ Les logs ont été désactivés")
                .set_color(colors::GREEN)
        );
    }

    Env::SQL << "UPDATE guilds SET logs_chan_id = ?  WHERE id = ?",
            soci::use(guild_id), soci::use(uint64_t(channel_id));

    Command::reply(event, dpp::embed()
            .set_description("✒️ Les logs seront envoyés dans <#" + std::to_string(channel_id) + ">")
            .set_color(colors::GREEN)
    );
}


void welcome_handler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];

    auto guild_id = uint64_t(event.command.guild_id);
    dpp::snowflake channel_id = subcommand.get_value<dpp::snowflake>(0);
    std::string message = subcommand.get_value<std::string>(1);

    if (channel_id.empty()) {
        Env::SQL << "UPDATE guilds SET welcome_chan_id = 0 WHERE id = ?", soci::use(guild_id);

        return Command::reply(event, dpp::embed()
                .set_description("✒️ Les messages de bienvenue ont été désactivés")
                .set_color(colors::GREEN)
        );
    }

    Env::SQL << "UPDATE guilds SET welcome_chan_id = ?, welcome_text = ? WHERE id = ?",
            soci::use(uint64_t(channel_id)), soci::use(message), soci::use(guild_id);

    Command::reply(event, dpp::embed()
            .set_description("✒️ Le message de bienvenue sera envoyé dans <#" + std::to_string(channel_id) + ">")
            .set_color(colors::GREEN)
    );
}


void newcomer_handler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];

    auto guild_id = uint64_t(event.command.guild_id);
    dpp::snowflake role_id = subcommand.get_value<dpp::snowflake>(0);

    if (role_id.empty()) {
        Env::SQL << "UPDATE guilds SET newcomer_role_id = 0 WHERE id = ?", soci::use(guild_id);

        return Command::reply(event, dpp::embed()
                .set_description("✒️ Les nouveaux ne recevront plus de rôle")
                .set_color(colors::GREEN)
        );
    }

    Env::SQL << "UPDATE guilds SET newcomer_role_id = ? WHERE id = ?",
            soci::use(uint64_t(role_id)), soci::use(guild_id);

    Command::reply(event, dpp::embed()
            .set_description("✒️ Les nouveaux recevront le rôle <@&" + std::to_string(role_id) + ">")
            .set_color(colors::GREEN)
    );
}


void announce_handler(const dpp::slashcommand_t &event) {
    auto subcommand = event.command.get_command_interaction().options[0];

    auto guild_id = uint64_t(event.command.guild_id);
    dpp::snowflake channel_id = subcommand.get_value<dpp::snowflake>(0);

    if (channel_id.empty()) {
        Env::SQL << "UPDATE guilds SET announce_chan_id = 0 WHERE id = ?", soci::use(guild_id);

        return Command::reply(event, dpp::embed()
                .set_description("✒️ Les annonces de niveaux ont été désactivées")
                .set_color(colors::GREEN)
        );
    }

    Env::SQL << "UPDATE guilds SET announce_chan_id = ? WHERE id = ?",
            soci::use(uint64_t(channel_id)), soci::use(guild_id);

    Command::reply(event, dpp::embed()
            .set_description("✒️ Les annonces de niveaux seront envoyées dans <#" + std::to_string(channel_id) + ">")
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
