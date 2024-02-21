use crate::modules::{GetArgument, respond};

use serenity::Error;
use serenity::prelude::Context;
use serenity::model::prelude::interaction::application_command::ApplicationCommandInteraction;


pub async fn run(ctx: &Context, command: &ApplicationCommandInteraction) {
    let n = command.data.get_raw(0, false)
        .as_u64().unwrap();

    let messages = command.channel_id.messages(
        &ctx.http, |m| m.limit(n)
    ).await.expect("Failed to get messages");

    match command.channel_id.delete_messages(&ctx.http, messages).await {
        Ok(_) => respond(ctx, command, true, |e| e
            .color(0x2ECC71)
            .description("✅ Messages supprimés")
        ).await,
        Err(Error::Http(_)) => respond(ctx, command, true, |e| e
            .color(0xE74C3C)
            .description("❌ Je n'ai pas les permissions nécessaires")
        ).await,
        Err(_) => respond(ctx, command, true, |e| e
            .color(0xE74C3C)
            .description("❌ Une erreur est survenue")
        ).await
    };
}
