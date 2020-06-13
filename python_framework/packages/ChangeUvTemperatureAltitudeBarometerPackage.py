from datetime import datetime

from packages.SmartWatchPackage import SmartWatchPackage


class ChangeUvTemperatureAltitudeBarometerPackage(SmartWatchPackage):
    def __init__(self, uv: int, temperature: int, altitude: int, barometer: int):
        # Set temperature
        if 0 <= temperature <= 99:
            temperature_fields = [(temperature * 10) % 256, int((temperature * 10) / 256) % 256]
        else:
            temperature_fields = [0x00, 0x00]

        # Set UV
        if 1 <= uv <= 5:
            uv_fields = [uv]
        elif uv == 0:
            # Looks like a workaround
            uv_fields = [0x06]
        else:
            uv_fields = [0x01]

        # Set altitude
        if 0 <= altitude <= 9999:
            altitude_fields = [altitude % 256, int(altitude / 256) % 256]
        else:
            altitude_fields = [0x00, 0x00]

        # Set barometer
        if 0 <= barometer <= 299999:
            barometer_fields = [barometer % 256, int(barometer / 256) % 256, int(barometer / (256 ** 2)) % 256, 0x00]
        else:
            barometer_fields = [0x00, 0x00, 0x00, 0x00]

        message_type = 0x1b
        content = barometer_fields + altitude_fields + temperature_fields + uv_fields
        super().__init__(message_type, content)
