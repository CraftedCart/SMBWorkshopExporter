package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.util.LogHelper;
import craftedcart.smbworkshopexporter.util.Vec3f;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CraftedCart
 *         Created on 18/02/2017 (DD/MM/YYYY)
 */
public class ConfigAnimData {

    protected Vec3f rotationCenter = new Vec3f();

    @Deprecated protected TreeMap<Float, Float> posXFrames = new TreeMap<>();
    @Deprecated protected TreeMap<Float, Float> posYFrames = new TreeMap<>();
    @Deprecated protected TreeMap<Float, Float> posZFrames = new TreeMap<>();

    @Deprecated protected TreeMap<Float, Float> rotXFrames = new TreeMap<>();
    @Deprecated protected TreeMap<Float, Float> rotYFrames = new TreeMap<>();
    @Deprecated protected TreeMap<Float, Float> rotZFrames = new TreeMap<>();

    protected float loopTime = 1000.0f;

    public void setRotationCenter(Vec3f rotationCenter) {
        this.rotationCenter = rotationCenter;
    }

    @NotNull
    public Vec3f getRotationCenter() {
        return rotationCenter;
    }

    //Pos
    @Deprecated
    public void setPosXFrame(float time, float pos) {
        posXFrames.put(time, pos);
    }

    @Deprecated
    public void setPosYFrame(float time, float pos) {
        posYFrames.put(time, pos);
    }

    @Deprecated
    public void setPosZFrame(float time, float pos) {
        posZFrames.put(time, pos);
    }

    @Deprecated
    public void removePosXFrame(float time) {
        posXFrames.remove(time);
    }

    @Deprecated
    public void removePosYFrame(float time) {
        posYFrames.remove(time);
    }

    @Deprecated
    public void removePosZFrame(float time) {
        posZFrames.remove(time);
    }

    @Deprecated
    public TreeMap<Float, Float> getPosXFrames() {
        return posXFrames;
    }

    @Deprecated
    public TreeMap<Float, Float> getPosYFrames() {
        return posYFrames;
    }

    @Deprecated
    public TreeMap<Float, Float> getPosZFrames() {
        return posZFrames;
    }

    //Rot
    @Deprecated
    public void setRotXFrame(float time, float rot) {
        rotXFrames.put(time, rot);
    }

    @Deprecated
    public void setRotYFrame(float time, float rot) {
        rotYFrames.put(time, rot);
    }

    @Deprecated
    public void setRotZFrame(float time, float rot) {
        rotZFrames.put(time, rot);
    }

    @Deprecated
    public void removeRotXFrame(float time) {
        rotXFrames.remove(time);
    }

    @Deprecated
    public void removeRotYFrame(float time) {
        rotYFrames.remove(time);
    }

    @Deprecated
    public void removeRotZFrame(float time) {
        rotZFrames.remove(time);
    }

    @Deprecated
    public TreeMap<Float, Float> getRotXFrames() {
        return rotXFrames;
    }

    @Deprecated
    public TreeMap<Float, Float> getRotYFrames() {
        return rotYFrames;
    }

    @Deprecated
    public TreeMap<Float, Float> getRotZFrames() {
        return rotZFrames;
    }

    public void setLoopTime(float loopTime) {
        this.loopTime = loopTime;
    }

    public float getLoopTime() {
        return loopTime;
    }

    /**
     * @param animFile The animation config file
     * @param maxAndLeadInTime Total time
     * @throws IOException File IO error
     */
    public void parseAnimConfig(File animFile, float maxAndLeadInTime) throws IOException {
        LogHelper.info(getClass(), "Parsing anim config: " + animFile.getAbsolutePath());

        Map<String, AnimDataFrame> indexedAnimData = new HashMap<>();

        FileInputStream fis = new FileInputStream(animFile);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        Pattern p = Pattern.compile("(.*) \\[ (.*) \\] \\. (.*) \\. (.*) = (.*)");

        String line;

        while ((line = br.readLine()) != null) {

            String[] splitLine = line.split("\\s+");

            if (Objects.equals(splitLine[0], "frame")) {
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid anim config pattern at line \"%s\"", line));
                }

                //Get AnimDataFrame object
                AnimDataFrame adf;
                if (indexedAnimData.containsKey(m.group(2))) {
                    //ID already exists
                    adf = indexedAnimData.get(m.group(2));
                } else {
                    adf = new AnimDataFrame();
                    indexedAnimData.put(m.group(2), adf);
                }

                if (Objects.equals(m.group(3), "pos")) { //Position
                    if (Objects.equals(m.group(4), "x")) {
                        adf.posX = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        adf.posY = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        adf.posZ = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "rot")) { //Rotation
                    if (Objects.equals(m.group(4), "x")) {
                        adf.rotX = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        adf.rotY = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        adf.rotZ = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "time")) { //Time
                    adf.time = Float.parseFloat(m.group(5)) / maxAndLeadInTime;

                } else {
                    LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(3), line));
                }

            }
        }

        LogHelper.info(getClass(), "Inserting AnimDataFrames");

        for (Map.Entry<String, AnimDataFrame> entry : indexedAnimData.entrySet()) {
            insertAnimDataFrame(entry.getValue());
        }

        LogHelper.info(getClass(), "Finished parsing anim config");
    }

    @Deprecated
    private void insertAnimDataFrame(AnimDataFrame adf) {
        if (adf.posX != null) posXFrames.put(adf.time, adf.posX);
        if (adf.posY != null) posYFrames.put(adf.time, adf.posY);
        if (adf.posZ != null) posZFrames.put(adf.time, adf.posZ);

        if (adf.rotX != null) rotXFrames.put(adf.time, adf.rotX);
        if (adf.rotY != null) rotYFrames.put(adf.time, adf.rotY);
        if (adf.rotZ != null) rotZFrames.put(adf.time, adf.rotZ);
    }

    @Deprecated
    class AnimDataFrame {
        float time = 0.0f;
        Float posX;
        Float posY;
        Float posZ;
        Float rotX;
        Float rotY;
        Float rotZ;
    }

}
