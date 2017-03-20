package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.placeables.Start;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * @author CraftedCart
 *         Created on 20/09/2016 (DD/MM/YYYY)
 */
public class ConfigData {

    @NotNull public Set<ExternalModel> models = new HashSet<>();

    @NotNull public Map<String, Start> startList = new HashMap<>();

    /**
     * 0: Static group<br>
     * All other groups exist for the purpose of animation
     */
    @NotNull public List<ItemGroup> itemGroups = new ArrayList<>();
    @NotNull private Map<String, Integer> animConfigIndexToItemGroupsIndexDict = new HashMap<>();

    //Keep track of animated objects so that they aren't added to the static group at the end
    @NotNull List<String> animatedObjects = new ArrayList<>();

    @NotNull public List<String> backgroundList = new ArrayList<>();

    public float falloutPlane = 0.0f;

    public float maxTime = 60.0f;
    public float leadInTime = 6.0f;

    @Deprecated
    public ItemGroup getStaticItemGroup() {
        return itemGroups.get(0);
    }

    @NotNull
    public ItemGroup getConfigIndexedItemGroup(String configIndex) {
        Integer itemGroupsIndex = animConfigIndexToItemGroupsIndexDict.get(configIndex);

        if (itemGroupsIndex != null) {
            return itemGroups.get(itemGroupsIndex);
        } else {
            animConfigIndexToItemGroupsIndexDict.put(configIndex, itemGroups.size());

            ItemGroup ig = new ItemGroup();
            itemGroups.add(ig);

            return ig;
        }
    }

    public List<String> getAnimatedObjects() {
        return animatedObjects;
    }

}
