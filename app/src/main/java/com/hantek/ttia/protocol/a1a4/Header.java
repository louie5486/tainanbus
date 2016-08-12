package com.hantek.ttia.protocol.a1a4;

import com.hantek.ttia.module.BitConverter;

public class Header {

    /**
     * 協定識別碼
     */
    public String protocolID;

    /**
     * 協定版本
     */
    public int protocolVer;

    /**
     * 訊息代碼
     */
    public int messageID;

    /**
     * 公車業者代碼
     */
    public int customerID;

    /**
     * 車輛代碼
     */
    public int carID;

    /**
     * 身分識別裝置(0:不存在, 1:存在)
     */
    public int idStorage;

    /**
     * 司機代碼
     */
    public long driverID;

    /**
     * 序號
     */
    public int sequence;

    /**
     * 保留
     */
    public int reserved;

    /**
     * payload 長度
     */
    public int payLoadLength;

    public Header() {

    }

    public byte[] getBytes() {
        byte[] bytes = new byte[20];
        int index = 0;

        this.protocolID = String.format("%4s", this.protocolID);
        for (int i = 0; i < 4; i++) {
            try {
                bytes[index] = this.protocolID.getBytes()[i];
            } catch (Exception e) {
            }
            index++;
        }

        bytes[index++] = (byte) this.protocolVer;

        bytes[index++] = (byte) this.messageID;

        System.arraycopy(BitConverter.toUShortByteArray(this.customerID), 0, bytes, index, BitConverter.UshortSIZE);
        index += BitConverter.UshortSIZE;

        System.arraycopy(BitConverter.toUShortByteArray(this.carID), 0, bytes, index, BitConverter.UshortSIZE);
        index += BitConverter.UshortSIZE;

        bytes[index++] = (byte) this.idStorage;

        System.arraycopy(BitConverter.toUIntegerByteArray(this.driverID), 0, bytes, index, BitConverter.UintSIZE);
        index += BitConverter.UintSIZE;

        System.arraycopy(BitConverter.toUShortByteArray(this.sequence), 0, bytes, index, BitConverter.UshortSIZE);
        index += BitConverter.UshortSIZE;

        bytes[index++] = (byte) this.reserved;

        System.arraycopy(BitConverter.toUShortByteArray(this.payLoadLength), 0, bytes, index, BitConverter.UshortSIZE);
        index += BitConverter.UshortSIZE;

        return bytes;
    }

    public static Header Parse(int[] headerDateBytes) {
        int index = 0;
        Header header = new Header();

        // byte 0,1,2,3
        try {
            header.protocolID = BitConverter.toString(headerDateBytes, 0, 4, "Big-5");
        } catch (Exception e) {
            e.printStackTrace();
        }
        index += 4;

        // byte 4
        header.protocolVer = headerDateBytes[index];
        index += 1;

        // byte 5
        header.messageID = headerDateBytes[index];
        index += 1;

        // byte 6,7
        header.customerID = BitConverter.toUShort(headerDateBytes, index);
        index += 2;

        // byte 8,9
        header.carID = BitConverter.toUShort(headerDateBytes, index);
        index += 2;

        // byte 10
        header.idStorage = headerDateBytes[index];
        index += 1;

        // byte 11,12,13,14
        header.driverID = BitConverter.toUInteger(headerDateBytes, index);
        index += 4;

        // byte 15,16
        header.sequence = BitConverter.toUShort(headerDateBytes, index);
        index += 2;

        // byte 17
        header.reserved = headerDateBytes[index];
        index += 1;

        // byte 18,19
        header.payLoadLength = BitConverter.toUShort(headerDateBytes, index);
        index += 2;

        return header;
    }

    public Header clone() {
        Header varCopy = new Header();

        varCopy.carID = this.carID;
        varCopy.customerID = this.customerID;
        varCopy.driverID = this.driverID;
        varCopy.idStorage = this.idStorage;
        varCopy.messageID = this.messageID;
        varCopy.payLoadLength = this.payLoadLength;
        varCopy.protocolID = this.protocolID;
        varCopy.protocolVer = this.protocolVer;
        varCopy.reserved = this.reserved;
        varCopy.sequence = this.sequence;

        return varCopy;
    }

    @Override
    public String toString() {
        return "Header [protocolID=" + protocolID + ", protocolVer=" + protocolVer + ", messageID=" + Integer.toHexString(messageID) + ", customerID=" + customerID + ", carID=" + carID
                + ", idStorage=" + idStorage + ", driverID=" + driverID + ", sequence=" + sequence + ", reserved=" + reserved + ", payLoadLength=" + payLoadLength + "]";
    }
}
