//
// Created by mrspaar on 3/3/23.
//

#include "framework/command.h"


DECLARE_COMMAND(Moderation) {
    handlers["clear"] = WRAP_CMD(clearHandler);

    toBuild.push_back(Command {
        "mod", "Base des commandes de modération", 
        dpp::p_view_channel, {
        {"clear", "Supprimer un nombre de messages", {
            {dpp::co_integer, "nombre", "Nombre de messages à supprimer (entre 2 et 100)", true}
        }}
    }});
}

COMMAND_HANDLER(clearHandler) {
    int64_t count = std::get<int64_t>(event.get_parameter("nombre"));

    if (count < 2 || count > 100)
        return reply(event, dpp::embed()
            .set_description("❌ Le nombre de messages à supprimer doit être compris entre 1 et 100")
            .set_color(RED), true
        );

    dpp::snowflake channel_id = event.command.channel_id;

    cluster.messages_get(channel_id, 0, 0, 0, count, [&, event, channel_id](const dpp::confirmation_callback_t &callback) {
        if (callback.is_error()) {
            logger(WARNING) << "Failed to get messages to delete in channel " << channel_id << std::endl;

            return reply(event, dpp::embed()
                .set_description("❌ Impossible de trouver les messages à supprimer")
                .set_color(RED), true
            );
        }

        std::vector<dpp::snowflake> to_delete{};
        dpp::message_map messages = get<dpp::message_map>(callback.value);

        for (auto &[snowflake, message]: messages)
            to_delete.push_back(snowflake);

        cluster.message_delete_bulk(to_delete, channel_id, [&](const dpp::confirmation_callback_t &callback) {
            if (callback.is_error()) {
                std::cout << callback.http_info.body << std::endl;

                return reply(event, dpp::embed()
                    .set_description("❌ Erreur lors de la suppression des messages")
                    .set_color(RED), true
                );
            }

            reply(event, dpp::embed()
                .set_color(GREEN)
                .set_description("🗑 "+std::to_string(to_delete.size())+" messages ont été supprimés"), true
            );
        });
    });
}
