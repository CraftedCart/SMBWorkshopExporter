package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.util.LogHelper;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CraftedCart
 *         Created on 20/09/2016 (DD/MM/YYYY)
 */
public class ConfigData {

    Map<String, Start> startList = new HashMap<>();
    Map<String, Goal> goalList = new HashMap<>();
    Map<String, Bumper> bumperList = new HashMap<>();
    Map<String, Jamabar> jamabarList = new HashMap<>();
    Map<String, Banana> bananaList = new HashMap<>();

    float falloutPlane = 0.0f;

    public void parseConfig(File configFile) throws IOException, IllegalStateException, NumberFormatException {
        
        FileInputStream fis = new FileInputStream(configFile);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        Pattern p = Pattern.compile("(.*) \\[ (\\d) \\] \\. (.*) \\. (.*) = (.*)");

        String line;

        while ((line = br.readLine()) != null) {

            String[] splitLine = line.split("\\s+");

            if (Objects.equals(splitLine[0], "start")) { //Start
                Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException(String.format("Invald config pattern at line \"%s\"", line));
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

            } else if (!Objects.equals(line, "")) { //If the line is not empty
                LogHelper.warn(getClass(), String.format("Invalid attribute \"%s\" - In line \"%s\"", splitLine[0], line));
            }
        }

        br.close();
        isr.close();
        fis.close();

    }
}

class Start {
    float posX;
    float posY;
    float posZ;

    float rotX;
    float rotY;
    float rotZ;
}

class Goal {
    float posX;
    float posY;
    float posZ;

    float rotX;
    float rotY;
    float rotZ;

    int type = 0;
}

class Bumper {
    float posX;
    float posY;
    float posZ;

    float rotX;
    float rotY;
    float rotZ;

    float sclX;
    float sclY;
    float sclZ;
}

class Jamabar {
    float posX;
    float posY;
    float posZ;

    float rotX;
    float rotY;
    float rotZ;

    float sclX;
    float sclY;
    float sclZ;
}

class Banana {
    float posX;
    float posY;
    float posZ;

    int type;
}