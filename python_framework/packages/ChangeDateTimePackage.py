from datetime import datetime

from packages.SmartWatchPackage import SmartWatchPackage


class ChangeDateTimePackage(SmartWatchPackage):
    def __init__(self, date: datetime):
        # Package data
        hour: int = date.hour
        minute: int = date.minute
        second: int = date.second
        day: int = date.day
        month: int = date.month
        year: int = date.year - 2000

        message_type = 0x01
        content = [year, month, day, hour, minute, second]
        super().__init__(message_type, content)
