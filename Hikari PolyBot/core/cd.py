from hikari import Member, GatewayGuild

from datetime import timedelta, datetime


class Cooldown:
    def __init__(self, usages: int, seconds: int):
        self.cooldowns = {}
        self.usages = usages
        self.seconds = seconds

    @property
    def now(self) -> datetime:
        return datetime.utcnow() + timedelta(hours=2)

    def update_cooldown(self, member: Member, guild: GatewayGuild):
        if member.id not in self.cooldowns:
            self.cooldowns[member.id] = {
                guild.id: {
                    "usages": 0,
                    "cool": self.now + timedelta(seconds=self.seconds),
                }
            }
            return True

        if guild.id not in self.cooldowns[member.id]:
            self.cooldowns[member.id][guild.id] = {
                "usages": 0,
                "cool": self.now + timedelta(seconds=self.seconds),
            }
            return True

        if self.now > self.cooldowns[member.id][guild.id]["cool"]:
            self.cooldowns[member.id][guild.id]["usages"] = 0
            self.cooldowns[member.id][guild.id]["cool"] += timedelta(seconds=self.seconds)
            return True

        self.cooldowns[member.id][guild.id]["usages"] += 1

        if self.cooldowns[member.id][guild.id]["usages"] <= self.usages - 1:
            return True

        return False
