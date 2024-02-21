use crate::modules::{GetArgument, respond};

use dotenv::var;
use std::fmt::Error;
use reqwest::Client;
use serde::Deserialize;
use std::time::SystemTime;

use serenity::prelude::{Context, TypeMapKey};
use serenity::model::application::interaction::application_command::ApplicationCommandInteraction;


#[derive(Deserialize)]
pub struct OAuth {
    pub access_token: String,
    pub expires_in: u64,
}

impl TypeMapKey for OAuth {
    type Value = OAuth;
}

#[derive(Deserialize)]
struct Twitch {
    pub data: Vec<Stream>,
}

#[derive(Deserialize)]
struct Stream {
    pub title: String,
    pub game_name: String,
    pub display_name: String,
    pub broadcaster_login: String,
}


pub async fn run(ctx: &Context, command: &ApplicationCommandInteraction) {
    let category = command.data.get_raw(0, true)
        .as_str().unwrap();

    let mut filters: Vec<&str> = Vec::new();
    let mut limit = "10";

    if let Some(filters_value) = command.data.get_option(0, true) {
        filters = filters_value
            .as_str().unwrap()
            .split(" ").collect();

        limit = "100";
    }

    let mut data = ctx.data.write().await;
    let oauth = data.get_mut::<OAuth>().unwrap();

    if refresh_oauth(oauth).await.is_err() {
        respond(ctx, command, false, |e| e
            .color(0xE74C3C)
            .description("Impossible de se connecter à Twitch")
        ).await;

        return;
    }

    let resp = Client::new()
        .get(format!(
            "https://api.twitch.tv/helix/search/channels?live_only=true&query={}&first={}",
            category, limit
        ))
        .header("Client-ID", var("TWITCH_CLIENT").expect("TWITCH_CLIENT not set"))
        .header("Authorization", format!("Bearer {}", oauth.access_token))
        .send().await.expect("Failed to send Twitch request")
        .json::<Twitch>().await;

    match resp {
        Ok(resp) => {
            let game = resp.data[0].game_name.clone();
            let stream_iter = resp.data
                .into_iter()
                .filter(|s| matches_filters(s, &filters));

            respond(ctx, command, false, |e| {
                for stream in stream_iter {
                    e.field(
                        stream.display_name,
                        format!("[{}](https://twitch.tv/{})", stream.title, stream.broadcaster_login),
                        true
                    );
                }

                e.color(0x3498DB)
                 .author(|a| a
                     .name(format!("Twitch - {}", game))
                     .icon_url("https://assets.stickpng.com/images/580b57fcd9996e24bc43c540.png")
                 )
            }).await;
        }

        _ => respond(ctx, command, true, |e| e
            .color(0xE74C3C)
            .description("Erreur lors de la recherche")
        ).await
    }
}


fn matches_filters(stream: &Stream, filters: &Vec<&str>) -> bool {
    if filters.len() == 0 {
        return true;
    }

    for filter in filters {
        if stream.title.to_lowercase().contains(&filter.to_lowercase()) {
            return true;
        }
    }

    false
}

async fn refresh_oauth(oauth: &mut OAuth) -> Result<(), Error> {
    let twitch_client = var("TWITCH_CLIENT")
        .expect("TWITCH_CLIENT not set");

    let twitch_token = var("TWITCH_TOKEN")
        .expect("TWITCH_TOKEN not set");

    let now = SystemTime::now().duration_since(SystemTime::UNIX_EPOCH)
        .expect("Failed to get current time")
        .as_secs();

    if now < oauth.expires_in {
        return Ok(());
    }

    let resp = Client::new()
        .post("https://id.twitch.tv/oauth2/token")
        .form(&[
            ("grant_type", "client_credentials"),
            ("client_id", twitch_client.as_str()),
            ("client_secret", twitch_token.as_str()),
        ])
        .send().await
        .expect("Failed to send Twitch token request")
        .json::<OAuth>().await;

    match resp {
        Ok(resp) => {
            oauth.access_token = resp.access_token;
            oauth.expires_in = now + resp.expires_in;
            Ok(())
        },
        Err(_) =>  Err(Error)
    }
}
