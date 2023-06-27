//
// Created by mrspaar on 3/3/23.
//

#ifndef POLYBOT_COMMAND_H
#define POLYBOT_COMMAND_H

#include "env.h"
typedef void (*slash_handler)(const dpp::slashcommand_t&);


namespace Commands {
    void bind();

    inline void reply(const dpp::slashcommand_t &event, const dpp::embed& embed, bool ephemeral = false) {
        dpp::message msg = dpp::message(event.command.channel_id, embed);
        msg.flags = ephemeral ? dpp::m_ephemeral : 0;
        event.reply(msg);
    }

    void logs_handler(const dpp::slashcommand_t &event);
    void welcome_handler(const dpp::slashcommand_t &event);
    void newcomer_handler(const dpp::slashcommand_t &event);
    void announce_handler(const dpp::slashcommand_t &event);
    void rank_handler(const dpp::slashcommand_t &event);
    void leaderboard_handler(const dpp::slashcommand_t &event);
    void buttons_handler(const dpp::slashcommand_t &event);
    void select_handler(const dpp::slashcommand_t &event);
    void kick_handler(const dpp::slashcommand_t &event);
    void ban_handler(const dpp::slashcommand_t &event);
    void unban_handler(const dpp::slashcommand_t &event);
    void clear_handler(const dpp::slashcommand_t &event);
    void twitch_handler(const dpp::slashcommand_t &event);
    void wiki_handler(const dpp::slashcommand_t &event);
}


class Command: public dpp::slashcommand {
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

    Command& add_option(const dpp::command_option_type& type, const std::string& name, const std::string& description, bool required = true) {
        Env::TO_BUILD.back().add_option(
                dpp::command_option(type, name, description, required)
        );

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
