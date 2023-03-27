//
// Created by mrspaar on 3/3/23.
//

#include "commands.h"


void buttons_handler(const dpp::slashcommand_t &event) {

}


void select_handler(const dpp::slashcommand_t &event) {

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
