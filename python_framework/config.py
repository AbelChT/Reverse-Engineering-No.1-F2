# Mac of the SmartWatch
bluetooth_address_of_my_smartwatch = [0xa9, 0xbc, 0x7a, 0x8e, 0xf0, 0x1d]

# Mac of the SmartWatch as string
smartwatch_MAC = ':'.join(["{:02x}".format(i) for i in bluetooth_address_of_my_smartwatch])

