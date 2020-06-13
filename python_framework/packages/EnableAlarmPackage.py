from packages.SmartWatchPackage import SmartWatchPackage


class EnableAlarmPackage(SmartWatchPackage):
    def __init__(self, hour: int, minutes: int, seconds: int):
        message_type = 0x02
        content = [hour, minutes, seconds]
        super().__init__(message_type, content)
