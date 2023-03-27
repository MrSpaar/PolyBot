//
// Created by mrspaar on 3/3/23.
//

#ifndef POLYBOT_COMMAND_H
#define POLYBOT_COMMAND_H

#include <string>
#include "env.h"


namespace colors {
    const uint32_t GREEN = 0x2ECC71;
    const uint32_t ORANGE = 0xC27C0E;
    const uint32_t RED = 0xE74C3C;
    const uint32_t BLUE = 0x3498DB;
    const uint32_t GOLD = 0xF1C40F;
}


static std::vector<dpp::slashcommand> TO_BUILD;
typedef void (*slash_handler)(const dpp::slashcommand_t&);


class Subcommand {
public:
    explicit Subcommand(const std::string& name, const std::string &description, slash_handler handler);

    [[nodiscard]] dpp::command_option get_option() const;
    Subcommand &add_option(const dpp::command_option_type &type, const std::string &name, const std::string &description, bool required = false);
private:
    dpp::command_option *sub_slash;
};


class Command {
public:
    explicit Command(const std::string& name, const std::string &description, slash_handler handler = nullptr);

    Command &add_subcommand(const Subcommand& subcommand);
    Command &add_option(const dpp::command_option_type &type, const std::string &name, const std::string &description, bool required = false);

    static void reply(const dpp::slashcommand_t &event, const dpp::embed &embed, bool ephemeral = false);
private:
    dpp::slashcommand *slash;
};


template<typename T>
class Listener {
public:
    Listener(dpp::event_router_t<T> *router, void (*handler)(const T&)) {
        router->attach(handler);
    };
};


#endif //POLYBOT_COMMAND_H
