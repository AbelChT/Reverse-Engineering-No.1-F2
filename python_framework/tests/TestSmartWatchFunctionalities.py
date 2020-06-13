import time
import unittest
from datetime import datetime

import pygatt

from services.ReceivedMessagesProcessor import ReceivedMessagesProcessor
from config import smartwatch_MAC, bluetooth_address_of_my_smartwatch
from packages.CallNotificationPackage import CallNotificationPackage
from packages.ChangeDateTimePackage import ChangeDateTimePackage
from packages.ChangeUvTemperatureAltitudeBarometerPackage import ChangeUvTemperatureAltitudeBarometerPackage
from packages.ConfigurePackage import ConfigurePackage
from packages.DisableAlarmPackage import DisableAlarmPackage
from packages.EnableAlarmPackage import EnableAlarmPackage
from packages.MessageNotificationPackage import MessageNotificationPackage
from packages.PhotoModePackage import PhotoModePackage
from packages.SearchNotificationPackage import SearchNotificationPackage


class TestSmartWatchFunctionalities(unittest.TestCase):
    def setUp(self):
        """
        Setup the SmartWatch
        """
        self.__bluetooth_adapter = pygatt.GATTToolBackend()
        self.__bluetooth_adapter.start()

        # Connect to the device
        while True:
            try:
                self.__bluetooth_device = self.__bluetooth_adapter.connect(smartwatch_MAC, timeout=3)
                break
            except pygatt.exceptions.NotConnectedError:
                print('Waiting...')

        # Configure message receiver
        self.__received_messages_processor = ReceivedMessagesProcessor()
        self.__received_messages_processor.start()

        self.__bluetooth_device.subscribe("c3e6fea2-e966-1000-8000-be99c223df6a",
                                          lambda handler, value: self.__received_messages_processor.add_message(handler,
                                                                                                                value))

        # Configure the device
        self.__bluetooth_device.char_write_handle(0x15, bytearray([0x00, 0x01]), False)
        self.__bluetooth_device.char_write_handle(0x12, bytearray(
            ConfigurePackage(bluetooth_address_of_my_smartwatch).get_package()), False)

    def tearDown(self):
        """
        Close communication with the SmartWatch
        """
        # Stop adapter
        self.__bluetooth_adapter.stop()

        # Stop message processor
        self.__received_messages_processor.stop()

    def test_change_date_time(self):
        # Change date and time
        date_to_set = datetime(2020, 5, 2, 21, 24, 0, 0)
        package = ChangeDateTimePackage(date_to_set)
        self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)

    def test_change_uv_temperature_altitude_barometer(self):
        # Change UV, temperature, altitude and barometer
        uv = 5
        altitude = 9999
        barometer = 299999
        temperature = 39

        package = ChangeUvTemperatureAltitudeBarometerPackage(uv, temperature, altitude, barometer)
        self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)

    def test_enable_alarm(self):
        # Set alarm
        hour = 20
        minutes = 41
        seconds = 0  # Value not used in my SmartWatch

        package = EnableAlarmPackage(hour, minutes, seconds)
        self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)

    def test_disable_alarm(self):
        # Disable alarm
        package = DisableAlarmPackage()
        self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)

    def test_call_notification(self):
        # Send call notification
        package = CallNotificationPackage()
        self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)

    def test_message_notification(self):
        # Send message notification
        package = MessageNotificationPackage()
        self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)

    def test_search_notification(self):
        # Send search notification
        package = SearchNotificationPackage()
        self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)

    def test_receive_pedometer(self):
        # Receive pedometer info
        # Warning: You must move the watch to simulate you are walking
        time.sleep(50)

    def test_receive_photo_action(self):
        # Receive photo action
        # Set photo mode
        package = PhotoModePackage(True)
        self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)

        time.sleep(30)

        # Unset photo mode
        package = PhotoModePackage(False)
        self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)


if __name__ == '__main__':
    unittest.main()
