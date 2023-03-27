//
// Created by mrspaar on 3/23/23.
//

#include "commands.h"


Listener<dpp::voice_state_update_t> vsuh(&Env::BOT.on_voice_state_update, [](const auto &event) {

});
