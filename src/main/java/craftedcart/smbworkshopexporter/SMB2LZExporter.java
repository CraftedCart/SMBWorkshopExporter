package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.placeables.*;
import craftedcart.smbworkshopexporter.util.LogHelper;
import craftedcart.smbworkshopexporter.util.Vec3f;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author CraftedCart
 *         Created on 01/10/2016 (DD/MM/YYYY)
 */
public class SMB2LZExporter extends AbstractLzExporter {

    private static final int FILE_HEADER_LENGTH = 2204;
    private static final int START_DATA_LENGTH = 20;
    private static final int FALLOUT_DATA_LENGTH = 4;
    private static final int COLLISION_HEADER_LENGTH = 1180;
    private static final int COLLISION_TRIANGLE_LENGTH = 64;
    private static final int COLLISION_TRIANGLE_INDEX_LENGTH = 2;
    private static final int COLLISION_TRIANGLE_LIST_POINTER_LENGTH = 4;
    private static final int LEVEL_MODEL_OFFSET_TYPE_A_LENGTH = 12;
    private static final int LEVEL_MODEL_OFFSET_TYPE_B_LENGTH = 4;
    private static final int LEVEL_MODEL_TYPE_A_LENGTH = 16;
    private static final int GOAL_LENGTH = 20;
    private static final int BUMPER_LENGTH = 32;
    private static final int JAMABAR_LENGTH = 32;
    private static final int BANANA_LENGTH = 16;
    private static final int WORMHOLE_LENGTH = 28;

    private static final float COLLISION_X_START = -256;
    private static final float COLLISION_Z_START = -256;
    private static final float COLLISION_X_STEP = 32;
    private static final float COLLISION_Z_STEP = 32;
    private static final int COLLISION_X_STEP_NUM = 16;
    private static final int COLLISION_Z_STEP_NUM = 16;

    private List<Byte> outBytes = new ArrayList<>();

    private ModelData modelData;
    private ConfigData configData;

    /**
     * Number of collision headers
     */
    private int collisionHeaderNum;

    /**
     * Offset to collision header<br>
     * Offset: FILE_HEADER_LENGTH<br>
     * Offset: Just after file header<br>
     * Length: 1180 * collisionHeaderNum
     */
    private int collisionHeaderOffset;

    /**
     * Length of all collision headers combined
     */
    private int collisionHeadersTotalLength;

    /**
     * Offset at the end of the collision header
     */
    private int collisionHeadersEndOffset;

    /**
     * Each ItemGroup's collision triangles list offset
     */
    private Map<ItemGroup, Integer> collisionTrianglesListOffsets = new HashMap<>();

    /**
     * Each ItemGroup's collision triangle list list offset<br>
     * The list that contains triangle indices and terminates in <code>0xFFFF</code><br>
     */
    private Map<ItemGroup, Integer> collisionTriangleListListOffsets = new HashMap<>();

    /**
     * Each ItemGroup's collision triangles list pointers offsets
     */
    private Map<ItemGroup, Integer> collisionTrianglesListPointersOffsets = new HashMap<>();

    /**
     * Each ItemGroup's level model offset - Type A list offsets
     */
    private Map<ItemGroup, Integer> levelModelOffsetListTypeAOffsets = new HashMap<>();

    /**
     * Each ItemGroup's level model offset - Type B list offsets
     */
    private Map<ItemGroup, Integer> levelModelOffsetListTypeBOffsets = new HashMap<>();

    /**
     * Each ItemGroup's model name list offsets
     */
    private Map<ItemGroup, Integer> levelModelNameListOffsets = new HashMap<>();

    /**
     * Each ItemGroup's level model - Type A list offsets
     */
    private Map<ItemGroup, Integer> levelModelListTypeAOffsets = new HashMap<>();

    /**
     * Each ItemGroup's goal list offsets
     */
    private Map<ItemGroup, Integer> goalListOffsets = new HashMap<>();

    /**
     * Each ItemGroup's bumper list offsets
     */
    private Map<ItemGroup, Integer> bumperListOffsets = new HashMap<>();

    /**
     * Each ItemGroup's jamabar list offsets
     */
    private Map<ItemGroup, Integer> jamabarListOffsets = new HashMap<>();

    /**
     * Each ItemGroup's banana list offsets
     */
    private Map<ItemGroup, Integer> bananaListOffsets = new HashMap<>();

    /**
     * Each ItemGroup's wormhole list offsets
     */
    private Map<ItemGroup, Integer> wormholeListOffsets = new HashMap<>();

    /**
     * Each ItemGroup's wormhole list offsets - Mapped to wormhole names
     */
    private Map<String, Integer> wormholeNameMappedOffsets = new HashMap<>();

    /**
     * Start data offset<br>
     * Length: 20
     */
    private int startDataOffset;

    /**
     * Fallout position offset<br>
     * Length: 4
     */
    private int falloutPosOffset;

    /**
     * Number of goals
     */
    private int globalGoalNum;

    /**
     * Goal data offset<br>
     * Length: 20 * {@link SMB2LZExporter#globalGoalNum}
     */
    private int globalGoalDataOffset;

    /**
     * Number of bumpers
     */
    private int globalBumperNum;

    /**
     * Bumper data offset<br>
     * Length: 20 * {@link SMB2LZExporter#globalBumperNum}
     */
    private int globalBumperDataOffset;

    /**
     * Number of jamabars
     */
    private int globalJamabarNum;

    /**
     * Jamabar data offset<br>
     * Length: 20 * {@link SMB2LZExporter#globalJamabarNum}
     */
    private int globalJamabarDataOffset;

    /**
     * Number of bananas
     */
    private int globalBananaNum;

    /**
     * Banana data offset<br>
     * Length: 20 * {@link SMB2LZExporter#globalBananaNum}
     */
    private int globalBananaDataOffset;

    /**
     * Number of wormholes
     */
    private int globalWormholeNum;

    /**
     * Wormhole data offset<br>
     * Length: 24 * {@link SMB2LZExporter#globalWormholeNum}
     */
    private int globalWormholeDataOffset;

    /**
     * Number of fallout volumes
     */
    private int globalFalloutVolumeNum;

    /**
     * Fallout volume data offset<br>
     * Length: 32 * {@link SMB2LZExporter#globalFalloutVolumeNum}
     */
    private int globalFalloutVolumeDataOffset; //TODO

    /**
     * Number of background models
     */
    private int backgroundModelNum;

    /**
     * Offset to background model data<br>
     * Length: 56 * {@link SMB2LZExporter#backgroundModelNum}
     */
    private int backgroundModelDataOffset;

    /**
     * Number of level models
     */
    private int levelModelNum;

    /**
     * Offset to level model offsets - Type A<br>
     * Length: 12 * {@link SMB2LZExporter#levelModelNum}
     */
    private int levelModelOffsetsOffsetTypeA;

    /**
     * Offset to level model offsets - Type B<br>
     * Length: 4 * {@link SMB2LZExporter#levelModelNum}
     */
    private int levelModelOffsetsOffsetTypeB;

    public void writeRawLZ(ModelData modelData, ConfigData configData, File outFile) throws IOException {

        this.modelData = modelData;
        this.configData = configData;

        startDataOffset = FILE_HEADER_LENGTH;
        falloutPosOffset = startDataOffset + START_DATA_LENGTH;
        collisionHeaderOffset = falloutPosOffset + FALLOUT_DATA_LENGTH;

        collisionHeaderNum = configData.itemGroups.size();
        collisionHeadersTotalLength = COLLISION_HEADER_LENGTH * collisionHeaderNum;
        collisionHeadersEndOffset = collisionHeaderOffset + collisionHeadersTotalLength;

        globalGoalNum = 0;
        globalBumperNum = 0;
        globalJamabarNum = 0;
        globalBananaNum = 0;
        globalWormholeNum = 0;
        globalFalloutVolumeNum = 0;

        int nextOffset = collisionHeadersEndOffset;

        for (ItemGroup itemGroup : configData.itemGroups) {
            globalGoalNum += itemGroup.goalList.size();
            globalBumperNum += itemGroup.bumperList.size();
            globalJamabarNum += itemGroup.jamabarList.size();
            globalBananaNum += itemGroup.bananaList.size();
            globalWormholeNum += itemGroup.wormholeList.size();
            globalFalloutVolumeDataOffset += itemGroup.falloutVolumeList.size();

            //Get offsets for collision triangle data per item group
            collisionTrianglesListOffsets.put(itemGroup, nextOffset);
            nextOffset += itemGroup.getLevelModelTriangleNum(modelData) * COLLISION_TRIANGLE_LENGTH;
        }

        nextOffset = roundUpNearest4(nextOffset);

        //Get offsets for collision triangle indices per item group
        for (ItemGroup itemGroup : configData.itemGroups) {
            collisionTriangleListListOffsets.put(itemGroup, nextOffset);

            int singlePointerListLength = 0;

            for (String objectName : itemGroup.levelModels) {
                singlePointerListLength += COLLISION_TRIANGLE_INDEX_LENGTH * modelData.getObjFromModelName(objectName).tris.size();
            }
            singlePointerListLength += COLLISION_TRIANGLE_INDEX_LENGTH; //Add the 0xFFFF terminator
            singlePointerListLength = roundUpNearest4(singlePointerListLength);

            nextOffset += singlePointerListLength * getCollisionBlocksNum();
        }

        //Get offsets for collision triangle indices lists per item group
        for (ItemGroup itemGroup : configData.itemGroups) {
            collisionTrianglesListPointersOffsets.put(itemGroup, nextOffset);
            nextOffset += COLLISION_TRIANGLE_LIST_POINTER_LENGTH * getCollisionBlocksNum();
        }

        for (ItemGroup itemGroup : configData.itemGroups) {
            levelModelOffsetListTypeBOffsets.put(itemGroup, nextOffset);
            nextOffset += LEVEL_MODEL_OFFSET_TYPE_B_LENGTH * itemGroup.levelModels.size();
        }

        for (ItemGroup itemGroup : configData.itemGroups) {
            levelModelListTypeAOffsets.put(itemGroup, nextOffset);
            nextOffset += LEVEL_MODEL_TYPE_A_LENGTH * itemGroup.levelModels.size();
        }

        //Add names of objects
        for (ItemGroup itemGroup : configData.itemGroups) {
            levelModelNameListOffsets.put(itemGroup, nextOffset);

            for (String objectName : itemGroup.levelModels) {
                nextOffset += roundUpNearest4(objectName.length() + 1);
            }
        }

        //Skip level model type As
        for (int i = 0; i < levelModelNum; i++) {
            nextOffset += LEVEL_MODEL_TYPE_A_LENGTH;
        }

        if (configData.itemGroups.size() > 0) {
            levelModelOffsetsOffsetTypeA = nextOffset;
            levelModelOffsetsOffsetTypeB = levelModelOffsetListTypeBOffsets.get(configData.itemGroups.get(0));
        }

        //Get level model offsets offset type As
        for (ItemGroup itemGroup : configData.itemGroups) {
            levelModelOffsetListTypeAOffsets.put(itemGroup, nextOffset);
            nextOffset += LEVEL_MODEL_OFFSET_TYPE_A_LENGTH * itemGroup.levelModels.size();
        }

        //Get offsets for goal lists per item group
        globalGoalDataOffset = nextOffset;
        for (ItemGroup itemGroup : configData.itemGroups) {
            goalListOffsets.put(itemGroup, nextOffset);
            nextOffset += GOAL_LENGTH * itemGroup.goalList.size();
        }

        //Get offsets for bumper lists per item group
        globalBumperDataOffset = nextOffset;
        for (ItemGroup itemGroup : configData.itemGroups) {
            bumperListOffsets.put(itemGroup, nextOffset);
            nextOffset += BUMPER_LENGTH * itemGroup.bumperList.size();
        }

        //Get offsets for jamabar lists per item group
        globalJamabarDataOffset = nextOffset;
        for (ItemGroup itemGroup : configData.itemGroups) {
            jamabarListOffsets.put(itemGroup, nextOffset);
            nextOffset += JAMABAR_LENGTH * itemGroup.jamabarList.size();
        }

        //Get offsets for banana lists per item group
        globalBananaDataOffset = nextOffset;
        for (ItemGroup itemGroup : configData.itemGroups) {
            bananaListOffsets.put(itemGroup, nextOffset);
            nextOffset += BANANA_LENGTH * itemGroup.bananaList.size();
        }

        //Get offsets for wormhole lists per item group and per wormhole
        globalWormholeDataOffset = nextOffset;
        for (ItemGroup itemGroup : configData.itemGroups) {
            wormholeListOffsets.put(itemGroup, nextOffset);

            for (Map.Entry<String, Wormhole> entry : itemGroup.wormholeList.entrySet()) {
                wormholeNameMappedOffsets.put(entry.getKey(), nextOffset);
                nextOffset += WORMHOLE_LENGTH;
            }
        }

//        globalFalloutVolumeDataOffset = ; //TODO

        backgroundModelNum = configData.backgroundList.size();
//        backgroundModelDataOffset = ; //TODO

        levelModelNum = modelData.cmnObjs.size() - backgroundModelNum; //Level models = total models - background models
//        levelModelOffsetsOffsetTypeA = ; //TODO
//        levelModelOffsetsOffsetTypeB = ; //TODO

        //Generate the raw LZ
        writeFileHeader();
        writeStartData();
        writeFalloutData();
        for (ItemGroup itemGroup : configData.itemGroups) writeCollisionHeader(itemGroup);
        for (ItemGroup itemGroup : configData.itemGroups) writeCollisionTriangles(itemGroup);
        for (ItemGroup itemGroup : configData.itemGroups) writeCollisionGridTriangleList(itemGroup);
        for (ItemGroup itemGroup : configData.itemGroups) writeCollisionGridTrianglePointerList(itemGroup);
        for (ItemGroup itemGroup : configData.itemGroups) writeLevelModelOffsetTypeBList(itemGroup);
        for (ItemGroup itemGroup : configData.itemGroups) writeLevelModelTypeAList(itemGroup);
        for (ItemGroup itemGroup : configData.itemGroups) writeLevelModelNameList(itemGroup);
        for (ItemGroup itemGroup : configData.itemGroups) writeLevelModelOffsetTypeAList(itemGroup);
        for (ItemGroup itemGroup : configData.itemGroups) writeGoalList(itemGroup);
        for (ItemGroup itemGroup : configData.itemGroups) writeBumperList(itemGroup);
        for (ItemGroup itemGroup : configData.itemGroups) writeJamabarList(itemGroup);
        for (ItemGroup itemGroup : configData.itemGroups) writeBananaList(itemGroup);
        for (ItemGroup itemGroup : configData.itemGroups) writeWormholeList(itemGroup);

        //Write the file
        FileOutputStream fos = new FileOutputStream(outFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        for (Byte b : outBytes) {
            bos.write(b);
        }

        bos.close();
        fos.close();

        if (SMBWorkshopExporter.verboseLogging) {
            LogHelper.trace(SMB2LZExporter.class, "Done exporting output.lz.raw");
        }

    }

    /**
     * Writes the file header<br>
     * Length: {@link SMB2LZExporter#FILE_HEADER_LENGTH} bytes
     */
    private void writeFileHeader() {
        addBytes(new Byte[]{0x00, 0x00, 0x00, 0x00, 0x44, 0x7A, 0x00, 0x00}); //Unknown / Magic number - Offset: 0

        addInt(collisionHeaderNum); //Number of collision headers - Offset: 8
        addInt(collisionHeaderOffset); //Offset to collision header - Offset: 12

        addInt(startDataOffset); //Offset to start position data - Offset: 16
        addInt(falloutPosOffset); //Offset to fallout position data - Offset: 20

        addInt(globalGoalNum); //Number of goals - Offset: 24
        addInt(globalGoalDataOffset); //Offset to goal data list - Offset: 28
        addInt(globalBumperNum); //Number of bumpers - Offset: 32
        addInt(globalBumperDataOffset); //Offset to bumper data list - Offset: 36
        addInt(globalJamabarNum); //Number of jamabars - Offset: 40
        addInt(globalJamabarDataOffset); //Offset to jamabar data list - Offset: 44
        addInt(globalBananaNum); //Number of bananas - Offset: 48
        addInt(globalBananaDataOffset); //Offset to banana data list - Offset: 52

        addNull(24); //Offset: 56

        addInt(globalFalloutVolumeNum); //Number of fallout volumes - Offset: 80
        addInt(globalFalloutVolumeDataOffset); //Offset to fallout volumes - Offset: 84

        addInt(backgroundModelNum); //Number of background models - Offset: 88
        addInt(backgroundModelDataOffset); //Offset to background model data - Offset: 92

        addNull(12); //Unknown - Offset: 96
        addBytes(new Byte[]{0x00, 0x00, 0x00, 0x01}); //Unknown - Offset: 108
        addNull(28); //Unknown - Offset: 112

        addInt(levelModelNum); //Number of level models - Offset: 140
        addInt(levelModelOffsetsOffsetTypeA); //Offset to level model offsets - Type A - Offset: 144
        addInt(levelModelNum); //Number of level models - Offset: 148
        addInt(levelModelOffsetsOffsetTypeB); //Offset to level model offsets - Type B - Offset: 152

        addNull(20); //Unknown / Zero - Offset: 156
        addNull(4); //Unknown / Zero / Offset to something? - Offset: 176

        addInt(globalWormholeNum); //Number of wormholes - Offset: 180
        addInt(globalWormholeDataOffset); //Offset to wormhole data - Offset: 184

        addNull(4); //Offset to mystery 2 - Offset: 188
        addNull(20); //Unknown / Zero - Offset: 192
        addNull(4); //Offset to mystery 3 - Offset: 212
        addNull(1988); //Unknown / Zero - Offset: 216
    }

    /**
     * Writes start data<br>
     * Length: {@link SMB2LZExporter#START_DATA_LENGTH} bytes
     */
    private void writeStartData() {
        Start start = configData.startList.entrySet().iterator().next().getValue();

        addVec3f(start.pos); //Start X / Y / Z Pos - Offset: 0
        addVec3fAngle(start.rot); //Start X / Y / Z Rot - Offset: 12
        addNull(2); //Padding - Offset: 18
    }

    /**
     * Writes fallout data<br>
     * Length: {@link SMB2LZExporter#FALLOUT_DATA_LENGTH} bytes
     */
    private void writeFalloutData() {
        float falloutY = configData.falloutPlane;

        addFloat(falloutY); //Fallout Y Pos - Offset: 0
    }

    /**
     * Writes a collision header for a given itemGroup<br>
     * Length: {@link SMB2LZExporter#COLLISION_HEADER_LENGTH} bytes
     * @param itemGroup The item group to write the collision header for
     */
    private void writeCollisionHeader(@NotNull ItemGroup itemGroup) {
        Vec3f centerPos = new Vec3f(0.0f, 0.0f, 0.0f); //0 if no animation
        Vec3f initialRot = new Vec3f(0.0f, 0.0f, 0.0f); //0 if no animation

        int animFrameDataOffset = 0; //0 if no animation

        int triangleDataOffset = collisionTrianglesListOffsets.get(itemGroup);
        int collisionGridPointersOffset = collisionTrianglesListPointersOffsets.get(itemGroup);

        int goalNum = itemGroup.goalList.size();
        int goalDataOffset = 0;
        int bumperNum = itemGroup.bumperList.size();
        int bumperDataOffset = 0;
        int jamabarNum = itemGroup.jamabarList.size();
        int jamabarDataOffset = 0;
        int bananaNum = itemGroup.bananaList.size();
        int bananaDataOffset = 0;
        int wormholeNum = itemGroup.wormholeList.size();
        int wormholeDataOffset = 0;
        int falloutVolumeNum = itemGroup.falloutVolumeList.size();
        int falloutVolumeDataOffset = 0;
        int levelModelNum = itemGroup.levelModels.size();
        int levelModelOffsetOffsetsTypeB = levelModelOffsetListTypeBOffsets.get(itemGroup); //TODO: Revert this
//        int levelModelOffsetOffsetsTypeB = 0; //TODO: Set to 0 temporarily as setting this crashes the game for now

        float animLoopTime = 0; //0 if no animation

        //Set vars
        if (goalNum > 0) goalDataOffset = goalListOffsets.get(itemGroup);
        if (bumperNum > 0) bumperDataOffset = bumperListOffsets.get(itemGroup);
        if (jamabarNum > 0) jamabarDataOffset = jamabarListOffsets.get(itemGroup);
        if (bananaNum > 0) bananaDataOffset = bananaListOffsets.get(itemGroup);
        if (wormholeNum > 0) wormholeDataOffset = wormholeListOffsets.get(itemGroup);
//        if (falloutVolumeNum > 0) falloutVolumeDataOffset = ; //TODO

        if (itemGroup.animData != null) { //Set variables if animation data exists
            centerPos = itemGroup.animData.getRotationCenter();

            //Get initial rotation
            Map.Entry<Float, Float> initialRotXEntry = itemGroup.animData.getRotXFrames().firstEntry();
            Map.Entry<Float, Float> initialRotYEntry = itemGroup.animData.getRotYFrames().firstEntry();
            Map.Entry<Float, Float> initialRotZEntry = itemGroup.animData.getRotZFrames().firstEntry();

            float initialRotX = initialRotXEntry == null ? 0.0f : initialRotXEntry.getValue();
            float initialRotY = initialRotYEntry == null ? 0.0f : initialRotYEntry.getValue();
            float initialRotZ = initialRotZEntry == null ? 0.0f : initialRotZEntry.getValue();

            initialRot = new Vec3f(initialRotX, initialRotY, initialRotZ);

//            animFrameDataOffset = ; //TODO

            animLoopTime = itemGroup.animData.loopTime;
        }

        //Add data
        addFloat(centerPos.x); //Animation center X - Offset: 0
        addFloat(centerPos.y); //Animation center Y - Offset: 4
        addFloat(centerPos.z); //Animation center Z - Offset: 8

        addShort(cnvAngle(initialRot.x)); //Initial rotation X - Offset: 12
        addShort(cnvAngle(initialRot.y)); //Initial rotation Y - Offset: 14
        addShort(cnvAngle(initialRot.z)); //Initial rotation Z - Offset: 16
        addNull(2); //Padding - Offset: 18

        addInt(animFrameDataOffset); //Animation frame data offset - Offset: 20

        addNull(12); //Unknown / Null - Offset: 24

        addInt(triangleDataOffset); //Triangle data offset - Offset: 36
        addInt(collisionGridPointersOffset); //Collision grid pointers offset - Offset: 40

        addFloat(COLLISION_X_START); //Collision start X - Offset: 44
        addFloat(COLLISION_Z_START); //Collision start Z - Offset: 48
        addFloat(COLLISION_X_STEP); //Collision step X - Offset: 52
        addFloat(COLLISION_Z_STEP); //Collision step Z - Offset: 56
        addInt(COLLISION_X_STEP_NUM); //Collision step X count - Offset: 60
        addInt(COLLISION_Z_STEP_NUM); //Collision step Z count - Offset: 64

        addInt(goalNum); //Number of goals - Offset: 68
        addInt(goalDataOffset); //Offset to goal data list - Offset: 72
        addInt(bumperNum); //Number of bumpers - Offset: 76
        addInt(bumperDataOffset); //Offset to bumper data list - Offset: 80
        addInt(jamabarNum); //Number of jamabars - Offset: 84
        addInt(jamabarDataOffset); //Offset to jamabar data list - Offset: 88
        addInt(bananaNum); //Number of bananas - Offset: 92
        addInt(bananaDataOffset); //Offset to banana data list - Offset: 96

        addNull(24); //Unknown / Zero - Offset: 100

        addInt(falloutVolumeNum); //Number of fallout volumes - Offset: 124
        addInt(falloutVolumeDataOffset); //Offset to fallout volume data list - Offset: 128

        addNull(16); //Unknown / Zero - Offset: 132

        addInt(levelModelNum); //Number of level models - Offset: 148
        addInt(levelModelOffsetOffsetsTypeB); //Offset to level model offsets - Type B - Offset: 152

        addNull(28); //Unknown / Zero - Offset: 156
        addNull(4); //Offset to mystery 5 - Offset: 184
        addNull(8); //Unknown / Zero - Offset: 188

        addInt(wormholeNum); //Number of wormholes - Offset: 196
        addInt(wormholeDataOffset); //Offset to wormhole data list - Offset: 200

        addNull(8); //Unknown / Zero - Offset: 204

        addFloat(animLoopTime); //Animation loop time - Offset: 212

        addNull(964); //Unknown / Zero - Offset: 216
    }

    /**
     * Code mostly ported from smb2cnv<br>
     * Length: {@link SMB2LZExporter#COLLISION_TRIANGLE_LENGTH} * number of triangles bytes<br>
     * Number of triangles = number of triangles per each object in {@link ItemGroup#levelModels}
     *
     * @param itemGroup The item group to write collision triangles for
     */
    private void writeCollisionTriangles(ItemGroup itemGroup) {
        for (String objectName : itemGroup.levelModels) {

            Obj obj = modelData.getObjFromModelName(objectName);

            for (int j = 0; j < obj.tris.size(); j++) {
                Triangle tri = obj.tris.get(j);

//                Vec3f na = new Vec3f(modelData.cmnVerticies.get(tri.vertANorm - 1).x, modelData.cmnVerticies.get(tri.vertANorm - 1).y, modelData.cmnVerticies.get(tri.vertANorm - 1).z);
                Vec3f a = new Vec3f(modelData.cmnVerticies.get(tri.vertA - 1).x, modelData.cmnVerticies.get(tri.vertA - 1).y, modelData.cmnVerticies.get(tri.vertA - 1).z);
                Vec3f b = new Vec3f(modelData.cmnVerticies.get(tri.vertB - 1).x, modelData.cmnVerticies.get(tri.vertB - 1).y, modelData.cmnVerticies.get(tri.vertB - 1).z);
                Vec3f c = new Vec3f(modelData.cmnVerticies.get(tri.vertC - 1).x, modelData.cmnVerticies.get(tri.vertC - 1).y, modelData.cmnVerticies.get(tri.vertC - 1).z);
//                if (a.y < configData.falloutPlane) configData.falloutPlane = a.y;
//                if (b.y < configData.falloutPlane) configData.falloutPlane = b.y;
//                if (c.y < configData.falloutPlane) configData.falloutPlane = c.y;
                Vec3f ba = new Vec3f(b.x - a.x, b.y - a.y, b.z - a.z);
                Vec3f ca = new Vec3f(c.x - a.x, c.y - a.y, c.z - a.z);
                Vec3f normal = normalize(cross(normalize(ba), normalize(ca)));
                float l = (float) Math.sqrt(normal.x * normal.x + normal.z * normal.z);
                float cy = normal.z / l;
                float sy = -normal.x / l;
                if (Math.abs(l) < 0.001) {
                    cy = 1.0f;
                    sy = 0.0f;
                }
                float cx = l;
                float sx = normal.y;
                Vec3f Rxr0 = new Vec3f(1.0f, 0.0f, 0.0f);
                Vec3f Rxr1 = new Vec3f(0.0f, cx, sx);
                Vec3f Rxr2 = new Vec3f(0.0f, -sx, cx);
                Vec3f Ryr0 = new Vec3f(cy, 0.0f, -sy);
                Vec3f Ryr1 = new Vec3f(0.0f, 1.0f, 0.0f);
                Vec3f Ryr2 = new Vec3f(sy, 0.0f, cy);
                Vec3f dotry = dotm(ba, Ryr0, Ryr1, Ryr2);
                Vec3f dotrxry = dotm(dotry, Rxr0, Rxr1, Rxr2);
                l = (float) Math.sqrt(dotrxry.x * dotrxry.x + dotrxry.y * dotrxry.y);
                float cz = dotrxry.x / l;
                float sz = -dotrxry.y / l;
                Vec3f Rzr0 = new Vec3f(cz, sz, 0.0f);
                Vec3f Rzr1 = new Vec3f(-sz, cz, 0.0f);
                Vec3f Rzr2 = new Vec3f(0.0f, 0.0f, 1.0f);
                Vec3f dotrz = dotm(dotrxry, Rzr0, Rzr1, Rzr2);
                dotry = dotm(ca, Ryr0, Ryr1, Ryr2);
                dotrxry = dotm(dotry, Rxr0, Rxr1, Rxr2);
                Vec3f dotrzrxry = dotm(dotrxry, Rzr0, Rzr1, Rzr2);
                Vec3f n0v = new Vec3f(dotrzrxry.x-dotrz.x, dotrzrxry.y-dotrz.y, dotrzrxry.z-dotrz.z);
                Vec3f n1v = new Vec3f(-dotrzrxry.x, -dotrzrxry.y, -dotrzrxry.z);
                Vec3f n0 = normalize(hat(n0v));
                Vec3f n1 = normalize(hat(n1v));
                float rotX = 360.0f - reverseAngle(cx, sx);
                float rotY = 360.0f - reverseAngle(cy, sy);
                float rotZ = 360.0f - reverseAngle(cz, sz);

                addFloat(a.x);
                addFloat(a.y);
                addFloat(a.z);

                addFloat(normal.x);
                addFloat(normal.y);
                addFloat(normal.z);
                addShort((cnvAngle(rotX)));
                addShort((cnvAngle(rotY)));
                addShort((cnvAngle(rotZ)));
                addByte(0);
                addByte(0);
                addFloat(dotrz.x);
                addFloat(dotrz.y);
                addFloat(dotrzrxry.x);
                addFloat(dotrzrxry.y);
                addFloat(n0.x);
                addFloat(n0.y);
                addFloat(n1.x);
                addFloat(n1.y);
            }

        }
    }

    /**
     * Writes the list of triangles used for each object's collision
     *
     * @param itemGroup The item group to write collision triangles for
     */
    private void writeCollisionGridTriangleList(ItemGroup itemGroup) {
        for (int i = 0; i < getCollisionBlocksNum(); i++) {

            //Write a list of all triangles for every collision block
            //Not the most efficient, but quick and easy
            //The game will check every triangle no matter what collision block you are in
            int triCount = 0;

            for (String objectName : itemGroup.levelModels) {
                Obj object = modelData.getObjFromModelName(objectName);

                //Write triangle indices: numbers 0 - triangleCount
                for (int j = 0; j < object.tris.size(); j++) {
                    addShort(triCount);
                    triCount++;
                }

            }

            addShort(0xFFFF); //Terminate the list with 0xFFFF

            padTo4ByteAlign();
        }
    }

    /**
     * Writes offsets to a triangle list in the collision grid section for each object
     *
     * @param itemGroup The item group to write collision grid triangle list pointers for
     */
    private void writeCollisionGridTrianglePointerList(ItemGroup itemGroup) {
        int nextOffset = collisionTriangleListListOffsets.get(itemGroup);

//        for (String objectName : itemGroup.levelModels) {
//            int nextOffset = objectToOffsetMap.get(objectName);
//
//            int collisionBlocks = getCollisionBlocksNum();
//            for (int i = 0; i < collisionBlocks; i++) {
//                addInt(nextOffset);
//
//                nextOffset += modelData.getObjFromModelName(objectName).tris.size() * COLLISION_TRIANGLE_INDEX_LENGTH;
//            }
//        }

        int collisionBlocks = getCollisionBlocksNum();
        for (int i = 0; i < collisionBlocks; i++) {
            addInt(nextOffset);

            for (String objectName : itemGroup.levelModels) {
                nextOffset += modelData.getObjFromModelName(objectName).tris.size() * COLLISION_TRIANGLE_INDEX_LENGTH;
            }

            nextOffset += 2; //Add the 0xFFFF terminator
            nextOffset = roundUpNearest4(nextOffset);
        }
    }

    /**
     * Writes offsets to a level model - Type B
     *
     * @param itemGroup The item group to write model offsets for
     */
    private void writeLevelModelOffsetTypeBList(ItemGroup itemGroup) {
        int nextOffset = levelModelOffsetListTypeAOffsets.get(itemGroup);

        for (String objectName : itemGroup.levelModels) {
            addInt(nextOffset);
            nextOffset += LEVEL_MODEL_OFFSET_TYPE_A_LENGTH;
        }
    }

    /**
     * Writes level models - Type A
     *
     * @param itemGroup The item group to write models for
     */
    private void writeLevelModelTypeAList(ItemGroup itemGroup) {
        int nextOffset = levelModelNameListOffsets.get(itemGroup);

        int i = 0;
        for (String objectName : itemGroup.levelModels) {
            addNull(4);
            addInt(nextOffset);
            addNull(8);
            nextOffset += roundUpNearest4(objectName.length() + 1);
            i++;
        }
    }

    /**
     * Writes level models names
     *
     * @param itemGroup The item group to write names for
     */
    private void writeLevelModelNameList(ItemGroup itemGroup) {
        for (String objectName : itemGroup.levelModels) {
            addNullTerminatedString(objectName);
            padTo4ByteAlign();
        }
    }

    /**
     * Writes offsets to a level model - Type A
     *
     * @param itemGroup The item group to write model offsets for
     */
    private void writeLevelModelOffsetTypeAList(ItemGroup itemGroup) {
        int nextOffset = levelModelListTypeAOffsets.get(itemGroup);

        for (String objectName : itemGroup.levelModels) {
            addNull(4);
            addInt(0x00000001);
            addInt(nextOffset);
            nextOffset += LEVEL_MODEL_TYPE_A_LENGTH;
        }
    }

    /**
     * Writes goals
     *
     * @param itemGroup The item group to write goals for
     */
    private void writeGoalList(ItemGroup itemGroup) {
        for (Map.Entry<String, Goal> entry : itemGroup.goalList.entrySet()) {
            Goal goal = entry.getValue();

            short goalType;
            switch (goal.type) {
                case BLUE:
                    goalType = 0x0001;
                    break;
                case GREEN:
                    goalType = 0x0101;
                    break;
                case RED:
                    goalType = 0x0201;
                    break;
                default: //Shouldn't happen, but default to blue
                    goalType = 0x0001;
                    break;
            }

            addVec3f(goal.pos); //Pos X / Y / Z - Offset: 0
            addVec3fAngle(goal.rot); //Rot X / Y / Z - Offset: 12

            addShort(goalType); //Goal type - Offset: 18
        }
    }

    /**
     * Writes bumpers
     *
     * @param itemGroup The item group to write bumpers for
     */
    private void writeBumperList(ItemGroup itemGroup) {
        for (Map.Entry<String, Bumper> entry : itemGroup.bumperList.entrySet()) {
            Bumper bumper = entry.getValue();

            addVec3f(bumper.pos); //Pos X / Y / Z - Offset: 0
            addVec3fAngle(bumper.rot); //Rot X / Y / Z - Offset: 12
            addNull(2); //Padding - Offset: 18
            addVec3f(bumper.scl); //Pos X / Y / Z - Offset: 20
        }
    }

    /**
     * Writes jamabars
     *
     * @param itemGroup The item group to write jamabars for
     */
    private void writeJamabarList(ItemGroup itemGroup) {
        for (Map.Entry<String, Jamabar> entry : itemGroup.jamabarList.entrySet()) {
            Jamabar jamabar = entry.getValue();

            addVec3f(jamabar.pos); //Pos X / Y / Z - Offset: 0

            addVec3fAngle(jamabar.rot); //Rot X / Y / Z - Offset: 12
            addNull(2); //Padding - Offset: 18

            addVec3f(jamabar.scl); //Scale X / Y / Z - Offset: 20

        }
    }

    /**
     * Writes bananas
     *
     * @param itemGroup The item group to write bananas for
     */
    private void writeBananaList(ItemGroup itemGroup) {
        for (Map.Entry<String, Banana> entry : itemGroup.bananaList.entrySet()) {
            Banana banana = entry.getValue();

            int bananaType;
            switch (banana.type) {
                case SINGLE:
                    bananaType = 0;
                    break;
                case BUNCH:
                    bananaType = 1;
                    break;
                default: //Shouldn't happen, but default to single
                    bananaType = 0;
                    break;
            }

            addVec3f(banana.pos); //Pos X / Y / Z - Offset: 0

            addInt(bananaType); //Banana type - Offset: 12


        }
    }

    /**
     * Writes wormholes
     *
     * @param itemGroup The item group to write wormholes for
     */
    private void writeWormholeList(ItemGroup itemGroup) {
        for (Map.Entry<String, Wormhole> entry : itemGroup.wormholeList.entrySet()) {
            Wormhole wormhole = entry.getValue();

            addInt(0x00000001); //0x00000001 - Offset: 0

            addVec3f(wormhole.pos); //Pos X / Y / Z - Offset: 4

            addVec3fAngle(wormhole.rot); //Rot X / Y / Z - Offset: 16
            addNull(2); //Padding - Offset: 122

            addInt(wormholeNameMappedOffsets.get(wormhole.destinationName)); //Offset to destination wormhole - Offset: 24
        }
    }

    private int getCollisionBlocksNum() {
        return COLLISION_X_STEP_NUM * COLLISION_Z_STEP_NUM;
    }

    private int roundUpNearest4(int n) {
        if (n % 4 == 0) return n;
        return (n + 3) / 4 * 4;
    }

    private static int cnvAngle(float theta) {
        return (int) (65536.0 * theta / 360.0);
    }

    private static Vec3f normalize(Vec3f toNorm) {
        Vec3f v = new Vec3f(toNorm.x, toNorm.y, toNorm.z);
        float len = (float) Math.sqrt((v.x * v.x) + (v.y * v.y) + (v.z * v.z));
        v.x /= len;
        v.y /= len;
        v.z /= len;
        return v;
    }

    private static Vec3f cross(Vec3f a, Vec3f b) {
        float d0 = (a.y * b.z) - (a.z * b.y);
        float d1 = (a.z * b.x) - (a.x * b.z);
        float d2 = (a.x * b.y) - (a.y * b.x);
        return new Vec3f(d0, d1, d2);
    }

    private static Vec3f dotm(Vec3f a, Vec3f r0, Vec3f r1, Vec3f r2) {
        float d0 = (a.x * r0.x) + (a.y * r1.x) + (a.z * r2.x);
        float d1 = (a.x * r0.y) + (a.y * r1.y) + (a.z * r2.y);
        float d2 = (a.x * r0.z) + (a.y * r1.z) + (a.z * r2.z);
        return new Vec3f(d0, d1, d2);
    }

    private static Vec3f hat(Vec3f v) {
        return new Vec3f(-v.y, v.x, 0.0f);
    }

    private static float reverseAngle(float c, float s) {
        float a = (float) Math.toDegrees(Math.asin(s));
        if (c < 0.0) {
            a = 180.0f - a;
        }
        if (Math.abs(c) < Math.abs(s)) {
            a = (float) Math.toDegrees(Math.acos(c));
            if (s < 0.0) {
                a =- a;
            }
        }
        if (a < 0.0) {
            if (a >- 0.001) {
                a = 0.0f;
            }
            else a += 360.0;
        }
        return a;
    }

    private void padTo4ByteAlign() {
        int toAdd = outBytes.size() % 4;
        if (toAdd != 0) {
            addNull(4 - toAdd);
        }
    }

    private void addByte(byte b) {
        outBytes.add(b);
    }

    private void addBytes(Collection<Byte> b) {
        outBytes.addAll(b);
    }

    private void addBytes(Byte[] b) {
        outBytes.addAll(Arrays.asList(b));
    }

    private void addByte(int b) {
        outBytes.add((byte) b);
    }

    private void addInt(int i) {
        addByte((i >>> 24) & 0xFF);
        addByte((i >>> 16) & 0xFF);
        addByte((i >>>  8) & 0xFF);
        addByte((i       ) & 0xFF);
    }

    private void addFloat(float f) {
        addInt(Float.floatToIntBits(f));
    }

    private void addVec3f(Vec3f v) {
        addFloat(v.x);
        addFloat(v.y);
        addFloat(v.z);
    }

    private void addVec3fAngle(Vec3f v) {
        addShort(cnvAngle(v.x));
        addShort(cnvAngle(v.y));
        addShort(cnvAngle(v.z));
    }

    private void addShort(int s) {
        addByte((s >>> 8) & 0xFF);
        addByte((s      ) & 0xFF);
    }

    private void addNullTerminatedString(String s) {
        for (char c : s.toCharArray()) {
            addByte(c);
        }
    }

    private void addNull(int num) {
        for (int i = 0; i < num; i++) {
            addByte(0);
        }
    }

}
