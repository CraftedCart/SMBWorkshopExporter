package craftedcart.smbworkshopexporter.util;

/**
 * @author CraftedCart
 *         Created on 21/09/2016 (DD/MM/YYYY)
 */
public class Vec3f {
    public float x;
    public float y;
    public float z;

    public Vec3f() {}

    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3f subtract(Vec3f vec3f) {
        return new Vec3f(x - vec3f.x, y - vec3f.y, z - vec3f.z);
    }
}
