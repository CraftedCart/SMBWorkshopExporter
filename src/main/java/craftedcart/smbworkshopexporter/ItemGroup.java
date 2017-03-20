package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.placeables.*;
import craftedcart.smbworkshopexporter.util.Vec3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CraftedCart
 *         Created on 21/02/2017 (DD/MM/YYYY)
 */
public class ItemGroup {

    @NotNull public Vec3f rotationCenter = new Vec3f();
    @NotNull public Vec3f initialRotation = new Vec3f();

    @NotNull public List<String> levelModels = new ArrayList<>();

    @NotNull public Map<String, Goal> goalList = new HashMap<>();
    @NotNull public Map<String, Bumper> bumperList = new HashMap<>();
    @NotNull public Map<String, Jamabar> jamabarList = new HashMap<>();
    @NotNull public Map<String, Banana> bananaList = new HashMap<>();
    @NotNull public Map<String, Wormhole> wormholeList = new HashMap<>();

    @NotNull public Map<String, FalloutVolume> falloutVolumeList = new HashMap<>();

    @Nullable public ConfigAnimData animData;

    public void addObject(String objectName) {
        levelModels.add(objectName);
    }

    public int getLevelModelTriangleNum(ModelData modelData) {
        int levelModelTriangleNum = 0;

        for (String objectName : levelModels) {
            levelModelTriangleNum += modelData.getObjFromModelName(objectName).tris.size();
        }

        return levelModelTriangleNum;
    }

}
