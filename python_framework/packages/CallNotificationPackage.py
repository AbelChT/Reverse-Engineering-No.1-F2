from packages.SmartWatchPackage import SmartWatchPackage


class CallNotificationPackage(SmartWatchPackage):
    def __init__(self):
        message_type = 0x19
        content = [0x00]
        super().__init__(message_type, content)
