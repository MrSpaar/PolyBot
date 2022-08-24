from discord.ext import commands
from discord.utils import get

from datetime import datetime, timedelta
from unicodedata import normalize
from inspect import Parameter
from aiohttp import ClientSession
from typing import Union

async def get_json(link: str, headers: dict = None, json: bool = True) -> Union[dict, str]:
    async with ClientSession() as s:
        async with s.get(link, headers=headers) as resp:
            return await resp.json() if json else await resp.text()

def normalize_string(s: str) -> str:
    return normalize(u'NFKD', s).encode('ascii', 'ignore').decode('utf8')

def now(utc: bool = False) -> datetime:
    if utc:
        return datetime.utcnow()
    return datetime.utcnow() + timedelta(hours=2)

def has_higher_perms() -> None:
    async def extended_check(ctx: commands.Context) -> bool:
        args = ctx.message.content.split()
        member = get(ctx.guild.members, id=int(args[1].strip('<@!>')))

        if not member:
            raise commands.MissingRequiredArgument(Parameter('member', Parameter.POSITIONAL_ONLY))

        author_perms = ctx.author.guild_permissions
        member_perms = member.guild_permissions

        if author_perms.administrator and not member_perms.administrator:
            return True
        elif author_perms.manage_roles and not member_perms.manage_roles:
            return True
        elif author_perms.manage_guild and not member_perms.manage_guild:
            return True
        elif author_perms.ban_members and not member_perms.ban_members:
            return True
        elif author_perms.kick_members and not member_perms.kick_members:
            return True
        elif author_perms.manage_messages and not member_perms.manage_messages:
            return True
        else:
            raise commands.MissingPermissions('')

    return commands.check(extended_check)

def vc_check():
    async def extended_check(ctx: commands.Context) -> bool:
        if not ctx.guild or not ctx.author.voice:
            raise commands.CommandInvokeError('channel')

        entry = await ctx.bot.db.pending.find({'guild_id': ctx.guild.id, 'voc_id': ctx.author.voice.channel.id})
        if not entry:
            raise commands.CommandInvokeError('Not temp')

        owner = ctx.guild.get_member(entry['owner'])
        if ctx.author != owner and not ctx.author.guild_permissions.manage_channels:
            raise commands.CommandInvokeError('Not owner')

        return True
    return commands.check(extended_check)
