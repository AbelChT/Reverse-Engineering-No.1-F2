from typing import List


class SmartWatchPackage(object):
    def __init__(self, message_type: int, content: List[int]):
        self.__message_type = message_type
        self.__content = content

    def get_package(self) -> List[int]:
        header_field_1: int = 0xa9
        header_field_2: int = self.__message_type
        header_field_3: int = 0x00
        header_field_4: int = len(self.__content)

        # Save fields in theirs positions
        package_fields = [header_field_1, header_field_2, header_field_3, header_field_4] + self.__content

        # Calculate CRC
        crc: int = (sum(package_fields)) % (2 ** 8)

        # Add CRC at the end of the package
        package_fields.append(crc)
        return package_fields
