package craftedcart.smbworkshopexporter.util;

/**
 * @author CraftedCart
 *         Created on 25/02/2017 (DD/MM/YYYY)
 */
public class Mat3f {

    private float[][] matrix = new float[3][3];

    public Mat3f() {}

    public Mat3f(float x0, float y0, float z0,
                 float x1, float y1, float z1,
                 float x2, float y2, float z2) {
        matrix[0][0] = x0;
        matrix[0][1] = y0;
        matrix[0][2] = z0;
        matrix[1][0] = x1;
        matrix[1][1] = y1;
        matrix[1][2] = z1;
        matrix[2][0] = x2;
        matrix[2][1] = y2;
        matrix[2][2] = z2;
    }

    public float[][] getMatrix() {
        return matrix;
    }

}
