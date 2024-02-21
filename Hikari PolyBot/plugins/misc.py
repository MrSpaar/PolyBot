import hikari as hk
import lightbulb as lb

plugin = lb.Plugin("Divers")


@plugin.command()
@lb.option("texte", "Le texte au format Question | Option 1 | Option 2 | ...", modifier=lb.OptionModifier.CONSUME_REST,)
@lb.command("sondage", description="Faire un sondage (9 choix au maximum)")
@lb.implements(lb.SlashCommand)
async def sondage(ctx: lb.Context):
    items = [arg.strip() for arg in ctx.options.texte.split("|")]
    question = items[0]
    reactions = ["1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣"]

    embed = hk.Embed(title=f">> {question[0].upper() + question[1:]}", color=0x3498DB)
    embed.set_author(name=f"Sondage de {ctx.member.display_name}", icon=ctx.author.avatar_url)

    for i in range(1, len(items)):
        embed.add_field(
            name=f"{reactions[i-1]} Option n°{i}",
            value=f"```{items[i]}```",
            inline=False,
        )

    resp = await ctx.respond(embed=embed)
    message = await resp.message()

    for i in range(len(items[1:])):
        await message.add_reaction(reactions[i])


@plugin.command()
@lb.option("membre", "Le membre dont tu veux afficher l'image de profil", hk.User, default=None)
@lb.command("pp", "Afficher l'image de profil d'un membre")
@lb.implements(lb.SlashCommand)
async def pp(ctx: lb.Context):
    member = ctx.options.membre or ctx.options.target or ctx.member
    embed = hk.Embed(color=member.get_top_role().color).set_image(member.avatar_url)

    await ctx.respond(embed=embed)


@plugin.command()
@lb.command("Image de profil", "Afficher l'image de profil d'un membre")
@lb.implements(lb.UserCommand)
async def _pp(ctx: lb.Context):
    await pp(ctx)


@plugin.command()
@lb.option("emoji", "L'emoji que tu veux afficher", hk.Emoji)
@lb.command("emoji", description="Afficher l'image d'origine d'un emoji")
@lb.implements(lb.SlashCommand)
async def emoji(ctx: lb.Context):
    emoji = await lb.EmojiConverter(ctx).convert(ctx.options.emoji)

    embed = (
        hk.Embed(color=ctx.member.get_top_role().color)
        .set_image(emoji.url)
        .set_footer(text=f"<:{emoji.name}:{emoji.id}>")
    )

    await ctx.respond(embed=embed)


def load(bot):
    bot.add_plugin(plugin)
