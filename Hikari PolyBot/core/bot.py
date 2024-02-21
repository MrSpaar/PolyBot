from hikari import Intents
from lightbulb import BotApp

from abc import ABC
from os import environ


class ExtendedBot(BotApp, ABC):
    def __init__(self):
        super().__init__(
            intents=Intents.ALL,
            token=environ["BOT_TOKEN"],
            logs="ERROR",
        )

        self.twitch = {}
