package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.util.LogHelper;
import craftedcart.smbworkshopexporter.util.Vec2f;
import craftedcart.smbworkshopexporter.util.Vec3f;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author CraftedCart
 *         Created on 20/09/2016 (DD/MM/YYYY)
 */
public class ModelData {

    List<Vec3f> cmnVerticies = new ArrayList<>();
    List<Vec2f> cmnTexCoords = new ArrayList<>();
    List<Vec3f> cmnNormals = new ArrayList<>();
    List<Obj> cmnObjs = new ArrayList<>();
    List<String> cmnObjNames = new ArrayList<>();

    public void parseObj(File objFile) throws IOException, NumberFormatException {

        FileInputStream fis = new FileInputStream(objFile);
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
                tri.va = Integer.valueOf(splitPos1[0]);
                if (Objects.equals(splitPos1[1], "")) {
                    tri.vta = 0;
                } else {
                    tri.vta = Integer.valueOf(splitPos1[1]);
                }
                tri.vna = Integer.valueOf(splitPos1[2]);

                String[] splitPos2 = splitLine[2].split("/");
                tri.vb = Integer.valueOf(splitPos2[0]);
                if (Objects.equals(splitPos2[1], "")) {
                    tri.vtb = 0;
                } else {
                    tri.vtb = Integer.valueOf(splitPos2[1]);
                }
                tri.vnb = Integer.valueOf(splitPos2[2]);

                String[] splitPos3 = splitLine[3].split("/");
                tri.vc = Integer.valueOf(splitPos3[0]);
                if (Objects.equals(splitPos3[1], "")) {
                    tri.vtc = 0;
                } else {
                    tri.vtc = Integer.valueOf(splitPos3[1]);
                }
                tri.vnc = Integer.valueOf(splitPos3[2]);

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

    }

}

class Triangle {
    int va;
    int vb;
    int vc;
    int vta;
    int vtb;
    int vtc;
    int vna;
    int vnb;
    int vnc;
}

class Obj {
    String mat;
    List<Triangle> tris = new ArrayList<>();
}
