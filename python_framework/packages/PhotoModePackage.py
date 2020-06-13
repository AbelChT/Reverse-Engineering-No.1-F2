from packages.SmartWatchPackage import SmartWatchPackage


class PhotoModePackage(SmartWatchPackage):
    def __init__(self, enable: bool):
        message_type = 0x1c
        content = [0x01 if enable else 0x00]
        super().__init__(message_type, content)
