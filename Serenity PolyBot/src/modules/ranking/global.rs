use crate::DbPool;
use crate::modules::respond;
use crate::modules::ranking::ProcessRows;

use serenity::prelude::Context;
use serenity::model::prelude::interaction::application_command::ApplicationCommandInteraction;

pub async fn run(ctx: &Context, command: &ApplicationCommandInteraction) {
    let data = ctx.data.read().await;
    let db = data.get::<DbPool>().unwrap();

    let guild_id = command.guild_id.unwrap().to_string();

    let rows = sqlx::query("
        SELECT id, level, xp, ROW_NUMBER() OVER (ORDER BY xp DESC) AS rank
        FROM users WHERE guild = ?1 LIMIT 10
    ")
        .bind(guild_id)
        .fetch_all(db)
        .await.unwrap();

    if rows.is_empty() {
        return respond(&ctx, &command, true, |e| { e
            .color(0xE74C3C)
            .description("Aucun membre n'a encore gagné d'expérience sur ce serveur")
        }).await;
    }

    respond(&ctx, &command, true, |e| { e
        .color(0x3498DB)
        .footer(|f| f.text("Classement global"))
        .process_rows(&rows)
    }).await;
}
