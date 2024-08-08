//
// Created by MrSpaar on 07/01/2024.
//

#pragma once
#include <dpp/dpp.h>
#include "utils/dotenv.h"
#include "utils/logger.h"
#include "utils/sqlite.h"


#define DECLARE_LISTENER(name) void add##name##Listener( \
    DotEnv &env, Logger &logger, SQLite &conn, dpp::cluster &cluster \
)

#define EVENT_HANDLER(name, type) void name( \
    DotEnv &env, Logger &logger, SQLite &conn, \
    dpp::cluster &cluster, const type &event\
)

#define WRAP_LTNR(func) \
    [&](const auto &event) { func(env, logger, conn, cluster, event); }


DECLARE_LISTENER(Voice);
EVENT_HANDLER(voiceHandler, dpp::voice_state_update_t);

DECLARE_LISTENER(Levels);
EVENT_HANDLER(messageCreateHandler, dpp::message_create_t);
EVENT_HANDLER(reactionHandler, dpp::message_reaction_add_t);

DECLARE_LISTENER(Logs);
EVENT_HANDLER(memberJoinHandler, dpp::guild_member_add_t);
EVENT_HANDLER(memberLeaveHandler, dpp::guild_member_remove_t);
EVENT_HANDLER(banHandler, dpp::guild_ban_add_t);
EVENT_HANDLER(unbanHandler, dpp::guild_ban_remove_t);


static void ADD_LISTENERS(
    DotEnv &env, Logger &logger, SQLite &conn, dpp::cluster &cluster
) {
    addVoiceListener(env, logger, conn, cluster);
    addLevelsListener(env, logger, conn, cluster);
    addLogsListener(env, logger, conn, cluster);
}
