pub mod global;
pub mod perso;

use sqlx::Row;
use sqlx::sqlite::SqliteRow;
use serenity::client::Context;
use serenity::builder::{CreateApplicationCommand, CreateEmbed};
use serenity::model::application::command::CommandOptionType;
use serenity::model::application::interaction::application_command::ApplicationCommandInteraction;

pub fn register(command: &mut CreateApplicationCommand) -> &mut CreateApplicationCommand {
    command
        .name("classement")
        .description("Base des commandes de classement")
        .create_option(|option| {
            option
                .name("global")
                .description("Classement global du serveur")
                .kind(CommandOptionType::SubCommand)
        })
        .create_option(|option| {
            option
                .name("perso")
                .description("Afficher le classement d'un membre")
                .kind(CommandOptionType::SubCommand)
                .create_sub_option(|option| {
                    option
                        .name("membre")
                        .description("Le membre dont afficher le classement")
                        .kind(CommandOptionType::User)
                })
        })
}

pub async fn run(ctx: &Context, command: &ApplicationCommandInteraction) {
    match command.data.options.get(0).unwrap().name.as_str() {
        "global" => global::run(ctx, command).await,
        "perso" => perso::run(ctx, command).await,
        _ => println!("Unknown command: {}", command.data.name),
    }
}


pub fn to_progress_bar(level: u32, xp: u32, length: usize) -> String {
    let mut bar = String::new();
    let next_cap = 5 * level*level + 50*level + 100;

    let filled = (length as u32 * xp / next_cap) as usize;
    let percent = (100 * xp / next_cap) as usize;

    bar.push_str(&"🟩".repeat(filled));
    bar.push_str(&"⬛".repeat(length - filled));
    bar.push_str(&format!(" {}%", percent));

    return bar;
}


pub trait ProcessRows {
    fn process_rows(&mut self, rows: &Vec<SqliteRow>) -> &mut Self;
}

impl ProcessRows for CreateEmbed {
    fn process_rows(&mut self, rows: &Vec<SqliteRow>) -> &mut Self {
        let mut names = String::new();
        let mut levels = String::new();
        let mut xps = String::new();

        for row in rows {
            let xp = row.get::<u32, usize>(2);
            let level = row.get::<u32, usize>(1);

            names.push_str(&format!("<@{}>\n", row.get::<String, usize>(0)));
            levels.push_str(&format!("{}\n", level));
            xps.push_str(&format!("{}\n", to_progress_bar(level, xp, 6)));
        }

        self
            .field("Noms", names, true)
            .field("Niveaux", levels, true)
            .field("Expérience", xps, true)
    }
}