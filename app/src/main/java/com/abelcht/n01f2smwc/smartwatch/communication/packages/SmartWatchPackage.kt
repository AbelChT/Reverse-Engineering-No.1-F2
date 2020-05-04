package com.abelcht.n01f2smwc.smartwatch.communication.packages

@ExperimentalUnsignedTypes
open class SmartWatchPackage(private val messageType: UByte, private val content: Array<UByte>) {
    fun getPackage(): Array<UByte> {
        val headerField1: UByte = 0xa9u
        val headerField2: UByte = messageType
        val headerField3: UByte = 0x00u
        val headerField4: UByte = content.size.toUByte()

        // Save fields in theirs positions
        val packageFields: Array<UByte> =
            arrayOf(headerField1, headerField2, headerField3, headerField4) + content

        // Calculate CRC
        val checkSum: UByte = packageFields.sum().toUByte()

        return packageFields + arrayOf(checkSum)
    }
}