pub mod config;
pub mod ranking;
pub mod moderation;
pub mod search;

use serenity::json::Value;
use serenity::prelude::Context;
use serenity::builder::{CreateApplicationCommands, CreateEmbed};
use serenity::model::application::interaction::InteractionResponseType;
use serenity::model::prelude::interaction::application_command::CommandData;
use serenity::model::application::interaction::application_command::ApplicationCommandInteraction;


pub fn register(commands: &mut CreateApplicationCommands) -> &mut CreateApplicationCommands {
    commands
        .create_application_command(|command| search::register(command))
        .create_application_command(|command| moderation::register(command))
        .create_application_command(|command| config::register(command))
        .create_application_command(|command| ranking::register(command))
}

pub async fn run(ctx: Context, command: ApplicationCommandInteraction) {
    match command.data.name.as_str() {
        "recherche" => search::run(&ctx, &command).await,
        "config" => config::run(&ctx, &command).await,
        "clear" => moderation::clear::run(&ctx, &command).await,
        "classement" => ranking::run(&ctx, &command).await,

        _ => println!("Unknown command: {}", command.data.name),
    }
}


pub trait GetArgument {
    fn get_raw(&self, index: usize, is_subcommand: bool) -> &Value;
    fn get_option(&self, index: usize, is_subcommand: bool) -> Option<&Value>;
}

impl GetArgument for CommandData {
    fn get_raw(&self, index: usize, is_subcommand: bool) -> &Value {
        if is_subcommand {
            self.options
                .get(0).unwrap()
                .options.get(index).unwrap()
                .value.as_ref().unwrap()
        } else {
            self.options
                .get(index).unwrap()
                .value.as_ref().unwrap()
        }
    }

    fn get_option(&self, index: usize, is_subcommand: bool) -> Option<&Value> {
        let option = if is_subcommand {
            self.options
                .get(0).unwrap()
                .options.get(index)
        } else {
            self.options.get(index)
        };

        if option.is_some() {
            option.unwrap().value.as_ref()
        } else {
            None
        }
    }
}


pub async fn respond<F>(ctx: &Context, command: &ApplicationCommandInteraction, ephemeral: bool, f: F)
    where F: FnOnce(&mut CreateEmbed) -> &mut CreateEmbed
{
    let mut create_embed = CreateEmbed::default();
    f(&mut create_embed);

    command.create_interaction_response(&ctx.http, |resp|
        resp
            .kind(InteractionResponseType::ChannelMessageWithSource)
            .interaction_response_data(|message|
                message
                    .ephemeral(ephemeral)
                    .set_embed(create_embed)
            )
    ).await.expect("Failed to send response");
}
