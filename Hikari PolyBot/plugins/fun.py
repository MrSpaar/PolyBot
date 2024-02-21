import hikari as hk
import lightbulb as lb
from hikari.impl import ActionRowBuilder

from games.hangman import Hangman
from games.chess import Chess
from random import randint, choice
from datetime import datetime
from asyncio import sleep

plugin = lb.Plugin("Jeux")


@plugin.command()
@lb.add_checks(lb.guild_only)
@lb.command("chess", "Base des commandes liées aux échecs")
@lb.implements(lb.SlashCommandGroup)
async def chess(_):
    pass


@chess.child
@lb.add_checks(lb.guild_only)
@lb.option("membre", "La personne contre qui tu veux jouer", hk.Member)
@lb.command("play", "Jouer aux échecs contre quelqu'un")
@lb.implements(lb.SlashSubCommand)
async def chess(ctx: lb.Context):
    member = ctx.options.membre or ctx.options.target
    if member.is_bot or member == ctx.member:
        return await ctx.respond("Tu ne peux pas jouer contre un bot ou contre toi-même")

    await ctx.respond("\u200b")
    await Chess(ctx).start()


@plugin.command()
@lb.command("Echecs", "Jouer aux échecs contre quelqu'un")
@lb.implements(lb.UserCommand)
async def _chess(ctx: lb.Context):
    await chess(ctx)


@plugin.command()
@lb.command("pendu", "Jouer au pendu")
@lb.implements(lb.SlashCommand)
async def pendu(ctx: lb.Context):
    await Hangman(ctx).start()


@chess.child
@lb.add_checks(lb.guild_only)
@lb.command("regles", "Message d'aide pour jouer aux échecs")
@lb.implements(lb.SlashSubCommand)
async def regles(ctx: lb.Context):
    embed = (
        hk.Embed(color=0x3498DB)
        .add_field(
            inline=False,
            name="Pour jouer",
            value="Envoie un message sous une des deux formes supportées :\n"
            + "    • SAN : représentation usuelle (`a4`, `Nf5`, ...)\n"
            + "    • UCI : représentation universelle `départarrivée` (`a2a4`, `b1c3`, ...)\n"
            + "\n Les promotions sous le format UCI se font en ajoutant un `q` à la fin (`a2a1q` par exemple)",
        )
        .add_field(
            inline=False,
            name="Autres fonctionnalités",
            value="Envoie `quit` pour abandonner la partie.\n",
        )
        .set_footer(text="⚠️ Ne pas mettre ! dans vos messages")
    )

    await ctx.respond(embed=embed)


@plugin.command()
@lb.option("face", "La face sur laquelle tu paries")
@lb.command("coinflip", "Jouer au pile ou face")
@lb.implements(lb.SlashCommand)
async def coinflip(ctx: lb.Context):
    result = choice(["Pile", "Face"])

    if ctx.options.face.title() not in ["Pile", "Face"]:
        color = 0xE74C3C
        desc = "❌ Tu dois entrer `pile` ou `face` !"
    elif ctx.options.face.title() in result:
        color = 0xF1C40F
        desc = f"🪙 {result} ! Tu as gagné."
    else:
        color = 0xE74C3C
        desc = f"🪙 {result} ! Tu as perdu."

    embed = hk.Embed(color=color, description=desc)
    await ctx.respond(embed=embed)


@plugin.command()
@lb.option("chaine", "Chaine de caractères sous la forme d'un lancé de dé Roll20 (2d10+3d20 par exemple)")
@lb.command("roll", "Faire une lancer de dés")
@lb.implements(lb.SlashCommand)
async def roll(ctx: lb.Context):
    try:
        content = ctx.options.chaine.split("+")
        rolls = [int(content.pop(i)) for i in range(len(content)) if content[i].isdigit()]

        for elem in content:
            n, faces = elem.split("d") if elem.split("d")[0] != "" else (1, elem[1:])
            rolls += [randint(1, int(faces)) for _ in range(int(n))]

        rolls_str = " + ".join([str(n) for n in rolls])
    except Exception:
        embed = hk.Embed(color=0xE74C3C, description="❌ Lancer invalide")
        return await ctx.respond(embed=embed)

    embed = hk.Embed(
        color=0xF1C40F,
        description=f"**🎲 Résultat du lancé :** {rolls_str} = **{sum(rolls)}**",
    )

    await ctx.respond(embed=embed)


@plugin.command()
@lb.command("reaction", "Tester son temps de réaction")
@lb.implements(lb.SlashCommand)
async def reaction(ctx: lb.Context):
    component = ActionRowBuilder()
    component.add_button(2, str(ctx.author.id)).set_label(
        "Appuie quand tu es prêt"
    ).add_to_container()
    message = await ctx.respond("\u200b", component=component)

    event = await ctx.bot.wait_for(
        hk.InteractionCreateEvent,
        predicate=lambda evt: evt.interaction.custom_id == str(ctx.author.id),
        timeout=None,
    )

    component = ActionRowBuilder()
    component.add_button(4, str(ctx.author.id)).set_label(
        "Appuie dès que je change de couleur"
    ).add_to_container()
    await event.interaction.create_initial_response(
        response_type=7, components=[component]
    )

    await sleep(randint(2, 10))
    temp = await ctx.get_channel().fetch_message(event.interaction.message.id)

    if not temp.components:
        return

    component = ActionRowBuilder()
    component.add_button(3, str(ctx.author.id)).set_label(
        "Maintenant !"
    ).add_to_container()
    await message.edit(component=component)
    start = datetime.now()

    event = await ctx.bot.wait_for(
        hk.InteractionCreateEvent,
        predicate=lambda evt: evt.interaction.custom_id == str(ctx.author.id),
        timeout=None,
    )
    td = datetime.now() - start
    td = round(td.seconds + td.microseconds / 1000000 - ctx.bot.heartbeat_latency, 3)

    embed = hk.Embed(color=0x3498DB, description=f"⏱️ Ton temps de réaction : `{td}` secondes")
    await event.interaction.create_initial_response(response_type=7, components=[], embed=embed)


@plugin.listener(hk.InteractionCreateEvent)
async def on_button_click(event):
    if isinstance(event.interaction, hk.CommandInteraction):
        return

    interaction = event.interaction
    button = interaction.message.components[0].components[0]

    if isinstance(button, hk.SelectMenuComponent):
        return

    if button.label == 'Appuie dès que je change de couleur' and button.style == 4:
        embed = hk.Embed(color=0xe74c3c, description='❌ Tu as appuyé trop tôt')
        return await event.interaction.create_initial_response(response_type=7, components=[], embed=embed)


def load(bot):
    bot.add_plugin(plugin)
