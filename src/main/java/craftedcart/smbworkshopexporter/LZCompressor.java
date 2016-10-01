package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.util.LZSSDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author CraftedCart
 *         Created on 26/09/2016 (DD/MM/YYYY)
 */
public class LZCompressor {

    /**
     * Compresses an 8 bit string to LZSS format
     * @param data String to compress
     */
    public static List<Byte> compress(Byte[] data) {

        LZSSDictionary dict = new LZSSDictionary();

        //Prime the dictionary
        dict.ptr = LZSSDictionary.WSIZE - 2 * LZSSDictionary.MAX_REF_LEN;
        for (int i = 0; i < LZSSDictionary.MAX_REF_LEN; i++) {
            List<Byte> byte0List = Collections.nCopies(LZSSDictionary.MAX_REF_LEN - i, (byte) 0);
            Byte[] dataByteList = Arrays.copyOfRange(data, 0, i);
            Byte[] byteList = new Byte[byte0List.size() + dataByteList.length];

            int j = 0;
            for (Byte b : byte0List) {
                byteList[j] = b;
                j++;
            }

            for (Byte b : dataByteList) {
                byteList[j] = b;
                j++;
            }

            dict.add(byteList);
        }

        //Output data
        List<Byte> output = new ArrayList<>();

        int i = 0;
        int dataSize = data.length;

        while (i < dataSize) {

            //Accumulated output chunk
            List<Byte> accum = new ArrayList<>();

            //Process 8 literals or references at a time
            int flags = 0;
            for (int bit = 0; bit < 8; bit++) {

                if (i >= dataSize) {
                    break;
                }

                //Next substring in dictionary?
                try {

                    Byte[] subStr = Arrays.copyOfRange(data, i, Math.min(i + LZSSDictionary.MAX_REF_LEN, data.length));
                    LZSSDictionary.OffsetLength ol = dict.find(subStr);

                    //Yes, append dictionary reference
//                    accum += new String(new char[] {(char) (ol.offset & 0xFF), (char) (((ol.offset >> 4) & 0xF0) | (ol.length - LZSSDictionary.MIN_REF_LEN))});
                    accum.add((byte) (ol.offset & 0xFF));
                    accum.add((byte) (((ol.offset >> 4) & 0xF0) | (ol.length - LZSSDictionary.MIN_REF_LEN)));

                    //Update dictionary
                    for (int j = 0; j < ol.length; j++) {
                        dict.add(Arrays.copyOfRange(data, i + j, Math.min(i + j + LZSSDictionary.MAX_REF_LEN, data.length)));
                    }

                    i += ol.length;

                } catch (NullPointerException e) {

                    //Append literal value
                    Byte v = data[i];
                    accum.add(v);

                    flags |= (1 << bit);

                    //Update dictionary
                    dict.add(Arrays.copyOfRange(data, i, Math.min(i + LZSSDictionary.MAX_REF_LEN, data.length)));

                    i += 1;

                }

            }

            //Chunk complete, add to output0
            output.add((byte) flags);
            output.addAll(accum);

        }

        //This is used in FF7, not SMB
//        //Add compressed size to the beginning (int, little endian - excluding these new 4 bytes)
//        int compressedSize = output.size();
//        output.add(0, (byte) (compressedSize & 0xFF));
//        output.add(1, (byte) ((compressedSize >> 8) & 0xFF));
//        output.add(2, (byte) ((compressedSize >> 16) & 0xFF));
//        output.add(3, (byte) ((compressedSize >> 24) & 0xFF));

        //<editor-fold desc="Super Monkey Ball specific stuff">
        //Add uncompressed size to the beginning (int, little endian)
        int uncompressedSize = data.length;
        output.add(0, (byte) (uncompressedSize & 0xFF));
        output.add(1, (byte) ((uncompressedSize >> 8) & 0xFF));
        output.add(2, (byte) ((uncompressedSize >> 16) & 0xFF));
        output.add(3, (byte) ((uncompressedSize >> 24) & 0xFF));

        //Add compressed size to the beginning (int, little endian - including these new 4 bytes)
        int compressedSize = output.size() + 4;
        output.add(0, (byte) (compressedSize & 0xFF));
        output.add(1, (byte) ((compressedSize >> 8) & 0xFF));
        output.add(2, (byte) ((compressedSize >> 16) & 0xFF));
        output.add(3, (byte) ((compressedSize >> 24) & 0xFF));
        //</editor-fold>

        return output;

    }

}
