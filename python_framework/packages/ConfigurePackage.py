from typing import List

from packages.SmartWatchPackage import SmartWatchPackage


class ConfigurePackage(SmartWatchPackage):
    def __init__(self, bluetooth_address: List[int]):
        message_type = 0x32

        content = [0x40, 0xe2, 0x01, 0x00, 0x00, 0x00] + bluetooth_address
        super().__init__(message_type, content)
