pub mod twitch;
pub mod wiki;

use serenity::client::Context;
use serenity::builder::CreateApplicationCommand;
use serenity::model::prelude::command::CommandOptionType;
use serenity::model::application::interaction::application_command::ApplicationCommandInteraction;


pub fn register(command: &mut CreateApplicationCommand) -> &mut CreateApplicationCommand {
    command
        .name("recherche")
        .description("Base des commandes de recherche")
        .create_option(|option| {
            option
                .name("twitch")
                .kind(CommandOptionType::SubCommand)
                .description("Rechercher des streams sur Twitch")
                .create_sub_option(|option| {
                    option
                        .name("categorie")
                        .kind(CommandOptionType::String)
                        .description("Catgérorie des streams")
                        .required(true)
                })
                .create_sub_option(|option| {
                    option
                        .name("filtres")
                        .kind(CommandOptionType::String)
                        .description("Mots clés pour filtrer les streams")
                        .required(false)
                })
        })
        .create_option(|option| {
            option
                .name("wiki")
                .kind(CommandOptionType::SubCommand)
                .description("Rechercher des articles Wikipedia")
                .create_sub_option(|option| {
                    option
                        .name("article")
                        .kind(CommandOptionType::String)
                        .description("Article à rechercher")
                        .required(true)
                })
        })
}


pub async fn run(ctx: &Context, command: &ApplicationCommandInteraction) {
    match command.data.options.get(0).unwrap().name.as_str() {
        "twitch" => twitch::run(ctx, command).await,
        "wiki" => wiki::run(ctx, command).await,
        _ => println!("Unknown command: {}", command.data.name),
    }
}