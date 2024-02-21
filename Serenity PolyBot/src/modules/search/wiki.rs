use crate::modules::{GetArgument, respond};

use serde::Deserialize;
use std::collections::HashMap;
use serenity::prelude::Context;
use serenity::model::application::interaction::application_command::ApplicationCommandInteraction;


#[derive(Deserialize)]
struct Wikipedia {
    query: Query
}

#[derive(Deserialize)]
struct Query {
    pages: HashMap<i32, Page>
}

#[derive(Deserialize)]
struct Page {
    title: String,
    extract: String
}


pub async fn run(ctx: &Context, command: &ApplicationCommandInteraction) {
    let title = command.data.get_raw(0, true)
        .as_str().expect("Failed to get category");

    let url = format!("https://fr.wikipedia.org/w/api.php?format=json&action=query&prop=extracts|pageimages&exintro&explaintext&redirects=1&titles={}", title);

    let response = reqwest::get(&url)
        .await.expect("Failed to get response")
        .json::<Wikipedia>().await;

    match response {
        Ok(response) => {
            let page = response.query.pages.values().next().unwrap();

            respond(ctx, command, false, |e| e
                .color(0x3498DB)
                .description(&page.extract)
                .author(|a| a
                    .name(format!("Wikipedia - {}", &page.title))
                    .url(format!("https://fr.wikipedia.org/wiki/{}", page.title))
                    .icon_url("https://upload.wikimedia.org/wikipedia/en/thumb/8/80/Wikipedia-logo-v2.svg/1200px-Wikipedia-logo-v2.svg.png")
                )
            ).await;
        },

        Err(_) => respond(ctx, command, true, |e| { e
            .color(0xE74C3C)
            .description("Aucun résultat trouvé")
        }).await
    }
}
