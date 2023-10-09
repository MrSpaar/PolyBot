//
// Created by mrspaar on 08/10/23.
//

#include "bot.h"

#include <utility>


Bot::Bot(const char *envPath, uint32_t intents,
        const std::string &token, uint32_t shards, uint32_t clusterId, uint32_t maxclusters,
        bool compressed, dpp::cache_policy_t policy, uint32_t requestThreads, uint32_t requestThreadsRaw
): cluster(token, intents, shards, clusterId, maxclusters, compressed, policy, requestThreads, requestThreadsRaw) {
    std::ifstream file(envPath);

    std::string line;
    std::string key, value;
    std::istringstream iss;

    while (std::getline(file, line)) {
        if (line.empty() || line[0] == '#')
            continue;

        iss = std::istringstream(line);
        std::getline(iss, key, '=');

        if (key.empty())
            continue;

        std::getline(iss, value);
        this->env[key] = value.substr(1, value.size() - 2);
    }

    this->db.init(env["DB_PATH"]);
    this->token = env["DISCORD_TOKEN"];

    on_slashcommand([&] (const dpp::slashcommand_t &event) {
        dpp::command_interaction command = event.command.get_command_interaction();
        std::string name = command.options.empty()? command.name : command.options[0].name;

        if (callbacks.contains(name))
            callbacks[name](event);
    });
}


Bot& Bot::command(
        const std::string &name, const std::string &description,
        const dpp::permission &permissions, const slash_callback_t& handler
) {
    toBuild.emplace_back(name, description, 0);

    if (permissions > 0)
        toBuild.back().set_default_permissions(permissions);

    if (handler != nullptr)
        callbacks[name] = handler;

    return *this;
}


Bot& Bot::subcommand(
        const std::string &name, const std::string &description,
        const slash_callback_t &handler, std::vector<dpp::command_option>&& options
) {
    auto &command = toBuild.back();
    callbacks[name] = handler;

    command.add_option({dpp::co_sub_command, name, description});
    command.options.back().options = std::move(options);

    return *this;
}


void Bot::reply(const dpp::slashcommand_t &event, const dpp::embed &embed, bool ephemeral) {
    dpp::message msg = dpp::message(event.command.channel_id, embed);
    msg.flags = ephemeral ? dpp::m_ephemeral : 0;
    event.reply(msg);
}
