//
// Created by MrSpaar on 09/01/2024.
//

#pragma once
#include <dpp/dpp.h>
#include "utils/dotenv.h"
#include "utils/logger.h"
#include "utils/sqlite.h"


#define DECLARE_COMMAND(name) void add##name##Command( \
    DotEnv &env, Logger &logger, SQLite &conn, dpp::cluster &cluster, \
    command_builder_t &toBuild, command_handlers_t &handlers \
)

#define COMMAND_HANDLER(name) void name( \
    DotEnv &env, Logger &logger, SQLite &conn, \
    dpp::cluster &cluster, const dpp::slashcommand_t &event\
)

#define WRAP_CMD(func) \
    [&](const dpp::slashcommand_t &event) { func(env, logger, conn, cluster, event); }

typedef std::vector<dpp::slashcommand> command_builder_t;
typedef std::unordered_map<std::string_view, std::function<void(const dpp::slashcommand_t &)>> command_handlers_t;


DECLARE_COMMAND(Config);
COMMAND_HANDLER(logsHandler);
COMMAND_HANDLER(welcomeHandler);
COMMAND_HANDLER(newcomerHandler);
COMMAND_HANDLER(announceHandler);

DECLARE_COMMAND(Levels);
COMMAND_HANDLER(rankHandler);
COMMAND_HANDLER(leaderboardHandler);

DECLARE_COMMAND(Moderation);
COMMAND_HANDLER(clearHandler);

DECLARE_COMMAND(Search);
COMMAND_HANDLER(twitchHandler);
COMMAND_HANDLER(wikiHandler);


class Command: public dpp::slashcommand {
public:
    Command(
        const char *name, const char *description, dpp::permission permissions = 0,
        std::initializer_list<std::tuple<const char*, const char*, std::initializer_list<dpp::command_option>>> subcommands = {}
    ) {
        this->name = name;
        this->description = description;
        this->default_permission = permissions;

        for (auto &[sub_name, sub_description, sub_options]: subcommands) {
            dpp::command_option subcommand(dpp::co_sub_command, sub_name, sub_description);
            for (auto &option: sub_options)
                subcommand.add_option(option);
            this->add_option(subcommand);
        }
    }
};

static void ADD_COMMANDS(
    DotEnv &env, Logger &logger, SQLite &conn, dpp::cluster &cluster,
    command_builder_t &toBuild, command_handlers_t &handlers
) {
    addConfigCommand(env, logger, conn, cluster, toBuild, handlers);
    addLevelsCommand(env, logger, conn, cluster, toBuild, handlers);
    addModerationCommand(env, logger, conn, cluster, toBuild, handlers);
    addSearchCommand(env, logger, conn, cluster, toBuild, handlers);
}

static void reply(const dpp::slashcommand_t &event, const dpp::embed& embed, bool ephemeral = false) {
    dpp::message msg = dpp::message(event.command.channel_id, embed);
    msg.flags = ephemeral ? dpp::m_ephemeral : 0;
    event.reply(msg);
}
