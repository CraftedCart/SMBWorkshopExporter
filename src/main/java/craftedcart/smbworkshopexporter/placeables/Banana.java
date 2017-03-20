package craftedcart.smbworkshopexporter.placeables;

import craftedcart.smbworkshopexporter.util.Vec3f;
import org.jetbrains.annotations.NotNull;

/**
 * @author CraftedCart
 *         Created on 18/03/2017 (DD/MM/YYYY)
 */
public class Banana {
    @NotNull public Vec3f pos = new Vec3f();
    @NotNull public EnumBananaType type = EnumBananaType.SINGLE;

    public enum EnumBananaType {
        SINGLE, BUNCH
    }
}
