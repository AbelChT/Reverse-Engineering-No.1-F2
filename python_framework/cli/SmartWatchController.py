import time
from cmd import Cmd

import re
from datetime import datetime

import pygatt

from packages.SearchNotificationPackage import SearchNotificationPackage
from services.ReceivedMessagesProcessor import ReceivedMessagesProcessor
from packages.CallNotificationPackage import CallNotificationPackage
from packages.ChangeDateTimePackage import ChangeDateTimePackage
from packages.ChangeUvTemperatureAltitudeBarometerPackage import ChangeUvTemperatureAltitudeBarometerPackage
from packages.ConfigurePackage import ConfigurePackage
from packages.DisableAlarmPackage import DisableAlarmPackage
from packages.EnableAlarmPackage import EnableAlarmPackage
from packages.MessageNotificationPackage import MessageNotificationPackage
from packages.PhotoModePackage import PhotoModePackage


class SmartWatchControllerShell(Cmd):
    def __init__(self):
        super().__init__()
        self.intro = 'Welcome to the SmartWatch controller shell. Type help or ? to list commands.\n'
        self.prompt = '(disconnected) '
        self.__bluetooth_adapter = pygatt.GATTToolBackend()
        self.__bluetooth_adapter.start()
        self.__bluetooth_device = None
        self.__is_connected = False

    def do_connect(self, arg: str):
        """
        Connect to the Smartwatch
        :type arg: Bluetooth address
        """
        if not self.__is_connected:
            # Check if address is valid
            bluetooth_address_re_pattern = "([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}"
            if re.match(bluetooth_address_re_pattern, arg):
                # Connect to the device
                i = 0
                max_iterations = 10
                while not self.__is_connected and i < max_iterations:
                    try:
                        self.__bluetooth_device = self.__bluetooth_adapter.connect(arg, timeout=3)
                        self.__is_connected = True
                    except pygatt.exceptions.NotConnectedError:
                        i += 1
                        print('Timeout: ', i, '/', max_iterations)

                if self.__is_connected:
                    self.prompt = '(connected) '
                    print("Successful connected to the device")

                    # Transform string to list
                    bluetooth_address_as_list = [int(arg[i * 3:i * 3 + 2], 16) for i in range(6)]

                    # Configure the device
                    self.__bluetooth_device.char_write_handle(0x15, bytearray([0x00, 0x01]), False)
                    self.__bluetooth_device.char_write_handle(0x12, bytearray(
                        ConfigurePackage(bluetooth_address_as_list).get_package()), False)
                else:
                    print("Can't connect to the device")
            else:
                print("You must provide a valid bluetooth address")
        else:
            print("You are already connected to a device")

    def do_change_date_time(self, arg):
        """
        Change date and time. Format: hour minute seconds day month year
        """
        parsed_arguments = self.__parse(arg)
        if self.__is_connected:
            # Change date and time
            date_to_set = datetime(parsed_arguments[5], parsed_arguments[4], parsed_arguments[3],
                                   parsed_arguments[0], parsed_arguments[1], parsed_arguments[2], 0)
            package = ChangeDateTimePackage(date_to_set)
            self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)
            print("Operation successful")
        else:
            print("Not device connected")

    def do_change_uv_temperature_altitude_barometer(self, arg):
        """
        Change uv, temperature, altitude and barometer. Format: uv temperature altitude barometer
        """
        parsed_arguments = self.__parse(arg)
        if self.__is_connected:
            # Change UV, temperature, altitude and barometer
            uv = parsed_arguments[0]
            altitude = parsed_arguments[2]
            barometer = parsed_arguments[3]
            temperature = parsed_arguments[1]

            package = ChangeUvTemperatureAltitudeBarometerPackage(uv, temperature, altitude, barometer)
            self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)
            print("Operation successful")
        else:
            print("Not device connected")

    def do_enable_alarm(self, arg):
        """
        Set alarm
        :type arg: New hour. Format: hour minutes
        """
        parsed_arguments = self.__parse(arg)
        if self.__is_connected:
            # Set alarm
            hour = parsed_arguments[0]
            minutes = parsed_arguments[1]
            seconds = 0  # Value not used in my SmartWatch

            package = EnableAlarmPackage(hour, minutes, seconds)
            self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)
            print("Operation successful")
        else:
            print("Not device connected")

    def do_disable_alarm(self, _):
        """
        Disable alarm
        """
        if self.__is_connected:
            # Disable alarm
            package = DisableAlarmPackage()
            self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)
            print("Operation successful")
        else:
            print("Not device connected")

    def do_call_notification(self, _):
        """
        Send call notification
        """
        if self.__is_connected:
            # Send call notification
            package = CallNotificationPackage()
            self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)
            print("Operation successful")
        else:
            print("Not device connected")

    def do_message_notification(self, _):
        """
        Send message notification
        """
        if self.__is_connected:
            # Send message notification
            package = MessageNotificationPackage()
            self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)
            print("Operation successful")
        else:
            print("Not device connected")

    def do_search_notification(self, _):
        """
        Send search notification
        """
        if self.__is_connected:
            # Send search notification
            package = SearchNotificationPackage()
            self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)
            print("Operation successful")
        else:
            print("Not device connected")

    def do_receive_photo_action(self, arg):
        """
        Receive photo action. Format: (seconds to wait)
        """
        parsed_arguments = self.__parse(arg)
        if self.__is_connected:
            # Configure message receiver
            received_messages_processor = ReceivedMessagesProcessor(False, True, False)
            received_messages_processor.start()

            self.__bluetooth_device.subscribe("c3e6fea2-e966-1000-8000-be99c223df6a",
                                              lambda handler, value: received_messages_processor.add_message(
                                                  handler,
                                                  value))

            # Receive photo action
            # Set photo mode
            package = PhotoModePackage(True)
            self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)

            time.sleep(parsed_arguments[0])

            # Unset photo mode
            package = PhotoModePackage(False)
            self.__bluetooth_device.char_write_handle(0x12, bytearray(package.get_package()), False)

            # Unsubscribe from handler
            self.__bluetooth_device.unsubscribe("c3e6fea2-e966-1000-8000-be99c223df6a")

            # Stop message processor
            received_messages_processor.stop()
            print("Operation successful")
        else:
            print("Not device connected")

    def do_receive_pedometer(self, arg):
        """
        Receive pedometer info. You must move the watch to simulate you are walking. Format: (seconds to wait)
        """
        parsed_arguments = self.__parse(arg)
        if self.__is_connected:
            # Configure message receiver
            received_messages_processor = ReceivedMessagesProcessor()
            received_messages_processor.start()

            self.__bluetooth_device.subscribe("c3e6fea2-e966-1000-8000-be99c223df6a",
                                              lambda handler, value: received_messages_processor.add_message(
                                                  handler,
                                                  value))

            time.sleep(parsed_arguments[0])

            # Unsubscribe from handler
            self.__bluetooth_device.unsubscribe("c3e6fea2-e966-1000-8000-be99c223df6a")

            # Stop message processor
            received_messages_processor.stop()
            print("Operation successful")
        else:
            print("Not device connected")

    def do_disconnect(self, _):
        """
        Disconnect from the Smartwatch
        """
        if self.__is_connected:
            self.__is_connected = False
            self.__bluetooth_device.disconnect()
            self.prompt = '(disconnected) '
            print("Successful disconnected")
        else:
            print("Not device connected")

    def do_exit(self, _):
        """
        Close the application
        """
        if self.__is_connected:
            self.__is_connected = False
            self.__bluetooth_device.disconnect()
        return True

    def __parse(self, arg):
        """
        Convert a series of zero or more numbers to an argument tuple
        """
        return tuple(map(int, arg.split()))
