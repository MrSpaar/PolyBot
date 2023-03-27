//
// Created by mrspaar on 3/3/23.
//

#include "commands.h"


void play_handler(const dpp::slashcommand_t &event) {
    Command::reply(event, dpp::embed().set_description("Musique jouée avec succès !"));
}


void skip_handler(const dpp::slashcommand_t &event) {
    Command::reply(event, dpp::embed().set_description("Musique passée avec succès !"));
}


Command play = Command("play", "Jouer une musique", play_handler)
        .add_option(dpp::co_string, "musique", "Le nom de la musique à jouer", true);

Command skip = Command("skip", "Passer à la musique suivante", skip_handler);
