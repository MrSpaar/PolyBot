//
// Created by mrspaar on 6/26/23.
//

#include "listeners.h"


void Listeners::onReady(const dpp::ready_t &event) {
    if (!dpp::run_once<struct register_commands>())
        return;

    Env::BOT.set_presence(dpp::presence(dpp::ps_online, dpp::at_game, "vous observer"));
    std::cout << "Logged in as " << event.from->creator->me.username << std::endl;

    std::ifstream in(Env::get("JSON_PATH"));
    if (in.is_open() && nlohmann::json::parse(in) == Env::TO_BUILD) {
        std::cout << "Commands already registered" << std::endl;
        in.close();
        return;
    }

    in.close();
    Env::BOT.global_bulk_command_create(Env::TO_BUILD, [](const auto &callback) {
        if (callback.is_error()) {
            std::cout << "Error creating commands: " << callback.get_error().message << std::endl;
            exit(1);
        }

        std::ofstream out(Env::get("JSON_PATH"));
        out << nlohmann::json(Env::TO_BUILD).dump(2);
        out.close();

        Env::TO_BUILD.clear();
        std::cout << "Commands registered" << std::endl;
    });
}
