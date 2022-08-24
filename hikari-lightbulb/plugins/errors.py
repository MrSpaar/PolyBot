import hikari as hk
import lightbulb as lb

plugin = lb.Plugin("Erreurs")


@plugin.listener(lb.SlashCommandErrorEvent)
async def on_command_error(event):
    ctx, error = event.context, event.exception.__cause__

    if error is None:
        embed = hk.Embed(color=0xE74C3C, description="❌ Une erreur inattendue est survenue")
        return await ctx.respond(embed=embed, flags=hk.MessageFlag.EPHEMERAL)

    handled = {
        lb.errors.MissingRequiredPermission: "❌ Tu n'as pas la permission de faire ça",
        lb.errors.MissingRequiredRole: "❌ Tu n'as pas la permission de faire ça",
        lb.errors.BotMissingRequiredPermission: "❌ Je n'ai pas la permission de faire ça",
        lb.errors.CheckFailure: "❌ Tu n'as pas la permission de faire ça",
        lb.errors.OnlyInGuild: "❌ Cette commande n'est utilisable que sur un serveur",
        lb.errors.NotOwner: "❌ Seul le créateur du bot peut utiliser cette commande",
    }

    if type(error) not in handled:
        raise error

    embed = hk.Embed(color=0xE74C3C, description=handled[type(error)])
    await ctx.respond(embed=embed, flags=hk.MessageFlag.EPHEMERAL)


def load(bot):
    bot.add_plugin(plugin)
