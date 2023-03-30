//
// Created by mrspaar on 3/30/23.
//

#include "commands.h"


Listener<dpp::button_click_t> bch(&Env::BOT.on_button_click, [](const auto &event) {
    std::string role_id = event.custom_id;
    dpp::embed embed = dpp::embed()
            .set_color(colors::RED)
            .set_description("Erreur dans la récupération du rôle");

    Env::BOT.guild_get_member(event.command.guild_id, event.command.usr.id, [&](const auto &callback) {
        if (callback.is_error())
            return event.reply(dpp::message().add_embed(embed));

        dpp::guild_member member = std::get<dpp::guild_member>(callback.value);

        if (std::find(member.roles.begin(), member.roles.end(), role_id) != member.roles.end()) {
            Env::BOT.guild_member_add_role(event.command.guild_id, event.command.usr.id, role_id);
            embed.set_color(colors::GREEN).set_description("Rôle <@&" + role_id + "> ajouté");
        } else {
            Env::BOT.guild_member_remove_role(event.command.guild_id, event.command.usr.id, role_id);
            embed.set_color(colors::RED).set_description("Rôle <@&" + role_id + "> retiré");
        }

        event.reply(dpp::message().add_embed(embed));
    });
});


Listener<dpp::select_click_t> sch(&Env::BOT.on_select_click, [](const auto &event) {
    std::string role_id = event.values[0];
    dpp::embed embed = dpp::embed()
            .set_color(colors::RED)
            .set_description("Erreur dans la récupération du rôle");

    event.get_original_response([&](const auto &callback) {
        if (callback.is_error())
            return;

        dpp::message msg = std::get<dpp::message>(callback.value);
        dpp::component select = msg.components[0];

        Env::BOT.guild_get_member(event.command.guild_id, event.command.usr.id, [&](const auto &callback) {
            if (callback.is_error())
                return event.reply(dpp::message().add_embed(embed));

            dpp::guild_member member = std::get<dpp::guild_member>(callback.value);

            for (const dpp::component &component: select.components)
                if (std::find(member.roles.begin(), member.roles.end(), component.custom_id) != member.roles.end())
                    return event.reply(dpp::message().add_embed(embed));

            Env::BOT.guild_member_add_role(event.command.guild_id, event.command.usr.id, role_id);

            event.reply(dpp::message().add_embed(embed
                    .set_color(colors::GREEN)
                    .set_description("Rôle " + select.components[0].label + " ajouté")
            ));
        });
    });
});
