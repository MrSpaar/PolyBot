#include "framework/command.h"
#include "framework/listener.h"


int main() {
    Logger logger;
    DotEnv env("../.env");
    SQLite conn(env["DB_PATH"]);

    dpp::cluster cluster(
        env["DISCORD_TOKEN"],
        dpp::i_default_intents | dpp::i_guild_members
    );
    
    command_builder_t toBuild;
    command_handlers_t handlers;

    ADD_LISTENERS(env, logger, conn, cluster);
    ADD_COMMANDS(env, logger, conn, cluster, toBuild, handlers);

    cluster.on_ready([&](const dpp::ready_t &event) {
        if (!dpp::run_once<struct register_commands>())
            return;
    
        cluster.set_presence(dpp::presence(dpp::ps_online, dpp::at_game, "vous observer"));
        logger(INFO) << "Logged in as " << event.from->creator->me.username << std::endl;

        std::ifstream in(env["JSON_PATH"]);
        if (in.is_open() && nlohmann::json::parse(in) == toBuild) {
            logger(INFO) << "Commands already registered" << std::endl;
            return in.close();
        }

        in.close();
        cluster.global_bulk_command_create(toBuild, [&](const dpp::confirmation_callback_t &callback) {
            if (callback.is_error()) {
                logger(INFO) << "Error creating commands: " << callback.get_error().human_readable << std::endl;
                exit(1);
            }

            std::ofstream out(env["JSON_PATH"]);
            out << nlohmann::json(toBuild).dump(2);
            out.close();

            toBuild.clear();
            logger(INFO) << "Commands registered" << std::endl;
        });
    });

    cluster.on_slashcommand([&](const dpp::slashcommand_t &event) {
        dpp::command_interaction command = event.command.get_command_interaction();
        std::string name = command.options.empty()? command.name : command.options[0].name;

        if (handlers.contains(name))
            handlers[name](event);
    });

    return cluster.start(dpp::st_wait), 0;
}
