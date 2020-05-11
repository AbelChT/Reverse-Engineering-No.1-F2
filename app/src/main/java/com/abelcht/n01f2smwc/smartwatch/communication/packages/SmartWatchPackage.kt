package com.abelcht.n01f2smwc.smartwatch.communication.packages

open class SmartWatchPackage(private val messageType: Byte, private val content: Array<Byte>) {
    fun getPackage(): Array<Byte> {
        val headerField1: Byte = 0xa9.toByte()
        val headerField2: Byte = messageType
        val headerField3: Byte = 0x00.toByte()
        val headerField4: Byte = content.size.toByte()

        // Save fields in theirs positions
        val packageFields: Array<Byte> =
            arrayOf(headerField1, headerField2, headerField3, headerField4) + content

        // Calculate CRC
        val checkSum: Byte = packageFields.sum().toByte()

        return packageFields + arrayOf(checkSum)
    }
}