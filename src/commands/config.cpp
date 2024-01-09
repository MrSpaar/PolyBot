//
// Created by mrspaar on 3/9/23.
//

#include "framework/command.h"


DECLARE_COMMAND(Config) {
    handlers["logs"] = WRAP_CMD(logsHandler);
    handlers["bienvenue"] = WRAP_CMD(welcomeHandler);
    handlers["nouveaux"] = WRAP_CMD(newcomerHandler);
    handlers["annonce"] = WRAP_CMD(announceHandler);

    toBuild.push_back(Command {
        "config", "Base des commandes de configuration", 
        dpp::p_manage_guild, {
        {"logs", "Définir le salon des logs", {
            {dpp::co_channel, "salon", "Salon des logs (vide pour désactiver)"}
        }},
        {"bienvenue", "Définir le salon des messages de bienvenue", {
            {dpp::co_channel, "salon", "Salon des messages de bienvenue (vide pour désactiver)"},
            {dpp::co_string, "message", "Message de bienvenue"}
        }},
        {"nouveaux", "Définir le rôle des nouveaux", {
            {dpp::co_role, "rôle", "Rôle des nouveaux (vide pour désactiver)"}
        }},
        {"annonce", "Définir le salon des annonces de niveaux", {
            {dpp::co_channel, "salon", "Salon des annonces de niveaux (vide pour désactiver)"}
        }}
    }});
}

COMMAND_HANDLER(logsHandler) {
    auto subcommand = event.command.get_command_interaction().options[0];
    std::string guild_id = std::to_string(event.command.guild_id);

    SQLQuery(conn, "INSERT OR IGNORE INTO guilds (id) VALUES (?);")
        .bind(guild_id).step();

    if (subcommand.options.empty()) {
        logger(INFO) << "Logs disabled for guild " << guild_id << std::endl;
        
        SQLQuery(conn, "UPDATE guilds SET logs_channel = 0 WHERE id = ?;")
            .bind(guild_id).step();

        return reply(event, dpp::embed()
            .set_description("✒️ Les logs ont été désactivés")
            .set_color(GREEN)
        );
    }

    std::string channel_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));
    logger(INFO) << "Logs enabled for guild " << guild_id << " in channel " << channel_id << std::endl;

    SQLQuery(conn, "UPDATE guilds SET logs_channel = ? WHERE id = ?")
        .bind(channel_id)
        .bind(guild_id)
        .step();

    reply(event, dpp::embed()
        .set_description("✒️ Les logs seront envoyés dans <#" + channel_id + ">")
        .set_color(GREEN)
    );
}

COMMAND_HANDLER(welcomeHandler) {
    auto subcommand = event.command.get_command_interaction().options[0];
    std::string guild_id = std::to_string(event.command.guild_id);

    SQLQuery(conn, "INSERT OR IGNORE INTO guilds (id) VALUES (?);")
        .bind(guild_id).step();

    if (subcommand.options.empty()) {
        logger(INFO) << "Welcome messages disabled for guild " << guild_id << std::endl;

        SQLQuery(conn, "UPDATE guilds SET welcome_channel = 0, welcome_message = '' WHERE id = ?")
            .bind(guild_id).step();

        return reply(event, dpp::embed()
            .set_description("✒️ Les messages de bienvenue ont été désactivés")
            .set_color(GREEN)
        );
    }

    std::string channel_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));
    logger(INFO) << "Welcome messages enabled for guild " << guild_id << " in channel " << channel_id << std::endl;

    SQLQuery(conn, "UPDATE guilds SET welcome_channel = ?, welcome_message = ? WHERE id = ?")
        .bind(channel_id)
        .bind(subcommand.get_value<std::string>(1))
        .bind(guild_id)
        .step();

    reply(event, dpp::embed()
        .set_description("✒️ Le message de bienvenue sera envoyé dans <#" + channel_id + ">")
        .set_color(GREEN)
    );
}

COMMAND_HANDLER(newcomerHandler) {
    auto subcommand = event.command.get_command_interaction().options[0];
    std::string guild_id = std::to_string(event.command.guild_id);

    SQLQuery(conn, "INSERT OR IGNORE INTO guilds (id) VALUES (?);")
        .bind(guild_id).step();

    if (subcommand.options.empty()) {
        logger(INFO) << "Newcomer role disabled for guild " << guild_id << std::endl;

        SQLQuery(conn, "UPDATE guilds SET newcomer_role = 0 WHERE id = ?")
            .bind(guild_id).step();

        return reply(event, dpp::embed()
                .set_description("✒️ Les nouveaux ne recevront plus de rôle")
                .set_color(GREEN)
        );
    }

    std::string role_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));
    logger(INFO) << "Newcomer role enabled for guild " << guild_id << " with role " << role_id << std::endl;

    SQLQuery(conn, "UPDATE guilds SET newcomer_role = ? WHERE id = ?")
        .bind(role_id).bind(guild_id).step();

    reply(event, dpp::embed()
        .set_description("✒️ Les nouveaux recevront le rôle <@&" + role_id + ">")
        .set_color(GREEN)
    );
}

COMMAND_HANDLER(announceHandler) {
    auto subcommand = event.command.get_command_interaction().options[0];
    std::string guild_id = std::to_string(event.command.guild_id);

    SQLQuery(conn, "INSERT OR IGNORE INTO guilds (id) VALUES (?);")
        .bind(guild_id).step();

    if (subcommand.options.empty()) {
        logger(INFO) << "Announce disabled for guild " << guild_id << std::endl;

        SQLQuery(conn, "UPDATE guilds SET announce_channel = 0 WHERE id = ?")
                .bind(guild_id).step();

        return reply(event, dpp::embed()
            .set_description("✒️ Les annonces de niveaux ont été désactivées")
            .set_color(GREEN)
        );
    }

    std::string channel_id = std::to_string(subcommand.get_value<dpp::snowflake>(0));
    logger(INFO) << "Announce enabled for guild " << guild_id << " in channel " << channel_id << std::endl;

    SQLQuery(conn, "UPDATE guilds SET announce_channel = ? WHERE id = ?")
        .bind(channel_id).bind(guild_id).step();

    reply(event, dpp::embed()
        .set_description("✒️ Les annonces de niveaux seront envoyées dans <#" + channel_id + ">")
        .set_color(GREEN)
    );
}
