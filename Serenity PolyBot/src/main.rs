mod modules;

use std::env::var;
use dotenv::dotenv;
use sqlx::{migrate, Pool, Sqlite};
use sqlx::sqlite::SqlitePoolOptions;
use crate::modules::search::twitch::OAuth;

use serenity::model::id::GuildId;
use serenity::model::gateway::Ready;
use serenity::{async_trait, Client};
use serenity::client::{Context, EventHandler};
use serenity::prelude::{GatewayIntents, TypeMapKey};
use serenity::model::application::interaction::Interaction;


pub struct DbPool;
pub struct Handler;

impl TypeMapKey for DbPool {
    type Value = Pool<Sqlite>;
}

#[async_trait]
impl EventHandler for Handler {
    async fn ready(&self, ctx: Context, ready: Ready) {
        println!("{} is connected!", ready.user.name);

        let guild_id = GuildId(
            var("GUILD_ID")
                .expect("Expected GUILD_ID in environment")
                .parse()
                .expect("GUILD_ID must be an integer"),
        );

        guild_id.set_application_commands(&ctx.http, modules::register)
            .await
            .expect("Failed to register application modules");
    }

    async fn interaction_create(&self, ctx: Context, interaction: Interaction) {
        if let Interaction::ApplicationCommand(command) = interaction {
            modules::run(ctx, command).await;
        }
    }
}


#[tokio::main]
async fn main() {
    dotenv().ok();

    let db = SqlitePoolOptions::new()
        .max_connections(5)
        .connect(&var("DATABASE_PATH").expect("DB_PATH must be set"))
        .await
        .expect("Failed to connect to database");

    migrate!("./migrations")
        .run(&db)
        .await
        .expect("Failed to run migrations");

    let mut client = Client::builder(
        var("DISCORD_TOKEN").expect("DISCORD_TOKEN must be set"),
        GatewayIntents::empty(),
    )
        .event_handler(Handler)
        .await
        .expect("Failed to create client");

    for guild_id in client.cache_and_http.http.get_guilds(None, None).await.unwrap() {
        let id = guild_id.id.0 as i64;

        sqlx::query!("INSERT OR IGNORE INTO guilds (id) VALUES (?)", id)
            .execute(&db)
            .await.unwrap();
    }

    {
        let mut data = client.data.write().await;

        data.insert::<OAuth>(OAuth {
            access_token: String::new(),
            expires_in: 0,
        });

        data.insert::<DbPool>(db);
    }

    if let Err(why) = client.start().await {
        println!("Client error: {:?}", why);
    }
}
