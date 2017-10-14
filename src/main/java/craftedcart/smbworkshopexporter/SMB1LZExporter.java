package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.placeables.*;
import craftedcart.smbworkshopexporter.util.EnumLZExportTask;
import craftedcart.smbworkshopexporter.util.LogHelper;
import craftedcart.smbworkshopexporter.util.Vec3f;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

/**
 * @author CraftedCart
 *         Created on 21/09/2016 (DD/MM/YYYY)
 */
public class SMB1LZExporter extends AbstractLzExporter {

    public void writeRawLZ(ModelData modelData, ConfigData configData, File outFile) throws IOException {

        File tempCfgFile = File.createTempFile("tempcfg", ".lz.raw.part");
        File tempColFile = File.createTempFile("tempcol", ".lz.raw.part");

        boolean madeDirs = outFile.getParentFile().mkdirs();
        if (!madeDirs && !outFile.getParentFile().exists()) {
            throw new IOException("Failed to make directories");
        }

        if (outFile.exists()) { //Delete the file and recreate it if it exists
            if (!outFile.delete()) {
                LogHelper.warn(getClass(), "Failed to delete original raw LZ file: " + outFile.getAbsolutePath());
                LogHelper.warn(getClass(), "Output raw LZ file may be corrupt");
            }
        }

        cfgBytesToWrite =
                        20 * configData.getFirstItemGroup().goalList.size() +
                        32 * configData.getFirstItemGroup().bumperList.size() +
                        32 * configData.getFirstItemGroup().jamabarList.size() +
                        16 * configData.getFirstItemGroup().bananaList.size();

        for (int i = 0; i < modelData.cmnObjs.size(); i++) {
            Obj obj = modelData.cmnObjs.get(i);

            for (int j = 0; j < obj.tris.size(); j++) {
                colBytesToWrite += 64;
            }
        }

        lzBytesToWrite = 2000 + cfgBytesToWrite + colBytesToWrite + (512 * Math.floorDiv(colBytesToWrite, 64)) + (modelData.cmnObjNames.size() * 12);
        for (String name : modelData.cmnObjNames) {
            lzBytesToWrite += 80;
        }
        if (lzBytesToWrite % 8 == 4) {
            lzBytesToWrite += 4;
        }
        while (lzBytesToWrite % 4 != 0) {
            lzBytesToWrite++;
        }
        
        //Write config
        RandomAccessFile rafConfig = new RandomAccessFile(tempCfgFile, "rw");

        int[] sectOffsets = new int[4];
        sectOffsets[0] = 256;

        //Write goals
        for (Map.Entry<String, Goal> entry : configData.getFirstItemGroup().goalList.entrySet()) {
            Goal goal = entry.getValue();

            int type = 'B' << 8;

            if (goal.type == Goal.EnumGoalType.BLUE) {
                type = 'B' << 8;
            } else if (goal.type == Goal.EnumGoalType.GREEN) {
                type = 'G' << 8;
            } else if (goal.type == Goal.EnumGoalType.RED) {
                type = 'R' << 8;
            }

            //Write position
            cfgWriteFloat(rafConfig, goal.pos.x);
            cfgWriteFloat(rafConfig, goal.pos.y);
            cfgWriteFloat(rafConfig, goal.pos.z);

            //Write rotation
            cfgWriteShort(rafConfig, (cnvAngle(goal.rot.x)));
            cfgWriteShort(rafConfig, (cnvAngle(goal.rot.y)));
            cfgWriteShort(rafConfig, (cnvAngle(goal.rot.z)));

            //Write type
            cfgWriteShort(rafConfig, type);
        }
        sectOffsets[1] = (int) (rafConfig.getFilePointer() + 256);

        //Bumpers
        for (Map.Entry<String, Bumper> entry : configData.getFirstItemGroup().bumperList.entrySet()) {
            Bumper bumper = entry.getValue();

            //Write position
            cfgWriteFloat(rafConfig, bumper.pos.x);
            cfgWriteFloat(rafConfig, bumper.pos.y);
            cfgWriteFloat(rafConfig, bumper.pos.z);

            //Write rotation
            cfgWriteShort(rafConfig, (cnvAngle(bumper.rot.x)));
            cfgWriteShort(rafConfig, (cnvAngle(bumper.rot.z)));
            cfgWriteShort(rafConfig, (cnvAngle(bumper.rot.z)));

            cfgWrite(rafConfig, 0);
            cfgWrite(rafConfig, 0);

            //Write scale
            cfgWriteFloat(rafConfig, bumper.scl.x);
            cfgWriteFloat(rafConfig, bumper.scl.y);
            cfgWriteFloat(rafConfig, bumper.scl.z);
        }
        sectOffsets[2] = (int) (rafConfig.getFilePointer() + 256);

        //Jamabars
        for (Map.Entry<String, Jamabar> entry : configData.getFirstItemGroup().jamabarList.entrySet()) {
            Jamabar jamabar = entry.getValue();

            //Write position
            cfgWriteFloat(rafConfig, jamabar.pos.x);
            cfgWriteFloat(rafConfig, jamabar.pos.y);
            cfgWriteFloat(rafConfig, jamabar.pos.z);

            //Write rotation
            cfgWriteShort(rafConfig, (cnvAngle(jamabar.rot.x)));
            cfgWriteShort(rafConfig, (cnvAngle(jamabar.rot.y)));
            cfgWriteShort(rafConfig, (cnvAngle(jamabar.rot.z)));

            cfgWrite(rafConfig, 0);
            cfgWrite(rafConfig, 0);

            //Write scale
            cfgWriteFloat(rafConfig, jamabar.scl.x);
            cfgWriteFloat(rafConfig, jamabar.scl.y);
            cfgWriteFloat(rafConfig, jamabar.scl.z);
        }
        sectOffsets[3] = (int) (rafConfig.getFilePointer() + 256);

        //Bananas
        for (Map.Entry<String, Banana> entry : configData.getFirstItemGroup().bananaList.entrySet()) {
            Banana banana = entry.getValue();

            //Write position
            cfgWriteFloat(rafConfig, banana.pos.x);
            cfgWriteFloat(rafConfig, banana.pos.y);
            cfgWriteFloat(rafConfig, banana.pos.z);

            //Write type
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

            cfgWriteInt(rafConfig, bananaType);
        }

        rafConfig.close();

        if (SMBWorkshopExporter.verboseLogging) {
            LogHelper.trace(SMB1LZExporter.class, "Done exporting tempcfg.lz.raw.part");
        }

        setCurrentTask(EnumLZExportTask.EXPORT_COLLISION);

        //Write collision triangles
        RandomAccessFile rafCol = new RandomAccessFile(tempColFile, "rw");

        for (int i = 0; i < modelData.cmnObjs.size(); i++) {
            Obj obj = modelData.cmnObjs.get(i);

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
                Vec3f normal = normalize(cross(normalize(ba),normalize(ca)));
                float l = (float) Math.sqrt(normal.x * normal.x + normal.z * normal.z);
                float cy = normal.z / l;
                float sy = -normal.x / l;
                if (Math.abs(l)<0.001) {
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
                float rot_x = 360.0f - reverseAngle(cx, sx);
                float rot_y = 360.0f - reverseAngle(cy, sy);
                float rot_z = 360.0f - reverseAngle(cz, sz);

                colWriteFloat(rafCol, a.x);
                colWriteFloat(rafCol, a.y);
                colWriteFloat(rafCol, a.z);

                colWriteFloat(rafCol, normal.x);
                colWriteFloat(rafCol, normal.y);
                colWriteFloat(rafCol, normal.z);
                colWriteShort(rafCol, (cnvAngle(rot_x)));
                colWriteShort(rafCol, (cnvAngle(rot_y)));
                colWriteShort(rafCol, (cnvAngle(rot_z)));
                colWrite(rafCol, 0);
                colWrite(rafCol, 0);
                colWriteFloat(rafCol, dotrz.x);
                colWriteFloat(rafCol, dotrz.y);
                colWriteFloat(rafCol, dotrzrxry.x);
                colWriteFloat(rafCol, dotrzrxry.y);
                colWriteFloat(rafCol, n0.x);
                colWriteFloat(rafCol, n0.y);
                colWriteFloat(rafCol, n1.x);
                colWriteFloat(rafCol, n1.y);
            }

        }

        rafCol.close();

        if (SMBWorkshopExporter.verboseLogging) {
            LogHelper.trace(SMB1LZExporter.class, "Done exporting tempcol.lz.raw.part");
        }

        setCurrentTask(EnumLZExportTask.EXPORT_LZ);

        //Write complete (uncompressed) file
        RandomAccessFile rafReadCfg = new RandomAccessFile(tempCfgFile, "r");
        RandomAccessFile rafReadCol = new RandomAccessFile(tempColFile, "r");
        RandomAccessFile rafOutRaw = new RandomAccessFile(outFile, "rw");

        int cfgSize = (int) rafReadCfg.length();
        int colSize = (int) rafReadCol.length();
        int realColSize = colSize + 0xB8 + (0x200 * (colSize / 0x40)) + 0x600;

        for (int i = 0; i < 7; i++) { //Write 7x 0
            lzWrite(rafOutRaw, 0);
        }
        lzWrite(rafOutRaw, 100);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 1);
        lzWriteInt(rafOutRaw, cfgSize + 256);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 160);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 180);

        //Write goals
        int goalCount = configData.getFirstItemGroup().goalList.size();
        if (goalCount > 0) {
            lzWriteInt(rafOutRaw, goalCount);
            lzWriteInt(rafOutRaw, sectOffsets[0]);
            lzWriteInt(rafOutRaw, goalCount);
        } else {
            for (int i = 0; i < 12; i++) { //Write 12x 0
                lzWrite(rafOutRaw, 0);
            }
        }

        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);

        //Write bumpers
        int bumperCount = configData.getFirstItemGroup().bumperList.size();
        if (bumperCount > 0) {
            lzWriteInt(rafOutRaw, bumperCount);
            lzWriteInt(rafOutRaw, sectOffsets[1]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                lzWrite(rafOutRaw, 0);
            }
        }

        //Write jamabars
        int jamabarCount = configData.getFirstItemGroup().jamabarList.size();
        if (jamabarCount > 0) {
            lzWriteInt(rafOutRaw, jamabarCount);
            lzWriteInt(rafOutRaw, sectOffsets[2]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                lzWrite(rafOutRaw, 0);
            }
        }

        //Write bananas
        int bananaCount = configData.getFirstItemGroup().bananaList.size();
        if (bananaCount > 0) {
            lzWriteInt(rafOutRaw, bananaCount);
            lzWriteInt(rafOutRaw, sectOffsets[3]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                lzWrite(rafOutRaw, 0);
            }
        }

        for (int i = 0; i < 24; i++) { //Write 24x 0
            lzWrite(rafOutRaw, 0);
        }

        int tallyObjNames = modelData.cmnObjNames.size();
        lzWriteInt(rafOutRaw, tallyObjNames + 1);
        lzWriteInt(rafOutRaw, realColSize + cfgSize + 256);

        for (int i = 0; i < 31; i++) { //Write 31x 0
            lzWrite(rafOutRaw, 0);
        }
        lzWrite(rafOutRaw, 1);
        for (int i = 0; i < 32; i++) { //Write 32x 0
            lzWrite(rafOutRaw, 0);
        }
        //Write start pos
        Start start = configData.startList.entrySet().iterator().next().getValue();
        lzWriteFloat(rafOutRaw, start.pos.x);
        lzWriteFloat(rafOutRaw, start.pos.y);
        lzWriteFloat(rafOutRaw, start.pos.z);

        lzWriteShort(rafOutRaw, (cnvAngle(start.rot.x)));
        lzWriteShort(rafOutRaw, (cnvAngle(start.rot.y)));
        lzWriteShort(rafOutRaw, (cnvAngle(start.rot.z)));

        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);

        //Write fallout pos
        lzWriteFloat(rafOutRaw, configData.falloutPlane);

        lzWriteInt(rafOutRaw, ((12 + ((tallyObjNames + 1) * 12) + realColSize + cfgSize + 256)));
        lzWriteInt(rafOutRaw, ((4 + ((tallyObjNames + 1) * 12) + realColSize + cfgSize + 256)));

        for (int i = 0; i < 64; i++) { //Write 64x 0
            lzWrite(rafOutRaw, 0);
        }

        //Write tempcfg.lz.raw.part into output.lz.raw
        for (int i = 0; i < cfgSize; i++) {
            lzWrite(rafOutRaw, rafReadCfg.read());
        }

        int whereAreWe = (int) rafOutRaw.getFilePointer();

        for (int i = 0; i < 27; i++) { //Write 27x 0
            lzWrite(rafOutRaw, 0);
        }

        lzWrite(rafOutRaw, 0xB8);

        lzWriteInt(rafOutRaw, whereAreWe + 0xB8);
        lzWriteInt(rafOutRaw, whereAreWe + 0x2B8 + colSize + (0x200 * colSize / 0x40));
        lzWrite(rafOutRaw, 0xC3);
        lzWrite(rafOutRaw, 0x80);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0xC3);
        lzWrite(rafOutRaw, 0x80);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0x42);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0x42);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 16);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 16);

        //Write goals (again)
        if (goalCount > 0) {
            lzWriteInt(rafOutRaw, goalCount);
            lzWriteInt(rafOutRaw, sectOffsets[0]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                lzWrite(rafOutRaw, 0);
            }
        }

        for (int i = 0; i < 8; i++) { //Write 8x 0
            lzWrite(rafOutRaw, 0);
        }

        //Write bumpers (again)
        if (bumperCount > 0) {
            lzWriteInt(rafOutRaw, bumperCount);
            lzWriteInt(rafOutRaw, sectOffsets[1]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                lzWrite(rafOutRaw, 0);
            }
        }

        //Write jamabars (again)
        if (jamabarCount > 0) {
            lzWriteInt(rafOutRaw, jamabarCount);
            lzWriteInt(rafOutRaw, sectOffsets[2]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                lzWrite(rafOutRaw, 0);
            }
        }

        //Write bananas (again)
        if (bananaCount > 0) {
            lzWriteInt(rafOutRaw, bananaCount);
            lzWriteInt(rafOutRaw, sectOffsets[3]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                lzWrite(rafOutRaw, 0);
            }
        }

        for (int i = 0; i < 24; i++) { //Write 24x 0
            lzWrite(rafOutRaw, 0);
        }

        lzWriteInt(rafOutRaw, tallyObjNames + 1 /* - noBgModels */);
        lzWriteInt(rafOutRaw, realColSize + cfgSize + 256);

        for (int i = 0; i < 52; i++) { //Write 52x 0
            lzWrite(rafOutRaw, 0);
        }

        //Write tempcol.lz.raw.part into output.lz.raw
        for (int i = 0; i < colSize; i++) {
            lzWrite(rafOutRaw, rafReadCol.read());
        }

        whereAreWe = (int) rafOutRaw.getFilePointer();

        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < (colSize / 64); j++) {
                lzWriteShort(rafOutRaw, j);
            }
            lzWrite(rafOutRaw, 0xFF);
            lzWrite(rafOutRaw, 0xFF);
        }

        for (int i = 0; i < 256; i++) {
            lzWriteInt(rafOutRaw, whereAreWe + i * 2 + (i * 2 * (colSize / 0x40)));
        }

        whereAreWe = (int) rafOutRaw.getFilePointer();

        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 1);
        lzWriteInt(rafOutRaw, whereAreWe + 28);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);

        whereAreWe = (int) rafOutRaw.getFilePointer();

        for (int i = 0; i < tallyObjNames; i++) {
            lzWrite(rafOutRaw, 0);
            lzWrite(rafOutRaw, 0);
            lzWrite(rafOutRaw, 0);
            lzWrite(rafOutRaw, 1);
            lzWriteInt(rafOutRaw, whereAreWe + 24 + (92 * i));
            lzWrite(rafOutRaw, 0);
            lzWrite(rafOutRaw, 0);
            lzWrite(rafOutRaw, 0);
            lzWrite(rafOutRaw, 0);
        }

        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 'n');
        lzWrite(rafOutRaw, 'u');
        lzWrite(rafOutRaw, 'l');
        lzWrite(rafOutRaw, 'l');
        lzWrite(rafOutRaw, '2');
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);

        for (int i = 0; i < tallyObjNames; i++) {
            char[] chars = modelData.cmnObjNames.get(i).toCharArray();
            int j = 0;
            for (char c : chars) {
                lzWrite(rafOutRaw, c);
                j++;

                if (j == 80) {
                    break;
                }
            }

            while (j < 80) {
                lzWrite(rafOutRaw, 0);
                j++;
            }
        }

        if (rafOutRaw.getFilePointer() % 8 == 4) {
            lzWrite(rafOutRaw, 0);
            lzWrite(rafOutRaw, 0);
            lzWrite(rafOutRaw, 0);
            lzWrite(rafOutRaw, 0);
        }

        while (rafOutRaw.getFilePointer() % 4 != 0) {
            lzWrite(rafOutRaw, 0);
        }

        rafReadCol.close();
        rafReadCfg.close();
        rafOutRaw.close();

        if (!tempCfgFile.delete()) {
            LogHelper.warn(getClass(), "Failed to delete temporary file: " + tempCfgFile.getAbsolutePath());
        }
        if (!tempColFile.delete()) {
            LogHelper.warn(getClass(), "Failed to delete temporary file: " + tempColFile.getAbsolutePath());
        }

        if (SMBWorkshopExporter.verboseLogging) {
            LogHelper.trace(SMB1LZExporter.class, "Done exporting output.lz.raw");
        }

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
    
    private void cfgWrite(RandomAccessFile raf, int toWrite) throws IOException {
        raf.write(toWrite);
        cfgBytesWritten++;
    }

    private void cfgWriteFloat(RandomAccessFile raf, float toWrite) throws IOException {
        raf.writeFloat(toWrite);
        cfgBytesWritten += 4;
    }

    private void cfgWriteInt(RandomAccessFile raf, int toWrite) throws IOException {
        raf.writeInt(toWrite);
        cfgBytesWritten += 4;
    }

    private void cfgWriteShort(RandomAccessFile raf, int toWrite) throws IOException {
        raf.writeShort(toWrite);
        cfgBytesWritten += 2;
    }

    private void colWrite(RandomAccessFile raf, int toWrite) throws IOException {
        raf.write(toWrite);
        colBytesWritten++;
    }

    private void colWriteFloat(RandomAccessFile raf, float toWrite) throws IOException {
        raf.writeFloat(toWrite);
        colBytesWritten += 4;
    }

    private void colWriteInt(RandomAccessFile raf, int toWrite) throws IOException {
        raf.writeInt(toWrite);
        colBytesWritten += 4;
    }

    private void colWriteShort(RandomAccessFile raf, int toWrite) throws IOException {
        raf.writeShort(toWrite);
        colBytesWritten += 2;
    }

    private void lzWrite(RandomAccessFile raf, int toWrite) throws IOException {
        raf.write(toWrite);
        lzBytesWritten++;
    }

    private void lzWriteFloat(RandomAccessFile raf, float toWrite) throws IOException {
        raf.writeFloat(toWrite);
        lzBytesWritten += 4;
    }

    private void lzWriteInt(RandomAccessFile raf, int toWrite) throws IOException {
        raf.writeInt(toWrite);
        lzBytesWritten += 4;
    }

    private void lzWriteShort(RandomAccessFile raf, int toWrite) throws IOException {
        raf.writeShort(toWrite);
        lzBytesWritten += 2;
    }

}
