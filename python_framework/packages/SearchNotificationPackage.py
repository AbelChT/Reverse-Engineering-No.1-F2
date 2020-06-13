from packages.SmartWatchPackage import SmartWatchPackage


class SearchNotificationPackage(SmartWatchPackage):
    def __init__(self):
        message_type = 0x0e
        content = []
        # Although 0x19 is the op code for the messages, this package has the same effect as the package used
        # to search for the bracelet. Must be a bug in the implementation of the bracelet
        # message_type = 0x19
        # content = []
        super().__init__(message_type, content)
