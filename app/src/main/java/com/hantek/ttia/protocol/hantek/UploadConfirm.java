package com.hantek.ttia.protocol.hantek;

import com.hantek.ttia.module.BitConverter;

/**
 * 上傳Log確認
 */
public class UploadConfirm {
    public int year;
    public int month;
    public int day;

    /**
     * 0:回報檔案, 1:回報執行, 2:回報結果.
     */
    public byte type;

    /**
     * 0:檔案不存在or未執行or異常, 1:存在or執行or成功.
     */
    public byte result;

    /**
     * 檔案名稱
     */
    public String fileName;

    public UploadConfirm() {
        this.fileName = "";
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[8 + this.fileName.length()];
        int index = 0;

        System.arraycopy(BitConverter.toUShortByteArray(this.year), 0, bytes, index, BitConverter.UshortSIZE);
        index += BitConverter.UshortSIZE;

        System.arraycopy(BitConverter.toUShortByteArray(this.month), 0, bytes, index, BitConverter.UshortSIZE);
        index += BitConverter.UshortSIZE;

        System.arraycopy(BitConverter.toUShortByteArray(this.day), 0, bytes, index, BitConverter.UshortSIZE);
        index += BitConverter.UshortSIZE;

        bytes[index++] = this.type;
        bytes[index++] = this.result;

        for (int i = 0; i < this.fileName.length(); i++) {
            try {
                bytes[index] = this.fileName.getBytes()[i];
            } catch (Exception e) {
            }
            index++;
        }

        return bytes;
    }
}
