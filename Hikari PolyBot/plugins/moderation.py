import hikari as hk
import lightbulb as lb

from core.funcs import is_higher

plugin = lb.Plugin("Moderation")
plugin.add_checks(lb.guild_only)


@plugin.command()
@lb.add_checks(lb.has_guild_permissions(hk.Permissions.MANAGE_MESSAGES))
@lb.option("x", "Le nombre de messages à supprimer", int)
@lb.command("clear", "Supprimer plusieurs messages en même temps")
@lb.implements(lb.SlashCommand)
async def clear(ctx: lb.Context):
    channel, messages = ctx.get_channel(), []
    async for message in channel.fetch_history():
        if len(messages) >= ctx.options.x:
            break

        messages.append(message)

    await channel.delete_messages(messages)
    await ctx.respond(f'{len(messages)} message supprimés', flags=hk.MessageFlag.EPHEMERAL)


@plugin.command()
@lb.add_checks(is_higher | lb.has_guild_permissions(hk.Permissions.KICK_MEMBERS))
@lb.option("membre", "Le membre à exclure du serveur", hk.Member)
@lb.option("raison", "La raison de l'exclusion", modifier=lb.OptionModifier.CONSUME_REST, default="Pas de raison")
@lb.command("kick", "Exclure un membre du serveur")
@lb.implements(lb.SlashCommand)
async def kick(ctx: lb.Context):
    embed = hk.Embed(color=0x2ECC71, description=f"✅ {ctx.options.membre.mention} a été kick")

    await ctx.options.membre.kick(reason=ctx.options.raison)
    await ctx.respond(embed=embed)


@plugin.command()
@lb.add_checks(is_higher | lb.has_guild_permissions(hk.Permissions.BAN_MEMBERS))
@lb.option("membre", "Le membre à bannir", hk.Member)
@lb.option("raison", "La raison du bannissement", modifier=lb.OptionModifier.CONSUME_REST, default="Pas de raison")
@lb.command("ban", "Bannir un membre du serveur")
@lb.implements(lb.SlashCommand)
async def ban(ctx: lb.Context):
    embed = hk.Embed(color=0x2ECC71, description=f"✅ {ctx.options.membre.mention} a été ban")

    await ctx.options.membre.ban(reason=ctx.options.raison)
    await ctx.respond(embed=embed)


@plugin.command()
@lb.add_checks(lb.has_guild_permissions(hk.Permissions.BAN_MEMBERS))
@lb.option("id", "L'ID du membre à débannir", int)
@lb.option("raison", "La raison du débannissement", modifier=lb.OptionModifier.CONSUME_REST, default="Pas de raison")
@lb.command("unban", "Débannir un membre du serveur")
@lb.command("unban", "Révoquer un bannissement")
@lb.implements(lb.SlashCommand)
async def unban(ctx: lb.Context):
    try:
        guild = ctx.get_guild()
        user = await plugin.bot.rest.fetch_user(ctx.options.id)

        await guild.unban(user, reason=ctx.options.raison)

        embed = hk.Embed(color=0x2ECC71, description=f"✅ `{user.mention}` a été unban")
        await ctx.respond(embed=embed)
    except Exception:
        embed = hk.Embed(color=0xE74C3C, description="❌ L'utilisateur n'est pas banni de ce serveur")
        await ctx.respond(embed=embed)


def load(bot):
    bot.add_plugin(plugin)
