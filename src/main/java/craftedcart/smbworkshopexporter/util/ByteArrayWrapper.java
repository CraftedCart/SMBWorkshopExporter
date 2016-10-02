package craftedcart.smbworkshopexporter.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author CraftedCart
 *         Created on 01/10/2016 (DD/MM/YYYY)
 */
public class ByteArrayWrapper {

    public Byte[] data;

    public ByteArrayWrapper(Byte[] data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof ByteArrayWrapper)) {
            return false;
        }

        Byte[] compareData = ((ByteArrayWrapper) obj).data;

        if (data.length != compareData.length) {
            return false;
        }

        int i = 0;
        for (Byte b : data) {

            if (!Objects.equals(b, compareData[i])) {
                return false;
            }

            i++;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

}
