use crate::DbPool;
use crate::modules::{GetArgument, respond};

use serenity::prelude::Context;
use serenity::model::prelude::interaction::application_command::ApplicationCommandInteraction;

pub async fn run(ctx: &Context, command: &ApplicationCommandInteraction) {
    let data = ctx.data.read().await;
    let db = data.get::<DbPool>().unwrap();

    let role = if let Some(val) = command.data.get_option(0, true) {
        val.as_str().unwrap()
    } else { "0" };

    let guild_id = command.guild_id.unwrap().to_string();

    sqlx::query!(r#"
        UPDATE guilds
        SET newcomer_role = ?1
        WHERE id = ?2"#,
        role, guild_id
    ).execute(db).await.unwrap();

    respond(ctx, command, false, |e| {
        if role != "0" {
            e.color(0x2ECC71);
            e.description(format!("✒️ Les nouveaux recevront le rôle <&{}>", role))
        } else {
            e.color(0x808080);
            e.description("✒️ Les nouveaux ne recevront plus de rôle")
        }
    }).await;
}