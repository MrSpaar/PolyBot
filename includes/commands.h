//
// Created by mrspaar on 3/3/23.
//

#ifndef POLYBOT_COMMAND_H
#define POLYBOT_COMMAND_H

#include "env.h"
typedef void (*slash_handler)(const dpp::slashcommand_t&);


namespace Commands {
    inline void reply(const dpp::slashcommand_t &event, const dpp::embed& embed, bool ephemeral = false) {
        dpp::message msg = dpp::message(event.command.channel_id, embed);
        msg.flags = ephemeral ? dpp::m_ephemeral : 0;
        event.reply(msg);
    }

    void logsHandler(const dpp::slashcommand_t &event);
    void welcomeHandler(const dpp::slashcommand_t &event);
    void newcomerHandler(const dpp::slashcommand_t &event);
    void announceHandler(const dpp::slashcommand_t &event);
    void rankHandler(const dpp::slashcommand_t &event);
    void leaderboardHandler(const dpp::slashcommand_t &event);
    void buttonsHandler(const dpp::slashcommand_t &event);
    void selectHandler(const dpp::slashcommand_t &event);
    void kickHandler(const dpp::slashcommand_t &event);
    void banHandler(const dpp::slashcommand_t &event);
    void unbanHandler(const dpp::slashcommand_t &event);
    void clearHandler(const dpp::slashcommand_t &event);
    void twitchHandler(const dpp::slashcommand_t &event);
    void wikiHandler(const dpp::slashcommand_t &event);
}


class Command {
public:
    Command(const std::string& name, const std::string& description, slash_handler handler = nullptr) {
        Env::TO_BUILD.emplace_back(name, description, 0);

        if (handler != nullptr)
            Env::BOT.on_slashcommand(Command::wrap(name, handler));
    }

    Command& add_subcommand(const std::string &name, const std::string &description, slash_handler handler, std::vector<dpp::command_option> options = {}) {
        dpp::command_option subcommand(dpp::co_sub_command, name, description);
        subcommand.options = std::move(options);
        Env::TO_BUILD.back().add_option(subcommand);

        if (handler != nullptr)
            Env::BOT.on_slashcommand(Command::wrap(name, handler));

        return *this;
    }
private:
    static std::function<void(const dpp::slashcommand_t&)> wrap(const std::string& name, slash_handler handler) {
        return [name, handler](const dpp::slashcommand_t &event) {
            dpp::command_interaction command = event.command.get_command_interaction();

            if (!command.options.empty() && command.options[0].name == name)
                return handler(event);

            if (command.name == name)
                return handler(event);
        };
    }
};


#endif //POLYBOT_COMMAND_H
