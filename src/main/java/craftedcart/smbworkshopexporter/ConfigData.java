package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.util.LogHelper;
import craftedcart.smbworkshopexporter.util.Vec3f;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CraftedCart
 *         Created on 20/09/2016 (DD/MM/YYYY)
 */
public class ConfigData {

    public Map<String, Start> startList = new HashMap<>();
    public Map<String, Goal> goalList = new HashMap<>();
    public Map<String, Bumper> bumperList = new HashMap<>();
    public Map<String, Jamabar> jamabarList = new HashMap<>();
    public Map<String, Banana> bananaList = new HashMap<>();
    public List<String> backgroundList = new ArrayList<>();

    public Map<String /* Index, not object name */, ConfigAnimData> animDataMap = new HashMap<>();

    public float falloutPlane = 0.0f;

    public float maxTime = 60.0f;
    public float leadInTime = 6.0f;

    public void parseConfig(File configFile) throws IOException, IllegalStateException, NumberFormatException {
        
        FileInputStream fis = new FileInputStream(configFile);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        Pattern p = Pattern.compile("(.*) \\[ (.*) \\] \\. (.*) \\. (.*) = (.*)");

        String line;

        while ((line = br.readLine()) != null) {

            String[] splitLine = line.split("\\s+");

            if (Objects.equals(splitLine[0], "start")) { //Start
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern at line \"%s\"", line));
                }

                //Get start object
                Start start;
                if (startList.containsKey(m.group(2))) {
                    //ID already exists
                    start = startList.get(m.group(2));
                } else {
                    start = new Start();
                    startList.put(m.group(2), start);
                }

                if (Objects.equals(m.group(3), "pos")) { //Position
                    if (Objects.equals(m.group(4), "x")) {
                        start.posX = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        start.posY = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        start.posZ = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "rot")) { //Rotation
                    if (Objects.equals(m.group(4), "x")) {
                        start.rotX = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        start.rotY = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        start.rotZ = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else {
                    LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(3), line));
                }

            } else if (Objects.equals(splitLine[0], "goal")) { //Goal
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                //Get goal object
                Goal goal;
                if (goalList.containsKey(m.group(2))) {
                    //ID already exists
                    goal = goalList.get(m.group(2));
                } else {
                    goal = new Goal();
                    goalList.put(m.group(2), goal);
                }

                if (Objects.equals(m.group(3), "pos")) { //Position
                    if (Objects.equals(m.group(4), "x")) {
                        goal.posX = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        goal.posY = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        goal.posZ = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "rot")) { //Rotation
                    if (Objects.equals(m.group(4), "x")) {
                        goal.rotX = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        goal.rotY = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        goal.rotZ = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "type")) { //Type
                    if (Objects.equals(m.group(5), "B")) {
                        goal.type = 0;
                    } else if (Objects.equals(m.group(5), "G")) {
                        goal.type = 1;
                    } else if (Objects.equals(m.group(5), "R")) {
                        goal.type = 2;
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else {
                    LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(3), line));
                }

            } else if (Objects.equals(splitLine[0], "bumper")) { //Bumper
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                //Get bumper object
                Bumper bumper;
                if (bumperList.containsKey(m.group(2))) {
                    //ID already exists
                    bumper = bumperList.get(m.group(2));
                } else {
                    bumper = new Bumper();
                    bumperList.put(m.group(2), bumper);
                }

                if (Objects.equals(m.group(3), "pos")) { //Position
                    if (Objects.equals(m.group(4), "x")) {
                        bumper.posX = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        bumper.posY = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        bumper.posZ = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "rot")) { //Rotation
                    if (Objects.equals(m.group(4), "x")) {
                        bumper.rotX = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        bumper.rotY = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        bumper.rotZ = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "scl")) { //Scale
                    if (Objects.equals(m.group(4), "x")) {
                        bumper.sclX = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        bumper.sclY = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        bumper.sclZ = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else {
                    LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(3), line));
                }

            } else if (Objects.equals(splitLine[0], "jamabar")) { //Jamabar
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                //Get jamabar object
                Jamabar jamabar;
                if (jamabarList.containsKey(m.group(2))) {
                    //ID already exists
                    jamabar = jamabarList.get(m.group(2));
                } else {
                    jamabar = new Jamabar();
                    jamabarList.put(m.group(2), jamabar);
                }

                if (Objects.equals(m.group(3), "pos")) { //Position
                    if (Objects.equals(m.group(4), "x")) {
                        jamabar.posX = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        jamabar.posY = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        jamabar.posZ = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "rot")) { //Rotation
                    if (Objects.equals(m.group(4), "x")) {
                        jamabar.rotX = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        jamabar.rotY = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        jamabar.rotZ = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "scl")) { //Scale
                    if (Objects.equals(m.group(4), "x")) {
                        jamabar.sclX = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        jamabar.sclY = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        jamabar.sclZ = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else {
                    LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(3), line));
                }

            } else if (Objects.equals(splitLine[0], "banana")) { //Banana
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                //Get banana object
                Banana banana;
                if (bananaList.containsKey(m.group(2))) {
                    //ID already exists
                    banana = bananaList.get(m.group(2));
                } else {
                    banana = new Banana();
                    bananaList.put(m.group(2), banana);
                }

                if (Objects.equals(m.group(3), "pos")) { //Position
                    if (Objects.equals(m.group(4), "x")) {
                        banana.posX = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        banana.posY = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        banana.posZ = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "type")) { //Type
                    if (Objects.equals(m.group(5), "N")) { //Single banana
                        banana.type = 0;
                    } else if (Objects.equals(m.group(5), "B")) { //Banana bunch
                        banana.type = 1;
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else {
                    LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(3), line));
                }

            } else if (Objects.equals(splitLine[0], "fallout")) { //Fallout
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                if (Objects.equals(m.group(3), "pos")) { //Position
                    if (Objects.equals(m.group(4), "y")) {
                        falloutPlane = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else {
                    LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(3), line));
                }

            } else if (Objects.equals(splitLine[0], "background")) { //Background
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                backgroundList.add(m.group(5));

            } else if (Objects.equals(splitLine[0], "maxtime")) { //Max time
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                maxTime = Float.parseFloat(m.group(5));

            } else if (Objects.equals(splitLine[0], "leadintime")) { //Lead in time
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                leadInTime = Float.parseFloat(m.group(5));

            } else if (Objects.equals(splitLine[0], "animobj")) {
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                //Get ConfigAnimData object
                ConfigAnimData ad;
                if (animDataMap.containsKey(m.group(2))) {
                    //ID already exists
                    ad = animDataMap.get(m.group(2));
                } else {
                    ad = new ConfigAnimData();
                    animDataMap.put(m.group(2), ad);
                }

                if (Objects.equals(m.group(3), "file")) {
                    ad.parseAnimConfig(new File(configFile.getParentFile(), m.group(5)), maxTime + leadInTime);
                } else if (Objects.equals(m.group(3), "name")) {
                    ad.setObjectName(m.group(5));
                } else if (Objects.equals(m.group(3), "center")) { //Set rotation center
                    if (Objects.equals(m.group(4), "x")) {
                        Vec3f oldCenter = ad.getRotationCenter();
                        ad.setRotationCenter(new Vec3f(Float.parseFloat(m.group(5)), oldCenter.y, oldCenter.z));
                    } else if (Objects.equals(m.group(4), "y")) {
                        Vec3f oldCenter = ad.getRotationCenter();
                        ad.setRotationCenter(new Vec3f(oldCenter.x, Float.parseFloat(m.group(5)), oldCenter.z));
                    } else if (Objects.equals(m.group(4), "z")) {
                        Vec3f oldCenter = ad.getRotationCenter();
                        ad.setRotationCenter(new Vec3f(oldCenter.x, oldCenter.y, Float.parseFloat(m.group(5))));
                    }
                }

            } else if (!Objects.equals(line, "")) { //If the line is not empty
                LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", splitLine[0], line));
            }
        }

        br.close();
        isr.close();
        fis.close();

    }

    public class Start {
        public float posX;
        public float posY;
        public float posZ;

        public float rotX;
        public float rotY;
        public float rotZ;
    }

    public class Goal {
        public float posX;
        public float posY;
        public float posZ;

        public float rotX;
        public float rotY;
        public float rotZ;

        public int type = 0;
    }

    public class Bumper {
        public float posX;
        public float posY;
        public float posZ;

        public float rotX;
        public float rotY;
        public float rotZ;

        public float sclX;
        public float sclY;
        public float sclZ;
    }

    public class Jamabar {
        public float posX;
        public float posY;
        public float posZ;

        public float rotX;
        public float rotY;
        public float rotZ;

        public float sclX;
        public float sclY;
        public float sclZ;
    }

    public class Banana {
        public float posX;
        public float posY;
        public float posZ;

        public int type;
    }

}
