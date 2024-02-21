pub mod clear;

use serenity::builder::CreateApplicationCommand;
use serenity::model::application::command::CommandOptionType;


pub fn register(command: &mut CreateApplicationCommand) -> &mut CreateApplicationCommand {
    command
        .name("clear")
        .description("Supprime un nombre de messages")
        .create_option(|o| o
            .name("n")
            .required(true)
            .kind(CommandOptionType::Integer)
            .description("Nombre de messages à supprimer")
        )
}
