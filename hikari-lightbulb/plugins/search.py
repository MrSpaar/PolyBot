import hikari as hk
import lightbulb as lb

from core.funcs import api_call, get_oauth
from datetime import datetime, timedelta
import matplotlib.pyplot as plt
from io import BytesIO
from os import environ

plugin = lb.Plugin("Recherche")


@plugin.command()
@lb.command("recherche", "Groupes de commandes en rapport avec la recherche")
@lb.implements(lb.SlashCommandGroup)
async def search(_):
    pass


@search.child
@lb.option("recherche", "Les mots-clés pour affiner la recherche", modifier=lb.OptionModifier.GREEDY, required=False)
@lb.option("categorie", "La catégorie dans laquelle rechercher des streams")
@lb.command("twitch", "Rechercher des streams Twitch")
@lb.implements(lb.SlashSubCommand)
async def twitch(ctx: lb.Context):
    count = 100 if ctx.options.recherche else 10
    query = f"https://api.twitch.tv/helix/search/channels?query={ctx.options.categorie}&first={count}&live_only=true"

    if datetime.now() > plugin.bot.twitch["expire"]:
        plugin.bot.twitch = await get_oauth()

    headers = {
        "Client-ID": environ["TWITCH_CLIENT"],
        "Authorization": f"Bearer {plugin.bot.twitch['token']}",
    }

    resp = (await api_call(query, headers))["data"]

    embed = hk.Embed(color=0x3498DB)
    embed.set_author(name=f"Twitch - {resp[0]['game_name']}", icon="https://i.imgur.com/gArdgyC.png")

    if not ctx.options.recherche:
        streams = resp[:10]
    else:
        streams = filter(
            lambda s: any(key in s["title"].lower() for key in ctx.options.recherche.split()),
            resp[:100]
        )

    for stream in streams:
        value = f"[{stream['title']}](https://www.twitch.tv/{stream['broadcaster_login']})"
        embed.add_field(name=stream["display_name"], value=value, inline=True)

    if len(embed.fields) == 0:
        embed.add_field(name="\u200b", value="Aucuns streams trouvés", inline=True)

    await ctx.respond(embed=embed)


@search.child
@lb.option("recherche", "Le nom de l'article Wikipedia", modifier=lb.OptionModifier.CONSUME_REST)
@lb.command("wikipedia", "Rechercher des articles wikipedia")
@lb.implements(lb.SlashSubCommand)
async def wikipedia(ctx: lb.Context):
    query = f"https://fr.wikipedia.org/w/api.php?action=opensearch&search={ctx.options.recherche}&namespace=0&limit=1"
    resp = list(await api_call(query))
    title, url = resp[1][0], resp[3][0]

    query = f"https://fr.wikipedia.org/w/api.php?format=json&action=query&prop=extracts|pageimages&exintro&explaintext&redirects=1&titles={title}"
    resp = dict(await api_call(query))["query"]["pages"]
    data = next(iter(resp.values()))

    desc = f"{data['extract']} [Lire l'article]({url})"
    thumbnail = data["thumbnail"]["source"] if "thumbnail" in data.keys() else ""

    embed = (
        hk.Embed(color=0x546E7A, description=desc)
        .set_author(name=f"Wikipedia - {title}", icon="https://i.imgur.com/nDTQgbf.png")
        .set_thumbnail(thumbnail)
    )

    await ctx.respond(embed=embed)


@search.child
@lb.option("nom", "Le nom de l'anime dont tu veux les informations", modifier=lb.OptionModifier.CONSUME_REST,)
@lb.command("anime", "Rechercher des animes")
@lb.implements(lb.SlashSubCommand)
async def anime(ctx: lb.Context):
    resp = await api_call(f"https://kitsu.io/api/edge/anime?filter[text]={ctx.options.nom}")
    data = resp["data"][0]
    anime = data["attributes"]

    end = (
        datetime.strptime(anime["endDate"], "%Y-%m-%d").strftime("%d/%m/%Y")
        if anime["endDate"]
        else "En cours"
    )
    ep = f"{anime['episodeCount']} épisodes" if anime["episodeCount"] else "En cours"
    h, m = divmod(int(anime["totalLength"]), 60)

    diff = f"{datetime.strptime(anime['startDate'], '%Y-%m-%d').strftime('%d/%m/%Y')} → {end}"

    embed = (
        hk.Embed(color=0x546E7A, description=anime["synopsis"])
        .add_field(name="🥇 Score", value=f"{anime['averageRating']}/100", inline=True)
        .add_field(name="🖥️ Épisodes", value=f"{ep} ({h:d}h{m:02d}min)", inline=True)
        .add_field(name="📅 Diffusion", value=diff, inline=True)
        .set_author(name=f"Anime - {anime['titles']['en_jp']}", icon=anime["posterImage"]["tiny"])
    )

    await ctx.respond(embed=embed)


@search.child
@lb.option("ville", "Le nom de la ville dont tu veux la météo", modifier=lb.OptionModifier.CONSUME_REST)
@lb.command("meteo", "Donne la météo d'une ville sur un jour")
@lb.implements(lb.SlashSubCommand)
async def meteo(ctx: lb.Context):
    query = f"https://api.openweathermap.org/data/2.5/forecast?q={ctx.options.ville}&units=metric&APPID={environ['WEATHER_TOKEN']}"
    resp = await api_call(query)

    if resp["cod"] != '200':
        embed = hk.Embed(color=0xE74C3C, description="❌ Ville introuvable ou inexistante")
        return await ctx.respond(embed=embed, flags=hk.MessageFlag.EPHEMERAL)

    today, now = resp["list"][0], datetime.now()
    info = {
        "wind": f"{today['wind']['speed']} km/h",
        "humidity": f"{today['main']['humidity']} %",
        "ID": f'{today["weather"][0]["icon"]}.png',
        "rain": f"{round(today['rain']['3h']/3, 2)} mm/h" if "rain" in today.keys() else "0 mm/h",
    }

    data = {entry["dt_txt"][:10]: [] for entry in resp["list"]}
    for entry in resp["list"]:
        temp = [entry["dt_txt"][11:-6] + "h", entry["main"]["temp"]]
        data[entry["dt_txt"][:10]].append(temp)

    days = [now.strftime("%Y-%m-%d"), (now + timedelta(hours=24)).strftime("%Y-%m-%d")]
    data = [item for d, l in data.items() if d in days for item in l]
    hours, temps = [], []

    for hour in data:
        if hour[0] not in hours:
            hours.append(hour[0])
            temps.append(hour[1])

    fig, ax = plt.subplots()
    ax.plot(hours, temps, color="#feca57", marker="o", lw=2, ls="--")

    ax.grid()
    ax.fill_between(hours, 0, temps, alpha=0.3, color="#1dd1a1")
    ax.set(xlim=(0, len(hours) - 1), ylim=(0, None), xticks=hours)
    ax.tick_params(axis="x", colors="white", pad=5)
    ax.tick_params(axis="y", colors="white")
    ax.spines["bottom"].set_color("white")
    ax.spines["top"].set_color("#2F3136")
    ax.spines["right"].set_color("#2F3136")
    ax.spines["left"].set_color("white")
    ax.set_facecolor("#2F3136")
    fig.patch.set_facecolor("#2F3136")

    stream = BytesIO()
    plt.savefig(stream, bbox_inches="tight")
    stream.seek(0)

    embed = (
        hk.Embed(title=f"🌦️ Prévisions météo à {ctx.options.ville.title()}", color=0x3498DB)
        .add_field(name="\u200b", value=f"Vent : {info['wind']}", inline=True)
        .add_field(name="\u200b", value=f"Humidité : {info['humidity']}", inline=True)
        .add_field(name="\u200b", value=f"Pluie : {info['rain']}", inline=True)
        .set_image(stream)
        .set_thumbnail("https://openweathermap.org/img/w/" + info["ID"])
    )

    await ctx.respond("\u200b")
    await ctx.edit_last_response(embed=embed)


def load(bot):
    bot.add_plugin(plugin)
