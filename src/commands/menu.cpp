//
// Created by mrspaar on 3/3/23.
//

#include "commands.h"


typedef std::function<void(const std::vector<dpp::snowflake> &)> callback_t;

void wait_for_roles(const dpp::slashcommand_t &event, callback_t callback) {
    auto listener = Env::BOT.on_message_create([&](const auto &event2) {
        if (event2.msg.author.id != event.command.usr.id || event2.msg.mention_roles.empty())
            return;

        callback(event2.msg.mention_roles);
    });

    std::time_t start = std::time(nullptr);
    Env::BOT.start_timer([&](dpp::timer t) {
        std::time_t now = std::time(nullptr);

        if (now - start > 60) {
            Env::BOT.stop_timer(t);
            event.delete_original_response();
            Env::BOT.on_message_create.detach(listener);
        }
    }, 1);
}


void buttons_handler(const dpp::slashcommand_t &event) {
    std::string title = event.command.get_command_interaction().options[0].get_value<std::string>(0);
    event.reply("Envoie un message avec les rôles à ajouter au menu");

    dpp::role_map guild_roles = Env::BOT.roles_get_sync(event.command.guild_id);
    if (guild_roles.empty())
        return;

    wait_for_roles(event, [&](const auto &roles) {
        auto component = dpp::component();

        for (const dpp::snowflake &role_id: roles) {
            if (!guild_roles.contains(role_id))
                continue;

            dpp::role role = guild_roles[role_id];
            component.add_component(dpp::component()
                        .set_type(dpp::cot_button)
                        .set_style(dpp::cos_primary)
                        .set_label(role.name)
                        .set_id(std::to_string(role.id))
            );
        }

        event.edit_original_response(dpp::message(title).add_component(component));
    });
}


void select_handler(const dpp::slashcommand_t &event) {
    std::string title = event.command.get_command_interaction().options[0].get_value<std::string>(0);
    event.reply("Envoie un message avec les rôles à ajouter au menu");

    dpp::role_map guild_roles = Env::BOT.roles_get_sync(event.command.guild_id);
    if (guild_roles.empty())
        return;

    wait_for_roles(event, [&](const auto &roles) {
        auto component = dpp::component()
                .set_id("menu")
                .set_type(dpp::cot_role_selectmenu)
                .set_placeholder("Sélectionne un rôle");

        for (const dpp::snowflake &role_id: roles) {
            if (!guild_roles.contains(role_id))
                continue;

            dpp::role role = guild_roles[role_id];
            component.add_select_option(dpp::select_option()
                    .set_label(role.name)
                    .set_value(std::to_string(role.id))
            );
        }

        event.edit_original_response(dpp::message(title).add_component(component));
    });
}


Command menu = Command("menu", "Base des commandes de menus")
        .add_subcommand(
                Subcommand("boutons", "Créer un menu de rôles avec des boutons", buttons_handler)
                .add_option(dpp::co_string, "titre", "Le titre du menu", true)
        )
        .add_subcommand(
                Subcommand("liste", "Créer un menu de rôles avec une liste déroulante", select_handler)
                .add_option(dpp::co_string, "titre", "Le titre du menu", true)
        );
