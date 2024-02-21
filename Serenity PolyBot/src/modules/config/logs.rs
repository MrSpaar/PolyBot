use crate::DbPool;
use crate::modules::{GetArgument, respond};

use serenity::prelude::Context;
use serenity::model::prelude::interaction::application_command::ApplicationCommandInteraction;

pub async fn run(ctx: &Context, command: &ApplicationCommandInteraction) {
    let data = ctx.data.read().await;
    let db = data.get::<DbPool>().unwrap();

    let channel = if let Some(val) = command.data.get_option(0, true) {
        val.as_str().unwrap()
    } else { "0" };

    let guild_id = command.guild_id.unwrap().to_string();

    sqlx::query!(r#"
        UPDATE guilds
        SET logs_channel = ?1
        WHERE id = ?2"#,
        channel, guild_id
    ).execute(db).await.unwrap();

    respond(ctx, command, false, |e| {
        if channel != "0" {
            e.color(0x2ECC71);
            e.description(format!("✒️ Les logs seront envoyés dans <#{}>", channel))
        } else {
            e.color(0x808080);
            e.description("✒️ Les logs ont été désactivés")
        }
    }).await;
}