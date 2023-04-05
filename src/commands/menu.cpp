//
// Created by mrspaar on 3/3/23.
//

#include "commands.h"


typedef std::function<void(const std::vector<dpp::snowflake> &)> callback_t;

void wait_for_roles(const dpp::slashcommand_t &event, const callback_t& callback) {
    dpp::event_handle listener = Env::BOT.on_message_create([listener, event, callback](const auto &event2) {
        if (event2.msg.author.id != event.command.member.user_id || event2.msg.mention_roles.empty())
            return;

        callback(event2.msg.mention_roles);
        Env::BOT.on_message_create.detach(listener);
        Env::BOT.message_delete(event2.msg.id, event2.msg.channel_id);
    });

    Env::BOT.start_timer([event, listener](dpp::timer t) {
        Env::BOT.stop_timer(t);
        event.delete_original_response();
        Env::BOT.on_message_create.detach(listener);
    }, 120);
}


void buttons_handler(const dpp::slashcommand_t &event) {
    std::string title = event.command.get_command_interaction().options[0].get_value<std::string>(0);
    event.reply("Envoie un message avec les rôles à ajouter au menu");

    wait_for_roles(event, [event, title](const auto &roles) {
        auto component = dpp::component();

        for (const dpp::snowflake &role_id: roles) {
            dpp::role *role = dpp::find_role(role_id);

            if (role == nullptr)
                continue;

            component.add_component(dpp::component()
                        .set_type(dpp::cot_button)
                        .set_style(dpp::cos_primary)
                        .set_label(role->name)
                        .set_id(std::to_string(role->id))
            );
        }

        event.edit_original_response(dpp::message("Menu de rôles - " + title).add_component(component));
    });
}


void select_handler(const dpp::slashcommand_t &event) {
    std::string title = event.command.get_command_interaction().options[0].get_value<std::string>(0);
    event.reply("Envoie un message avec les rôles à ajouter au menu");

    wait_for_roles(event, [event, title](const auto &roles) {
        auto component = dpp::component()
                .set_id("menu")
                .set_type(dpp::cot_selectmenu)
                .set_placeholder("Sélectionne un rôle");

        for (const dpp::snowflake &role_id: roles) {
            dpp::role *role = dpp::find_role(role_id);

            if (role == nullptr)
                continue;

            component.add_select_option(dpp::select_option()
                    .set_label(role->name)
                    .set_value(std::to_string(role->id))
            );
        }

        dpp::component wrapper = dpp::component().add_component(component);
        event.edit_original_response(dpp::message("Menu de rôles - " + title).add_component(wrapper));
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
