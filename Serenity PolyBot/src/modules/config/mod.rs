pub mod logs;
pub mod welcome;
pub mod newcomer;
pub mod announce;

use serenity::prelude::Context;
use serenity::builder::CreateApplicationCommand;
use serenity::model::prelude::command::CommandOptionType;
use serenity::model::prelude::interaction::application_command::ApplicationCommandInteraction;


pub fn register(command: &mut CreateApplicationCommand) -> &mut CreateApplicationCommand {
    command
        .name("config")
        .description("Gérer la configuration du serveur")
        .create_option(|option| {
            option
                .name("logs")
                .description("Définir le salon des logs")
                .kind(CommandOptionType::SubCommand)
                .create_sub_option(|option| {
                    option
                        .name("salon")
                        .description("Le salon où envoyer les logs")
                        .kind(CommandOptionType::Channel)
                })
        })
        .create_option(|option| {
            option
                .name("bienvenue")
                .description("Définir le salon de bienvenu")
                .kind(CommandOptionType::SubCommand)
                .create_sub_option(|option| {
                    option
                        .name("salon")
                        .description("Le salon où envoyer les messages de bienvenue")
                        .kind(CommandOptionType::Channel)
                })
                .create_sub_option(|option| {
                    option
                        .name("message")
                        .description("Le message de bienvenue")
                        .kind(CommandOptionType::String)
                })
        })
        .create_option(|option| {
            option
                .name("nouveau")
                .description("Définir le rôle des nouveaux")
                .kind(CommandOptionType::SubCommand)
                .create_sub_option(|option| {
                    option
                        .name("role")
                        .description("Le rôle à donner aux nouveaux membres")
                        .kind(CommandOptionType::Role)
                })
        })
        .create_option(|option| {
            option
                .name("annonce")
                .description("Définir le salon des annonces de niveau")
                .kind(CommandOptionType::SubCommand)
                .create_sub_option(|option| {
                    option
                        .name("salon")
                        .description("Le salon où envoyer les annonces de niveau")
                        .kind(CommandOptionType::Channel)
                })
        })
}


pub async fn run(ctx: &Context, command: &ApplicationCommandInteraction) {
    match command.data.options.get(0).unwrap().name.as_str() {
        "logs" => logs::run(ctx, command).await,
        "bienvenue" => welcome::run(ctx, command).await,
        "nouveau" => newcomer::run(ctx, command).await,
        "annonce" => announce::run(ctx, command).await,

        _ => println!("Unknown command: {}", command.data.name),
    }
}