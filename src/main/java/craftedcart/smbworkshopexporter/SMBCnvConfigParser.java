package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.placeables.*;
import craftedcart.smbworkshopexporter.util.LogHelper;
import craftedcart.smbworkshopexporter.util.Vec3f;

import java.io.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CraftedCart
 *         Created on 18/03/2017 (DD/MM/YYYY)
 */
public class SMBCnvConfigParser {

    public static void parseConfig(ConfigData configData, File configFile) throws IOException, IllegalStateException, NumberFormatException {

        //TODO: Fallout volumes
        //TODO: Wormholes

        //Create static itemGroup
        configData.addItemGroup("Static", new ItemGroup());

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
                if (configData.startList.containsKey("Start position" + m.group(2))) {
                    //ID already exists
                    start = configData.startList.get("Start position" + m.group(2));
                } else {
                    start = new Start();
                    configData.startList.put("Start position" + m.group(2), start);
                }

                if (Objects.equals(m.group(3), "pos")) { //Position
                    if (Objects.equals(m.group(4), "x")) {
                        start.pos.x = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        start.pos.y = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        start.pos.z = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "rot")) { //Rotation
                    if (Objects.equals(m.group(4), "x")) {
                        start.rot.x = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        start.rot.y = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        start.rot.z = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else {
                    LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(3), line));
                }

            } else if (Objects.equals(splitLine[0], "goal")) { //Goal
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                //Get goal object
                Goal goal;
                if (configData.getFirstItemGroup().goalList.containsKey("Goal " + m.group(2))) {
                    //ID already exists
                    goal = configData.getFirstItemGroup().goalList.get("Goal " + m.group(2));
                } else {
                    goal = new Goal();
                    configData.getFirstItemGroup().goalList.put("Goal " + m.group(2), goal);
                }

                if (Objects.equals(m.group(3), "pos")) { //Position
                    if (Objects.equals(m.group(4), "x")) {
                        goal.pos.x = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        goal.pos.y = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        goal.pos.z = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "rot")) { //Rotation
                    if (Objects.equals(m.group(4), "x")) {
                        goal.rot.x = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        goal.rot.y = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        goal.rot.z = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "type")) { //Type
                    if (Objects.equals(m.group(5), "B")) {
                        goal.type = Goal.EnumGoalType.BLUE;
                    } else if (Objects.equals(m.group(5), "G")) {
                        goal.type = Goal.EnumGoalType.GREEN;
                    } else if (Objects.equals(m.group(5), "R")) {
                        goal.type = Goal.EnumGoalType.RED;
                    } else {
                        LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else {
                    LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(3), line));
                }

            } else if (Objects.equals(splitLine[0], "bumper")) { //Bumper
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                //Get bumper object
                Bumper bumper;
                if (configData.getFirstItemGroup().bumperList.containsKey("Bumper " + m.group(2))) {
                    //ID already exists
                    bumper = configData.getFirstItemGroup().bumperList.get("Bumper " + m.group(2));
                } else {
                    bumper = new Bumper();
                    configData.getFirstItemGroup().bumperList.put("Bumper " + m.group(2), bumper);
                }

                if (Objects.equals(m.group(3), "pos")) { //Position
                    if (Objects.equals(m.group(4), "x")) {
                        bumper.pos.x = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        bumper.pos.y = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        bumper.pos.z = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "rot")) { //Rotation
                    if (Objects.equals(m.group(4), "x")) {
                        bumper.rot.x = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        bumper.rot.y = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        bumper.rot.z = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "scl")) { //Scale
                    if (Objects.equals(m.group(4), "x")) {
                        bumper.scl.x = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        bumper.scl.y = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        bumper.scl.z = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else {
                    LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(3), line));
                }

            } else if (Objects.equals(splitLine[0], "jamabar")) { //Jamabar
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                //Get jamabar object
                Jamabar jamabar;
                if (configData.getFirstItemGroup().jamabarList.containsKey("Jamabar " + m.group(2))) {
                    //ID already exists
                    jamabar = configData.getFirstItemGroup().jamabarList.get("Jamabar " + m.group(2));
                } else {
                    jamabar = new Jamabar();
                    configData.getFirstItemGroup().jamabarList.put("Jamabar " + m.group(2), jamabar);
                }

                if (Objects.equals(m.group(3), "pos")) { //Position
                    if (Objects.equals(m.group(4), "x")) {
                        jamabar.pos.x = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        jamabar.pos.y = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        jamabar.pos.z = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "rot")) { //Rotation
                    if (Objects.equals(m.group(4), "x")) {
                        jamabar.rot.x = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        jamabar.rot.y = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        jamabar.rot.z = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "scl")) { //Scale
                    if (Objects.equals(m.group(4), "x")) {
                        jamabar.scl.x = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        jamabar.scl.y = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        jamabar.scl.z = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else {
                    LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(3), line));
                }

            } else if (Objects.equals(splitLine[0], "banana")) { //Banana
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                //Get banana object
                Banana banana;
                if (configData.getFirstItemGroup().bananaList.containsKey("Banana " + m.group(2))) {
                    //ID already exists
                    banana = configData.getFirstItemGroup().bananaList.get("Banana " + m.group(2));
                } else {
                    banana = new Banana();
                    configData.getFirstItemGroup().bananaList.put("Banana " + m.group(2), banana);
                }

                if (Objects.equals(m.group(3), "pos")) { //Position
                    if (Objects.equals(m.group(4), "x")) {
                        banana.pos.x = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "y")) {
                        banana.pos.y = Float.parseFloat(m.group(5));
                    } else if (Objects.equals(m.group(4), "z")) {
                        banana.pos.z = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else if (Objects.equals(m.group(3), "type")) { //Type
                    if (Objects.equals(m.group(5), "N")) { //Single banana
                        banana.type = Banana.EnumBananaType.SINGLE;
                    } else if (Objects.equals(m.group(5), "B")) { //Banana bunch
                        banana.type = Banana.EnumBananaType.BUNCH;
                    } else {
                        LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else {
                    LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(3), line));
                }

            } else if (Objects.equals(splitLine[0], "fallout")) { //Fallout
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                if (Objects.equals(m.group(3), "pos")) { //Position
                    if (Objects.equals(m.group(4), "y")) {
                        configData.falloutPlane = Float.parseFloat(m.group(5));
                    } else {
                        LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(4), line));
                    }

                } else {
                    LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", m.group(3), line));
                }

            } else if (Objects.equals(splitLine[0], "background")) { //Background
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                configData.backgroundList.add(m.group(5));

            } else if (Objects.equals(splitLine[0], "maxtime")) { //Max time
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                configData.maxTime = Float.parseFloat(m.group(5));

            } else if (Objects.equals(splitLine[0], "leadintime")) { //Lead in time
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                configData.leadInTime = Float.parseFloat(m.group(5));

            } else if (Objects.equals(splitLine[0], "animobj")) {
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invalid config pattern \"%s\"", line));
                }

                //Get ConfigAnimData object
                ItemGroup itemGroup = configData.getItemGroup(m.group(2));
                ConfigAnimData ad = itemGroup.animData;

                if (ad == null) {
                    ad = new ConfigAnimData();
                    itemGroup.animData = ad;
                }

                if (Objects.equals(m.group(3), "file")) {
                    ad.parseAnimConfig(new File(configFile.getParentFile(), m.group(5)), configData.maxTime + configData.leadInTime);
                } else if (Objects.equals(m.group(3), "name")) {
                    itemGroup.addObject(m.group(5));
                    configData.animatedObjects.add(m.group(5));
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
                LogHelper.warn(configData.getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", splitLine[0], line));
            }
        }

        br.close();
        isr.close();
        fis.close();

    }

}
