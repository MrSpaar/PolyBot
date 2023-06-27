//
// Created by mrspaar on 6/26/23.
//

#ifndef POLYBOT_LISTENERS_H
#define POLYBOT_LISTENERS_H

#include "env.h"


namespace Listeners {
    void bind();

    void onReady(const dpp::ready_t &event);
    void onButtonClick(const dpp::button_click_t &event);
    void onSelectClick(const dpp::select_click_t &event);
    void onMessageCreate(const dpp::message_create_t &event);
    void onReactionAdd(const dpp::message_reaction_add_t &event);
    void onGuildMemberAdd(const dpp::guild_member_add_t &event);
    void onGuildMemberRemove(const dpp::guild_member_remove_t &event);
    void onGuildBanAdd(const dpp::guild_ban_add_t &event);
    void onGuildBanRemove(const dpp::guild_ban_remove_t &event);
    void onVoiceStateUpdate(const dpp::voice_state_update_t &event);
}


#endif //POLYBOT_LISTENERS_H
