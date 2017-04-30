package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.placeables.Start;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author CraftedCart
 *         Created on 20/09/2016 (DD/MM/YYYY)
 */
public class ConfigData {

    @NotNull public Set<ExternalModel> models = new HashSet<>();

    @NotNull public Map<String, Start> startList = new HashMap<>();

    @NotNull private Map<String, ItemGroup> itemGroupMap = new HashMap<>();

    //Keep track of animated objects so that they aren't added to the static group at the end
    @NotNull List<String> animatedObjects = new ArrayList<>();

    @NotNull public List<String> backgroundList = new ArrayList<>();

    public float falloutPlane = 0.0f;

    public float maxTime = 60.0f;
    public float leadInTime = 6.0f;

    public ItemGroup getFirstItemGroup() {
        if (itemGroupMap.containsKey("Static")) {
            //Use static item group if it exists
            return itemGroupMap.get("Static");
        } else {
            //If static doesn't exist, find the first non STAGE_RESERVED / BACKGROUND_RESERVED item group
            for (Map.Entry<String, ItemGroup> entry : itemGroupMap.entrySet()) {
                if (!entry.getKey().equals("STAGE_RESERVED") || !entry.getKey().equals("BACKGROUND_RESERVED")) return entry.getValue();
            }

            //If no item groups exist, create one
            String igName = addItemGroup();
            return itemGroupMap.get(igName);
        }
    }

    /**
     * Generates a new item group
     * @return The name of the new item group
     */
    public String addItemGroup() {
        String name = "New Item Group 1";

        int i = 1;
        Set<String> allNames = itemGroupMap.keySet();
        while (allNames.contains(name)) {
            i++;
            name = "New Item Group " + String.valueOf(i);
        }

        itemGroupMap.put(name, new ItemGroup());

        return name;
    }

    public void addItemGroup(String name, ItemGroup ig) {
        itemGroupMap.put(name, ig);
    }

    @NotNull
    public Map<String, ItemGroup> getItemGroupMap() {
        return itemGroupMap;
    }

    public ItemGroup getItemGroup(String name) {
        return itemGroupMap.get(name);
    }

    public List<String> getAnimatedObjects() {
        return animatedObjects;
    }

}
