from packages.SmartWatchPackage import SmartWatchPackage


class DisableAlarmPackage(SmartWatchPackage):
    def __init__(self):
        message_type = 0x02
        content = []
        super().__init__(message_type, content)
