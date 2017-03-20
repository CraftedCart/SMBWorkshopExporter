package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.util.LogHelper;
import craftedcart.smbworkshopexporter.util.Vec2f;
import craftedcart.smbworkshopexporter.util.Vec3f;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author CraftedCart
 *         Created on 20/09/2016 (DD/MM/YYYY)
 */
public class ModelData {

    public List<Vec3f> cmnVerticies = new ArrayList<>();
    public List<Vec2f> cmnTexCoords = new ArrayList<>();
    public List<Vec3f> cmnNormals = new ArrayList<>();
    public List<Obj> cmnObjs = new ArrayList<>();
    public List<String> cmnObjNames = new ArrayList<>();

    public void parseObj(Set<File> objFiles) throws IOException, NumberFormatException {

        int lastVertIndex = 0;
        int lastTexCoordIndex = 0;
        int lastVertNormIndex = 0;

        for (File file : objFiles) {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String line;

            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split("\\s+");

                if (Objects.equals(splitLine[0], "v")) { //Vertex Identifier
                    Vec3f vert = new Vec3f();

                    vert.x = Float.valueOf(splitLine[1]);
                    vert.y = Float.valueOf(splitLine[2]);
                    vert.z = Float.valueOf(splitLine[3]);

                    cmnVerticies.add(vert);

//                if (SMBWorkshopExporter.verboseLogging) {
//                    LogHelper.info(ModelData.class, "Done processing line: " + line);
//                }
                } else if (Objects.equals(splitLine[0], "vt")) { //TexCoord Identifier
                    Vec2f texCoord = new Vec2f();

                    texCoord.x = Float.valueOf(splitLine[1]);
                    texCoord.y = Float.valueOf(splitLine[2]);

                    cmnTexCoords.add(texCoord);

//                if (SMBWorkshopExporter.verboseLogging) {
//                    LogHelper.info(ModelData.class, "Done processing line: " + line);
//                }
                } else if (Objects.equals(splitLine[0], "vn")) { //Normal Identifier
                    Vec3f norm = new Vec3f();

                    norm.x = Float.valueOf(splitLine[1]);
                    norm.y = Float.valueOf(splitLine[2]);
                    norm.z = Float.valueOf(splitLine[3]);

                    cmnNormals.add(norm);

//                if (SMBWorkshopExporter.verboseLogging) {
//                    LogHelper.info(ModelData.class, "Done processing line: " + line);
//                }
                } else if (Objects.equals(splitLine[0], "f")) { //Face Identifier
                    Triangle tri = new Triangle();

                    String[] splitPos1 = splitLine[1].split("/");
                    tri.vertA = Integer.valueOf(splitPos1[0]) + lastVertIndex;
                    if (Objects.equals(splitPos1[1], "")) {
                        tri.vertATexCoord = 0;
                    } else {
                        tri.vertATexCoord = Integer.valueOf(splitPos1[1]) + lastTexCoordIndex;
                    }
                    tri.vertANorm = Integer.valueOf(splitPos1[2]) + lastVertNormIndex;

                    String[] splitPos2 = splitLine[2].split("/");
                    tri.vertB = Integer.valueOf(splitPos2[0]) + lastVertIndex;
                    if (Objects.equals(splitPos2[1], "")) {
                        tri.vertBTexCoord = 0;
                    } else {
                        tri.vertBTexCoord = Integer.valueOf(splitPos2[1]) + lastTexCoordIndex;
                    }
                    tri.vertBNorm = Integer.valueOf(splitPos2[2]) + lastVertNormIndex;

                    String[] splitPos3 = splitLine[3].split("/");
                    tri.vertC = Integer.valueOf(splitPos3[0]) + lastVertIndex;
                    if (Objects.equals(splitPos3[1], "")) {
                        tri.vertCTexCoord = 0;
                    } else {
                        tri.vertCTexCoord = Integer.valueOf(splitPos3[1]) + lastTexCoordIndex;
                    }
                    tri.vertCNorm = Integer.valueOf(splitPos3[2]) + lastVertNormIndex;

                    if (cmnObjs.size() > 0) {
                        cmnObjs.get(cmnObjs.size() - 1).tris.add(tri);
                    } else {
                        Obj obj = new Obj();
                        obj.tris.add(tri);
                        cmnObjs.add(obj);
                    }

//                if (SMBWorkshopExporter.verboseLogging) {
//                    LogHelper.info(ModelData.class, "Done processing line: " + line);
//                }
                } else if (Objects.equals(splitLine[0], "o")) { //Object Name Identifier

                    cmnObjNames.add(splitLine[1]);

                    Obj obj = new Obj();
                    cmnObjs.add(obj);

//                if (SMBWorkshopExporter.verboseLogging) {
//                    LogHelper.info(ModelData.class, "Done processing line: " + line);
//                }
                } else {
                    if (SMBWorkshopExporter.verboseLogging) {
                        LogHelper.trace(ModelData.class, "Skipping line: " + line);
                    }
                }
            }

            br.close();
            isr.close();
            fis.close();

            lastVertIndex = cmnVerticies.size();
            lastTexCoordIndex = cmnTexCoords.size();
            lastVertNormIndex = cmnNormals.size();
        }

    }

    public Obj getObjFromModelName(String objectName) {
        return cmnObjs.get(cmnObjNames.indexOf(objectName));
    }

}

class Triangle {
    /**
     * Triangle vertex A
     */
    int vertA;

    /**
     * Triangle vertex B
     */
    int vertB;

    /**
     * Triangle vertex C
     */
    int vertC;

    /**
     * Triangle vertex A Tex Coordinate
     */
    int vertATexCoord;

    /**
     * Triangle vertex B Tex Coordinate
     */
    int vertBTexCoord;

    /**
     * Triangle vertex C Tex Coordinate
     */
    int vertCTexCoord;

    /**
     * Triangle vertex A Normal
     */
    int vertANorm;

    /**
     * Triangle vertex B Normal
     */
    int vertBNorm;

    /**
     * Triangle vertex C Normal
     */
    int vertCNorm;
}

class Obj {
    List<Triangle> tris = new ArrayList<>();
}
