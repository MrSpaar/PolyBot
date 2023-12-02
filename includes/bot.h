//
// Created by mrspaar on 3/23/23.
//

#ifndef POLYBOT_BOT_H
#define POLYBOT_BOT_H

#include <dpp/dpp.h>

#include "logger.h"
#include "database.h"

#define GREEN 0x2ECC71
#define ORANGE 0xC27C0E
#define RED 0xE74C3C
#define BLUE 0x3498DB
#define GOLD 0xF1C40F

#define WRAP_2(type, func) [&](const type &event) { bot.func(event); }
#define WRAP_1(func) WRAP_2(dpp::slashcommand_t, func)
#define CHOOSE_MACRO(_1, _2, NAME, ...) NAME
#define WRAP(...) CHOOSE_MACRO(__VA_ARGS__, WRAP_2, WRAP_1)(__VA_ARGS__)


typedef std::function<void(const dpp::slashcommand_t&)> slash_callback_t;
typedef std::function<void(const std::vector<dpp::snowflake> &)> role_callback_t;


class Bot: public dpp::cluster {
public:
    Logger logger;
public:
    explicit Bot(const char *envPath, uint32_t intents = dpp::i_default_intents,
            const std::string &token="", uint32_t shards=0, uint32_t clusterId=0, uint32_t maxclusters=1,
            bool compressed=true, dpp::cache_policy_t policy={dpp::cp_aggressive,dpp::cp_aggressive,dpp::cp_aggressive}, uint32_t requestThreads=12, uint32_t requestThreadsRaw=1
    );

    std::string getEnv(const std::string &key) const { return env.at(key); }
    Database& getDB() { return db; }

    Bot& command(
            const std::string &name, const std::string &description,
            const dpp::permission &permissions = 0, const slash_callback_t& handler = nullptr
    );
    Bot& subcommand(
            const std::string &name, const std::string &description,
            const slash_callback_t& handler, std::vector<dpp::command_option>&& options = {}
    );

    void logsHandler(const dpp::slashcommand_t &event);
    void welcomeHandler(const dpp::slashcommand_t &event);
    void newcomerHandler(const dpp::slashcommand_t &event);
    void announceHandler(const dpp::slashcommand_t &event);
    void rankHandler(const dpp::slashcommand_t &event);
    void leaderboardHandler(const dpp::slashcommand_t &event);
    void unbanHandler(const dpp::slashcommand_t &event);
    void clearHandler(const dpp::slashcommand_t &event);
    void twitchHandler(const dpp::slashcommand_t &event);
    void wikiHandler(const dpp::slashcommand_t &event);

    void readyHandler(const dpp::ready_t &event);
    void messageHandler(const dpp::message_create_t &event);
    void reactionHandler(const dpp::message_reaction_add_t &event);
    void memberJoinHandler(const dpp::guild_member_add_t &event);
    void memberLeaveHandler(const dpp::guild_member_remove_t &event);
    void banHandler(const dpp::guild_ban_add_t &event);
    void unbanHandler(const dpp::guild_ban_remove_t &event);
    void voiceHandler(const dpp::voice_state_update_t &event);
private:
    Database db;

    std::vector<dpp::slashcommand> toBuild;
    std::map<std::string, std::string> env;
    std::map<std::string, slash_callback_t> callbacks;

    static void reply(const dpp::slashcommand_t &event, const dpp::embed& embed, bool ephemeral = false);
};


#endif //POLYBOT_BOT_H
