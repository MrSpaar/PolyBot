from discord import Embed
from discord.ext.commands import Context
from discord.ext import commands

from core.cls import Bot
from difflib import get_close_matches as gcm


class ErrorHandler(commands.Cog):
    def __init__(self, bot: Bot):
        self.bot = bot

    @commands.Cog.listener()
    async def on_command_error(self, ctx: Context, error):
        closest = gcm(ctx.message.content.split()[0][1:], [cmd.name for cmd in self.bot.commands])

        handled = {
            commands.MissingPermissions: "❌ Tu n'as pas la permission de faire ça",
            commands.BotMissingPermissions: "❌ Je n'ai pas la permission de faire ça",
            commands.MissingRequiredArgument: f"❌ Il manque au moins un argument : `{getattr(getattr(error, 'param', ''), 'name', '')}`",
            commands.MemberNotFound: '❌ Membre inexistant',
            commands.PartialEmojiConversionFailure: "❌ Cette commande ne marche qu'avec les emojis custom",
            commands.CommandNotFound: f"❌ Commande inexistante{'' if not closest else ', peut-être voulais-tu utiliser `' + closest[0] + '` ?'}",
            commands.CheckFailure: "❌ Tu n'es pas le créateur de ce channel ou tu n'es pas connecté à un channel",
            commands.ChannelNotFound: '❌ Channel introuvable',
            commands.BadArgument: '❌ Les arguments doivent être des nombres entiers',
            commands.CommandOnCooldown: "❌ Commande en cooldown",
            commands.MaxConcurrencyReached: "❌ Il y a une limite d'une partie en même temps",
            commands.CheckFailure: "❌ Tu ne peux pas utiliser cette commande sur ce serveur",
            commands.NoPrivateMessage: "❌ Cette commande n'est utilisable que sur un serveur",
            commands.CommandInvokeError: {
                'Not temp': "❌ Tu n'es pas dans un channel temporaire",
                'Not owner': "❌ Tu n'es pas le créateur de ce channel",
                'channel': "❌ Tu n'es connecté à aucun channel",
                'string index': '❌ Erreur dans la conversion',
                'list index': "❌ Recherche invalide, aucun résultat trouvé",
                'UnknownObjectException': "❌ Recherche invalide, aucun résultat trouvé",
                'not enough values to unpack': "❌ Lancer invalide",
                "KeyError: 'list'": "❌ Ville introuvable ou inexistante",
                'This video may be': "❌ Restriction d'âge, impossible de jouer la vidéo",
                'No video formats found': "❌ Aucun format vidéo trouvé, impossible de jouer la vidéo",
                'RecursionError': '❌ Trop de récursions, nombre trop grand',
                '4000': "❌ Mon message est trop long, impossible de l'envoyer",
            },
        }

        if type(error) not in handled:
            raise error

        error_entry = handled[type(error)]
        if isinstance(error_entry, dict):
            for key, value in error_entry.items():
                if key in str(error):
                    error_entry = value

        if isinstance(error_entry, dict):
            raise error

        if ctx.command and ctx.command.brief and not isinstance(error, commands.MissingPermissions):
            error_entry += f"\nExemple d'utilisation : `{self.bot.command_prefix}{ctx.command.full_parent_name+' ' if ctx.command.full_parent_name else ''}{ctx.command.name} {ctx.command.brief}`"

        embed = Embed(color=0xe74c3c, description=error_entry)
        await ctx.send(embed=embed)


def setup(bot):
    bot.add_cog(ErrorHandler(bot))
