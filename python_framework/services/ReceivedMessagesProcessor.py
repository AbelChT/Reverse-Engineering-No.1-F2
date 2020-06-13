import time
from multiprocessing import Process, Queue
from typing import List


class ReceivedMessagesProcessor(object):
    def __init__(self, display_pedometer_notifications: bool = True, display_photo_action_notifications: bool = True,
                 display_other_notifications: bool = True):
        self.__message_queue = Queue()
        self.__processing_process = Process(target=self.__message_processing, args=(
            self.__message_queue, display_pedometer_notifications, display_photo_action_notifications,
            display_other_notifications))

    @staticmethod
    def __message_processing(queue: Queue, display_pedometer_notifications: bool,
                             display_photo_action_notifications: bool,
                             display_other_notifications: bool):
        # True if we are reassembling a package
        is_reassembling_package: bool = False

        # Package that are been reassembled
        reassembled_package = []

        # Package handler
        package_handler = 0

        # Bytes to finish the package
        bytes_to_read = 0

        while True:
            (handle, value) = queue.get()
            value_as_list = [i for i in value]

            # Reassemble package
            if is_reassembling_package:
                reassembled_package = reassembled_package + value_as_list
                if bytes_to_read != len(value_as_list):
                    bytes_to_read = bytes_to_read - len(value_as_list)
                else:
                    is_reassembling_package = False
                    bytes_to_read = 0

            elif value_as_list[0] == 0xa9:
                # New package with correct header
                package_expected_size = 4 + value_as_list[3] + 1  # header size + content size + crc
                reassembled_package = value_as_list
                package_handler = handle
                if package_expected_size != len(value_as_list):
                    bytes_to_read = package_expected_size - len(value_as_list)
                    is_reassembling_package = True
            else:
                # New package with wrong header
                bytes_to_read = 0
                package_handler = 0
                reassembled_package = []
                print("Error in received message: Wrong header")

            if bytes_to_read < 0:
                # Error in message
                bytes_to_read = 0
                is_reassembling_package = False
                reassembled_package = []
                print("Error in received message: Exceeded length")

            elif not is_reassembling_package:
                # Analyze package
                if package_handler == 0x14 and reassembled_package[1] == 0x21:
                    # Pedometer package
                    if display_pedometer_notifications:
                        ReceivedMessagesProcessor.__process_pedometer_package(reassembled_package)

                elif package_handler == 0x14 and reassembled_package[1] == 0x0f:
                    # Photo action package
                    if display_photo_action_notifications:
                        print("Photo action message received")

                else:
                    if display_other_notifications:
                        print(time.asctime(), "Message received in handler:", hex(package_handler), "with value",
                              [hex(i) for i in reassembled_package])

    @staticmethod
    def __process_pedometer_package(value: List[int]):
        pedometer_date = value[4:7]
        number_of_packages = value[7]
        packages_start_index = [8 + i * (3 + 3 * 4) for i in range(number_of_packages)]

        packages = [(value[i: i + 3], value[i + 3: i + 7], value[i + 7:i + 11], value[i + 11:i + 14]) for i in
                    packages_start_index]

        print("Pedometer date: ", pedometer_date)

        for unknown_value, steps, kcal, km in packages:
            print("Pedometer info: steps ",
                  steps[0] + steps[1] * (2 ** 8) + steps[2] * (2 ** 16) + steps[3] * (2 ** 24), " kcal ",
                  kcal[0] + kcal[1] * (2 ** 8) + kcal[2] * (2 ** 16) + kcal[3] * (2 ** 24), " km ",
                  km[0] + km[1] * (2 ** 8) + km[2] * (2 ** 16), " unknown value ", unknown_value)

    def start(self):
        # Message analyzer process
        self.__processing_process.start()

    def stop(self):
        # End analysis process
        self.__processing_process.kill()
        self.__processing_process.join()

    def add_message(self, handler, value):
        self.__message_queue.put((handler, value))
