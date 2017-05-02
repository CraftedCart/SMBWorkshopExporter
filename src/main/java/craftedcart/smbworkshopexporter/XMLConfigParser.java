package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.placeables.*;
import craftedcart.smbworkshopexporter.util.LogHelper;
import craftedcart.smbworkshopexporter.util.Vec3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author CraftedCart
 *         Created on 18/03/2017 (DD/MM/YYYY)
 */
public class XMLConfigParser {

    public static final int[] PARSER_VERSION = new int[]{1, 0, 0};

    public static void parseConfig(ConfigData configData, File configFile) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(configFile);

        Element root = doc.getDocumentElement();
        String versionStr = root.getAttribute("version");
        int[] version = parseVersion(versionStr);

        LogHelper.info(XMLConfigParser.class, String.format("XML config parser version: %d.%d.%d", PARSER_VERSION[0], PARSER_VERSION[1], PARSER_VERSION[2]));
        LogHelper.info(XMLConfigParser.class, String.format("Config version: %d.%d.%d", version[0], version[1], version[2]));

        //Model imports
        NodeList modelImportList = root.getElementsByTagName("modelImport");
        for (int i = 0; i < modelImportList.getLength(); i++) {
            Element modelImportElement = (Element) modelImportList.item(i);
            ExternalModel model = parseModelImport(modelImportElement, configFile.getParentFile());
            if (model != null) {
                configData.models.add(model);
            }
        }

        NodeList bgModelList = root.getElementsByTagName("backgroundModel");
        for (int i = 0; i < bgModelList.getLength(); i++) {
            Element bgModelElement = (Element) bgModelList.item(i);
            String name = getDefNameOrUniqueName(bgModelElement, "SMBWorkshopExporter-Error-BackgroundNotFound", null);
            configData.backgroundList.add(name);
            //TODO: Background position, rotation and scale
        }

        NodeList startList = root.getElementsByTagName("start");
        if (startList.getLength() > 0) {
            Element startElement = (Element) startList.item(0);
            String name = getDefNameOrUniqueName(startElement, "Start position", getAllNames(configData.getItemGroupMap(), configData.startList.keySet()));
            configData.startList.put(name, getStart(startElement));
        }

        NodeList falloutList = root.getElementsByTagName("falloutPlane");
        configData.falloutPlane = getFloatAttr((Element) falloutList.item(0), "y");

        NodeList itemGroupList = root.getElementsByTagName("itemGroup");
        for (int i = 0; i < itemGroupList.getLength(); i++) {
            Element itemGroupElement = (Element) itemGroupList.item(i);

            String itemGroupName = getDefNameOrUniqueName(itemGroupElement, "Item Group", getAllNames(configData.getItemGroupMap(), configData.startList.keySet()));
            ItemGroup itemGroup = parseItemGroup(itemGroupElement, itemGroupName, configData);
            configData.addItemGroup(itemGroupName, itemGroup);
        }
    }

    /**
     * @param modelImportElement The element
     * @param relativeDir Used for relative file paths - Should be the directory where the config file resides
     * @return An ExternalModel, or null if the type is invalid
     */
    @Nullable
    private static ExternalModel parseModelImport(Element modelImportElement, File relativeDir) {
        File file;
        ModelType type;

        String typeStr = modelImportElement.getAttribute("type");
        try {
            type = ModelType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            LogHelper.error(XMLConfigParser.class, "Invalid model import type for element " + modelImportElement.toString());
            return null; //Failed - return null
        }

        if (modelImportElement.getTextContent().length() == 0) { //Filepath must be specified
            LogHelper.error(XMLConfigParser.class, "No filepath specified for model import element " + modelImportElement.toString());
            return null; //Failed - return null
        }

        if (modelImportElement.getTextContent().startsWith("//")) { //Starting with // - Relative filepath
            file = new File(relativeDir, modelImportElement.getTextContent().substring(2));
        } else { //Not starting with // - Assume URI
            try {
                file = new File(new URI(modelImportElement.getTextContent()));
            } catch (URISyntaxException e) {
                LogHelper.error(XMLConfigParser.class, "Invalid URI for model import element " + modelImportElement.toString());
                LogHelper.error(XMLConfigParser.class, LogHelper.stackTraceToString(e));
                return null; //Failed - return null
            }
        }

        return new ExternalModel(file, type);
    }

    @NotNull
    private static ItemGroup parseItemGroup(Element itemGroupElement, String itemGroupName, ConfigData configData) {
        Map<String, ItemGroup> allItemGroups = new HashMap<>();

        ItemGroup itemGroup = new ItemGroup();

        allItemGroups.putAll(configData.getItemGroupMap());
        allItemGroups.put(itemGroupName, itemGroup);

        itemGroup.rotationCenter = getPosition(getSingleElement(itemGroupElement, "rotationCenter"));
        itemGroup.initialRotation = getPosition(getSingleElement(itemGroupElement, "initialRotation"));

        //TODO: Collision

        //Level Models
        NodeList levelModelList = itemGroupElement.getElementsByTagName("levelModel");
        for (int i = 0; i < levelModelList.getLength(); i++) {
            Element levelModelElement = (Element) levelModelList.item(i);
            itemGroup.levelModels.add(levelModelElement.getTextContent());
        }

        //Goals
        NodeList goalList = itemGroupElement.getElementsByTagName("goal");
        for (int i = 0; i < goalList.getLength(); i++) {
            Element goalElement = (Element) goalList.item(i);

            String name = getDefNameOrUniqueName(goalElement, "Goal", getAllNames(allItemGroups, configData.startList.keySet()));

            itemGroup.goalList.put(name, getGoal(goalElement));
        }

        //Bumpers
        NodeList bumperList = itemGroupElement.getElementsByTagName("bumper");
        for (int i = 0; i < bumperList.getLength(); i++) {
            Element bumperElement = (Element) bumperList.item(i);

            String name = getDefNameOrUniqueName(bumperElement, "Bumper", getAllNames(allItemGroups, configData.startList.keySet()));

            itemGroup.bumperList.put(name, getBumper(bumperElement));
        }

        //Jamabars
        NodeList jamabarList = itemGroupElement.getElementsByTagName("jamabar");
        for (int i = 0; i < jamabarList.getLength(); i++) {
            Element jamabarElement = (Element) jamabarList.item(i);

            String name = getDefNameOrUniqueName(jamabarElement, "Jamabar", getAllNames(allItemGroups, configData.startList.keySet()));

            itemGroup.jamabarList.put(name, getJamabar(jamabarElement));
        }

        //Bananas
        NodeList bananaList = itemGroupElement.getElementsByTagName("banana");
        for (int i = 0; i < bananaList.getLength(); i++) {
            Element bananaElement = (Element) bananaList.item(i);

            String name = getDefNameOrUniqueName(bananaElement, "Banana", getAllNames(allItemGroups, configData.startList.keySet()));

            itemGroup.bananaList.put(name, getBanana(bananaElement));
        }

        //Wormholes
        NodeList wormholeList = itemGroupElement.getElementsByTagName("wormhole");
        for (int i = 0; i < wormholeList.getLength(); i++) {
            Element wormholeElement = (Element) wormholeList.item(i);

            String name = getDefNameOrUniqueName(wormholeElement, "Wormhole", getAllNames(allItemGroups, configData.startList.keySet()));

            itemGroup.wormholeList.put(name, getWormhole(wormholeElement));
        }

        return itemGroup;
    }

    private static Start getStart(Element startElement) {
        Start start = new Start();

        start.pos = getPosition(startElement);
        start.rot = getRotation(startElement);

        return start;
    }

    private static Goal getGoal(Element goalElement) {
        Goal goal = new Goal();

        goal.pos = getPosition(goalElement);
        goal.rot = getRotation(goalElement);
        goal.type = getEnumGoalType(goalElement);

        return goal;
    }

    private static Bumper getBumper(Element bumperElement) {
        Bumper bumper = new Bumper();

        bumper.pos = getPosition(bumperElement);
        bumper.rot = getRotation(bumperElement);
        bumper.scl = getScale(bumperElement);

        return bumper;
    }

    private static Jamabar getJamabar(Element jamabarElement) {
        Jamabar jamabar = new Jamabar();

        jamabar.pos = getPosition(jamabarElement);
        jamabar.rot = getRotation(jamabarElement);
        jamabar.scl = getScale(jamabarElement);

        return jamabar;
    }

    private static Banana getBanana(Element bananaElement) {
        Banana banana = new Banana();

        banana.pos = getPosition(bananaElement);
        banana.type = getEnumBananaType(bananaElement);

        return banana;
    }

    private static Wormhole getWormhole(Element wormholeElement) {
        Wormhole wormhole = new Wormhole();

        wormhole.pos = getPosition(wormholeElement);
        wormhole.rot = getRotation(wormholeElement);

        Node destinationNode = wormholeElement.getElementsByTagName("destinationName").item(0);

        if (destinationNode != null) {
            wormhole.destinationName = destinationNode.getTextContent();
        } else {
            LogHelper.warn(XMLConfigParser.class, "No destination wormhole specified for wormhole " + wormholeElement.toString());
        }

        return wormhole;
    }

    private static Set<String> getAllNames(Map<String, ItemGroup> itemGroupMap, Collection<String> startListNames) {
        Set<String> names = new HashSet<>();

        names.addAll(startListNames);
        names.addAll(itemGroupMap.keySet());

        for (ItemGroup itemGroup : itemGroupMap.values()) {
            names.addAll(itemGroup.goalList.keySet());
            names.addAll(itemGroup.bumperList.keySet());
            names.addAll(itemGroup.jamabarList.keySet());
            names.addAll(itemGroup.bananaList.keySet());
            names.addAll(itemGroup.falloutVolumeList.keySet());
            names.addAll(itemGroup.wormholeList.keySet());
        }

        return names;
    }

    /**
     * Returns a defined name within the name tag in the specified element, or generates a
     * unique name if the specified name is already taken, or if no name is specified
     *
     * @param element The element to search for a name element within
     * @param prefix The prefix to use when generating a unique name
     * @param takenNames Names already taken to check against to avoid conflicts
     * @return The defined name, or a unique name if a defined name doesn't exist / is already taken
     */
    @NotNull
    private static String getDefNameOrUniqueName(Element element, String prefix, Collection<String> takenNames) {
        if (takenNames == null) { //Replace null with empty set
            takenNames = new HashSet<>();
        }

        NodeList children = element.getChildNodes();
        String name = null;

        for (int i = 0; i < children.getLength(); i++) {
            if (Objects.equals(children.item(i).getNodeName(), "name")) {
                name = children.item(i).getTextContent();
                break;
            }
        }

        if (name == null || takenNames.contains(name)) {
            //Name not specified or name already taken
            return getUniqueName(prefix, takenNames);
        } else {
            return name;
        }
    }

    @NotNull
    private static String getUniqueName(String prefix, Collection<String> takenNames) {
        int i = 1;
        while (i < Integer.MAX_VALUE) {
            String name = String.format("%s %d", prefix, i);
            if (!takenNames.contains(name)) return name; //Unique name found

            i++; //No unique name found - Increment i and try again
        }

        //This shouldn't happen, unless you're crazy enough to try to create Integer.MAX_VALUE items...
        throw new RuntimeException("No unique names remaining!");
    }

    @Nullable
    private static Element getSingleElement(Element parent, String name) {
        NodeList list = parent.getElementsByTagName(name);
        return (Element) list.item(0);
    }

    /**
     * @param element The element
     * @param attrName The attribute name
     * @return The parsed string value of the attribute, or an empty string if it failed to parse / doesn't exist
     */
    private static String getStringAttr(Element element, String attrName) {
        if (element == null) return null;

        return element.getAttribute(attrName);
    }

    /**
     * @param element The element
     * @param attrName The attribute name
     * @return The parsed integer value of the attribute, or 0 if it failed to parse / doesn't exist
     */
    private static int getIntAttr(Element element, String attrName) {
        if (element == null) return 0;

        try {
            return Integer.parseInt(element.getAttribute(attrName));
        } catch (NumberFormatException e) {
            LogHelper.error(XMLConfigParser.class,  String.format("Invalid integer at %s", element.toString()));
        }

        return 0; //Failed - Return 0
    }

    /**
     * @param element The element
     * @param attrName The attribute name
     * @return The parsed float value of the attribute, or 0 if it failed to parse / doesn't exist
     */
    private static float getFloatAttr(Element element, String attrName) {
        if (element == null) return 0;

        try {
            return Float.parseFloat(element.getAttribute(attrName));
        } catch (NumberFormatException e) {
            LogHelper.error(XMLConfigParser.class,  String.format("Invalid float at %s", element.toString()));
        }

        return 0; //Failed - Return 0
    }

    /**
     * @param element The element
     * @return The position defined within the tag <code>&lt;position x="0" y="0" z="0" /&gt</code> within the element
     */
    @NotNull
    private static Vec3f getPosition(Element element) {
        return getVec3fChild(element, "position");
    }

    @NotNull
    private static Vec3f getRotation(Element element) {
        return getVec3fChild(element, "rotation");
    }

    @NotNull
    private static Vec3f getScale(Element element) {
        return getVec3fChild(element, "scale");
    }

    @NotNull
    private static Goal.EnumGoalType getEnumGoalType(Element element) {
        Node typeNode = element.getElementsByTagName("type").item(0);

        if (typeNode != null) {
            try {
                return Goal.EnumGoalType.valueOf(typeNode.getTextContent());
            } catch (IllegalArgumentException e) {
                LogHelper.error(XMLConfigParser.class,  "Invalid goal type specified - Must be BLUE, GREEN or RED - Defaulting to blue");
                return Goal.EnumGoalType.BLUE;
            }
        } else {
            //No type specified - Default to blue
            return Goal.EnumGoalType.BLUE;
        }
    }

    @NotNull
    private static Banana.EnumBananaType getEnumBananaType(Element element) {
        Node typeNode = element.getElementsByTagName("type").item(0);

        if (typeNode != null) {
            try {
                return Banana.EnumBananaType.valueOf(typeNode.getTextContent());
            } catch (IllegalArgumentException e) {
                LogHelper.error(XMLConfigParser.class,  "Invalid banana type specified - Must be SINGLE or BUNCH - Defaulting to single");
                return Banana.EnumBananaType.SINGLE;
            }
        } else {
            //No type specified - Default to blue
            return Banana.EnumBananaType.SINGLE;
        }
    }

    @NotNull
    private static Vec3f getVec3fChild(Element element, String tagName) {
        NodeList posElementList = element.getElementsByTagName(tagName);

        if (posElementList.getLength() == 0) {
            //No position specified - Return 0, 0, 0
            return new Vec3f(0, 0, 0);
        }

        Element posElement = (Element) posElementList.item(0);
        return parseVec3f(posElement);
    }

    @NotNull
    private static Vec3f parseVec3f(Element element) {
        Vec3f vec = new Vec3f();

        String xStr = element.getAttribute("x");
        String yStr = element.getAttribute("y");
        String zStr = element.getAttribute("z");

        try {
            vec.x = Float.parseFloat(xStr);
            vec.y = Float.parseFloat(yStr);
            vec.z = Float.parseFloat(zStr);
        } catch (NumberFormatException e) {
            LogHelper.error(XMLConfigParser.class,  String.format("Invalid vector at %s", element.toString()));
        }

        return vec;
    }

    @NotNull
    private static int[] parseVersion(String versionStr) {
        int[] version = new int[3];
        if (versionStr.isEmpty()) {
            LogHelper.warn(XMLConfigParser.class,  "No version specified on XML config file - Certain parts may not work");
        } else {
            String[] versionStrSplit = versionStr.split("\\.");
            if (versionStrSplit.length == 3) {
                try {
                    version[0] = Integer.parseInt(versionStrSplit[0]);
                    version[1] = Integer.parseInt(versionStrSplit[1]);
                    version[2] = Integer.parseInt(versionStrSplit[2]);
                } catch (NumberFormatException e) {
                    LogHelper.warn(XMLConfigParser.class,  "Invalid version specified on XML config file - Version sections are not integers");
                }
            } else {
                LogHelper.warn(XMLConfigParser.class,  "Invalid version specified on XML config file - Version is not split into 3 elements");
            }
        }

        return version;
    }

}
