//
// Created by mrspaar on 3/30/23.
//

#include "listeners.h"


void Listeners::onButtonClick(const dpp::button_click_t &event) {
    Env::BOT.guild_get_member(event.command.guild_id, event.command.usr.id, [event](const auto &callback) {
        dpp::message msg;
        msg.flags = dpp::m_ephemeral;

        if (callback.is_error())
            return event.reply(msg.add_embed(dpp::embed()
                    .set_color(colors::RED)
                    .set_description("Erreur dans la récupération du rôle")
            ));

        dpp::embed embed;
        std::string role_id = event.custom_id;
        dpp::guild_member member = std::get<dpp::guild_member>(callback.value);

        if (std::find(member.roles.begin(), member.roles.end(), role_id) == member.roles.end()) {
            Env::BOT.guild_member_add_role(event.command.guild_id, event.command.usr.id, role_id);
            embed.set_color(colors::GREEN).set_description("Rôle <@&" + role_id + "> ajouté");
        } else {
            Env::BOT.guild_member_remove_role(event.command.guild_id, event.command.usr.id, role_id);
            embed.set_color(colors::RED).set_description("Rôle <@&" + role_id + "> retiré");
        }

        msg.add_embed(embed);
        event.reply(msg);
    });
}


void Listeners::onSelectClick(const dpp::select_click_t &event) {
    Env::BOT.guild_get_member(event.command.guild_id, event.command.usr.id, [event](const auto &callback) {
        dpp::message msg;
        msg.flags = dpp::m_ephemeral;

        if (callback.is_error())
            return event.reply(msg.add_embed(dpp::embed()
                    .set_color(colors::RED)
                    .set_description("Erreur dans la récupération du rôle")
            ));

        std::string role_id = event.values[0];
        dpp::guild_member member = std::get<dpp::guild_member>(callback.value);

        if (std::find(member.roles.begin(), member.roles.end(), role_id) != member.roles.end())
            return event.reply(msg.add_embed(dpp::embed()
                    .set_color(colors::RED)
                    .set_description("Vous avez déjà ce rôle")
            ));

        Env::BOT.guild_member_add_role(event.command.guild_id, event.command.usr.id, role_id);

        event.reply(msg.add_embed(dpp::embed()
                .set_color(colors::GREEN)
                .set_description("Rôle <@&" + role_id + "> ajouté")
        ));
    });
}
