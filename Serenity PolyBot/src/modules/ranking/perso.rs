use crate::DbPool;
use crate::modules::respond;
use crate::modules::ranking::to_progress_bar;

use sqlx::Row;
use serenity::prelude::Context;
use serenity::model::prelude::interaction::application_command::ApplicationCommandInteraction;

pub async fn run(ctx: &Context, command: &ApplicationCommandInteraction) {
    let data = ctx.data.read().await;
    let db = data.get::<DbPool>().unwrap();

    let guild_id = command.guild_id.unwrap().to_string();
    let user_id = command.user.id.to_string();

    let row = sqlx::query("
        SELECT level, xp, rank FROM (
            SELECT level, xp, id, ROW_NUMBER() OVER (ORDER BY xp DESC) AS rank
            FROM users WHERE guild = ?1
        ) WHERE id = ?2
    ")
        .bind(guild_id)
        .bind(user_id)
        .fetch_one(db)
        .await.unwrap();

    let xp = row.get::<u32, _>("xp");

    if xp == 0 {
        return respond(&ctx, &command, true, |e| { e
            .color(0xE74C3C)
            .description("Vous n'avez pas encore gagné d'expérience sur ce serveur")
        }).await;
    }

    let level = row.get::<u32, _>("level");
    let rank = row.get::<u32, _>("rank");

    respond(&ctx, &command, true, |e| { e
        .color(0x2ECC71)
        .author(|a| a
            .name(format!("Progression de {}", command.user.name))
            .icon_url(&command.user
                .avatar_url()
                .unwrap_or(command.user.default_avatar_url())
            )
        )
        .field(
            format!("Niveau {} - Rang {}", level, rank),
            to_progress_bar(level, xp, 14),
            false
        )
    }).await;
}