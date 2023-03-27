//
// Created by mrspaar on 3/12/23.
//

#include "commands.h"


std::function<void(const dpp::slashcommand_t &)> wrap_handler(const std::string &name, slash_handler handler) {
    return [name, handler](const dpp::slashcommand_t &event) {
        dpp::command_interaction command = event.command.get_command_interaction();

        if (!command.options.empty() && command.options[0].name == name)
            return handler(event);

        if (command.name == name)
            return handler(event);
    };
}


Command::Command(const std::string& name, const std::string &description, slash_handler handler) {
    TO_BUILD.emplace_back(name, description, 0);
    slash = &TO_BUILD.back();

    if (handler != nullptr)
        Env::BOT.on_slashcommand(wrap_handler(name, handler));
}

Command &Command::add_subcommand(const Subcommand& subcommand) {
    slash->options.push_back(subcommand.get_option());
    return *this;
}

Command &Command::add_option(const dpp::command_option_type &type, const std::string &opt_name, const std::string &description, bool required) {
    slash->options.emplace_back(type, opt_name, description, required);
    return *this;
}

void Command::reply(const dpp::slashcommand_t &event, const dpp::embed &embed, bool ephemeral) {
    dpp::message msg = dpp::message(event.command.channel_id, embed);
    msg.flags = ephemeral ? dpp::m_ephemeral : 0;
    event.reply(msg);
}


Subcommand::Subcommand(const std::string& name, const std::string &description, slash_handler handler) {
    Env::BOT.on_slashcommand(wrap_handler(name, handler));
    sub_slash = new dpp::command_option(dpp::co_sub_command, name, description);
}

Subcommand &Subcommand::add_option(const dpp::command_option_type &type, const std::string &opt_name, const std::string &description, bool required) {
    sub_slash->options.emplace_back(type, opt_name, description, required);
    return *this;
}

dpp::command_option Subcommand::get_option() const {
    dpp::command_option option = *sub_slash;
    delete sub_slash;
    return option;
}


Listener<dpp::ready_t> ready_listener(&Env::BOT.on_ready, [](const dpp::ready_t &event) {
    if (!dpp::run_once<struct register_commands>())
        return;

    Env::BOT.set_presence(dpp::presence(dpp::ps_online, dpp::at_game, "vous observer"));
    printf("Logged in as %s\n", event.from->creator->me.username.c_str());

    Env::BOT.guild_bulk_command_create(TO_BUILD, Env::get("GUILD_ID"), [](const dpp::confirmation_callback_t &callback) {
        if (callback.is_error()) {
            printf("Error creating commands: %s\n", callback.get_error().message.c_str());
            exit(1);
        }

        TO_BUILD.clear();
        printf("Commands registered\n");
    });
});
