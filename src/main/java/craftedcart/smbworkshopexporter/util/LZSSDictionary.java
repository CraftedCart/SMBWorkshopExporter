package craftedcart.smbworkshopexporter.util;

import java.util.*;

/**
 * @author CraftedCart
 *         Created on 26/09/2016 (DD/MM/YYYY)
 */
public class LZSSDictionary {

    public static final int WSIZE = 0x1000; //Window size
    public static final int WMASK = 0x0FFF; //Window offset bit mask

    public static final int MAX_REF_LEN = 18; //Maximum reference length
    public static final int MIN_REF_LEN = 3; //Minimum reference length

    private List<Map<ByteArrayWrapper, Integer>> d;
    private List<Map<Integer, ByteArrayWrapper>> r;

    public int ptr = 0;

    public LZSSDictionary() {
        //For each reference length there is one dictionary mapping substrings to dictionary offsets
        d = new ArrayList<>(MAX_REF_LEN + 1);
        for (int i = 0; i < MAX_REF_LEN + 1; i++) {
            d.add(new HashMap<>());
        }

        //For each reference length there is also a reverse dictionary mapping dictionary offsets to substrings
        //This makes removing dictionary entries much more efficient
        r = new ArrayList<>(MAX_REF_LEN + 1);
        for (int i = 0; i < MAX_REF_LEN + 1; i++) {
            r.add(new HashMap<>());
        }
    }

    /**
     * Add all initial parts of a byte array to the dictionary
     * @param bytes Byte array
     */
    public void add(Byte[] bytes) {
        int maxLength = MAX_REF_LEN;
        if (maxLength > bytes.length) {
            maxLength = bytes.length;
        }

        int offset = ptr;

        //Generate all substrings
        for (int length = MIN_REF_LEN; length < maxLength + 1; length++) {
            ByteArrayWrapper subStr = new ByteArrayWrapper(Arrays.copyOfRange(bytes, 0, length));

            //Remove obsolete mapping, if present
            try {
                Integer prevOffset = d.get(length).get(subStr);
                if (prevOffset != null) {
                    r.get(length).remove(prevOffset);
                }
            } catch (NullPointerException e) {
            }

            ByteArrayWrapper prevSubStr = r.get(length).get(offset);
            if (prevSubStr != null) {
                d.get(length).remove(prevSubStr);
            }

            d.get(length).put(subStr, offset);
            r.get(length).put(offset, subStr);

        }

        ptr = (ptr + 1) & WMASK;

    }

    /**
     * Find any of the initial substrings of a string in the dictionary, looking for long matches first
     * @param bytes Byte array
     * @return Offset and length if found
     * @throws NullPointerException if not found
     */
    public OffsetLength find(Byte[] bytes) throws NullPointerException {
        int maxLength = MAX_REF_LEN;
        if (maxLength > bytes.length) {
            maxLength = bytes.length;
        }

        for (int length = maxLength; length > MIN_REF_LEN - 1; length--) {
            ByteArrayWrapper subStr = new ByteArrayWrapper(Arrays.copyOfRange(bytes, 0, length));

            try {
                int offset = d.get(length).get(subStr);
                if (offset != ptr) { //The FFZ LZSS decompressor can't handle this case
                    return new OffsetLength(offset, length);
                }
            } catch (NullPointerException e) {
            }
        }

        throw new NullPointerException();
    }

    public class OffsetLength {
        public int offset;
        public int length;

        public OffsetLength(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }
    }

}
