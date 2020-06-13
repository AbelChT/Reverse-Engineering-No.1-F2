from packages.SmartWatchPackage import SmartWatchPackage


class MessageNotificationPackage(SmartWatchPackage):
    def __init__(self):
        message_type = 0x19
        content = [0x01]
        super().__init__(message_type, content)
