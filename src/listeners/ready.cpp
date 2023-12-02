//
// Created by mrspaar on 6/26/23.
//

#include "bot.h"


void Bot::readyHandler(const dpp::ready_t &event) {
    if (!dpp::run_once<struct register_commands>())
        return;

    set_presence(dpp::presence(dpp::ps_online, dpp::at_game, "vous observer"));
    logger(INFO) << "Logged in as " << event.from->creator->me.username << std::endl;

    std::ifstream in(env["JSON_PATH"]);
    if (in.is_open() && nlohmann::json::parse(in) == toBuild) {
        logger(INFO) << "Commands already registered" << std::endl;
        in.close();
        return;
    }

    in.close();
    global_bulk_command_create(toBuild, [&](const auto &callback) {
        if (callback.is_error()) {
            logger(INFO) << "Error creating commands: " << callback.get_error().message << std::endl;
            exit(1);
        }

        std::ofstream out(env["JSON_PATH"]);
        out << nlohmann::json(toBuild).dump(2);
        out.close();

        toBuild.clear();
        logger(INFO) << "Commands registered" << std::endl;
    });
}
