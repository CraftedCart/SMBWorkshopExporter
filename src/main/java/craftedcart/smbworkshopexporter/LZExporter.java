package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.util.LogHelper;
import craftedcart.smbworkshopexporter.util.Vec3f;

import java.io.*;
import java.util.Map;

/**
 * @author CraftedCart
 *         Created on 21/09/2016 (DD/MM/YYYY)
 */
public class LZExporter {

    public static void writeLZ(ModelData modelData, ConfigData configData, File outDir) throws IOException {

        File tempCfgFile = new File(outDir, "tempcfg.lz.raw.part");
        File tempColFile = new File(outDir, "tempcol.lz.raw.part");
        File outputRawFile = new File(outDir, "output.lz.raw");

        boolean madeDirs = outDir.mkdirs();
        if (!madeDirs && !outDir.exists()) {
            throw new IOException("Failed to make directories");
        }

        //Write config
        RandomAccessFile rafConfig = new RandomAccessFile(tempCfgFile, "rw");

        int[] sectOffsets = new int[4];
        sectOffsets[0] = 256;

        //Write goals
        for (Map.Entry<String, Goal> entry : configData.goalList.entrySet()) {
            Goal goal = entry.getValue();

            int type = 'B' << 8;

            if (goal.type == 0) {
                type = 'B' << 8;
            } else if (goal.type == 1) {
                type = 'G' << 8;
            } else if (goal.type == 2) {
                type = 'R' << 8;
            }

            //Write position
            rafConfig.writeFloat(goal.posX);
            rafConfig.writeFloat(goal.posY);
            rafConfig.writeFloat(goal.posZ);

            //Write rotation
            rafConfig.writeShort(cnvAngle(goal.rotX));
            rafConfig.writeShort(cnvAngle(goal.rotY));
            rafConfig.writeShort(cnvAngle(goal.rotZ));

            //Write type
            rafConfig.writeShort(type);
        }
        sectOffsets[1] = (int) (rafConfig.getFilePointer() + 256);

        //Bumpers
        for (Map.Entry<String, Bumper> entry : configData.bumperList.entrySet()) {
            Bumper bumper = entry.getValue();

            //Write position
            rafConfig.writeFloat(bumper.posX);
            rafConfig.writeFloat(bumper.posY);
            rafConfig.writeFloat(bumper.posZ);

            //Write rotation
            rafConfig.writeShort(cnvAngle(bumper.rotX));
            rafConfig.writeShort(cnvAngle(bumper.rotY));
            rafConfig.writeShort(cnvAngle(bumper.rotZ));

            rafConfig.write(0);
            rafConfig.write(0);

            //Write scale
            rafConfig.writeFloat(bumper.sclX);
            rafConfig.writeFloat(bumper.sclY);
            rafConfig.writeFloat(bumper.sclZ);
        }
        sectOffsets[2] = (int) (rafConfig.getFilePointer() + 256);

        //Jamabars
        for (Map.Entry<String, Jamabar> entry : configData.jamabarList.entrySet()) {
            Jamabar jamabar = entry.getValue();

            //Write position
            rafConfig.writeFloat(jamabar.posX);
            rafConfig.writeFloat(jamabar.posY);
            rafConfig.writeFloat(jamabar.posZ);

            //Write rotation
            rafConfig.writeShort(cnvAngle(jamabar.rotX));
            rafConfig.writeShort(cnvAngle(jamabar.rotY));
            rafConfig.writeShort(cnvAngle(jamabar.rotZ));

            rafConfig.write(0);
            rafConfig.write(0);

            //Write scale
            rafConfig.writeFloat(jamabar.sclX);
            rafConfig.writeFloat(jamabar.sclY);
            rafConfig.writeFloat(jamabar.sclZ);
        }
        sectOffsets[3] = (int) (rafConfig.getFilePointer() + 256);

        //Bananas
        for (Map.Entry<String, Banana> entry : configData.bananaList.entrySet()) {
            Banana banana = entry.getValue();

            //Write position
            rafConfig.writeFloat(banana.posX);
            rafConfig.writeFloat(banana.posY);
            rafConfig.writeFloat(banana.posZ);

            //Write type
            rafConfig.writeInt(banana.type);
        }

        rafConfig.close();

        if (SMBWorkshopExporter.verboseLogging) {
            LogHelper.trace(LZExporter.class, "Done exporting tempcfg.lz.raw.part");
        }

        //Write collision triangles
        RandomAccessFile rafCol = new RandomAccessFile(tempColFile, "rw");

        for (int i = 0; i < modelData.cmnObjs.size(); i++) {
            Obj obj = modelData.cmnObjs.get(i);

            for (int j = 0; j < obj.tris.size(); j++) {
                Triangle tri = obj.tris.get(j);

//                Vec3f na = new Vec3f(modelData.cmnVerticies.get(tri.vna - 1).x, modelData.cmnVerticies.get(tri.vna - 1).y, modelData.cmnVerticies.get(tri.vna - 1).z);
                Vec3f a = new Vec3f(modelData.cmnVerticies.get(tri.va - 1).x, modelData.cmnVerticies.get(tri.va - 1).y, modelData.cmnVerticies.get(tri.va - 1).z);
                Vec3f b = new Vec3f(modelData.cmnVerticies.get(tri.vb - 1).x, modelData.cmnVerticies.get(tri.vb - 1).y, modelData.cmnVerticies.get(tri.vb - 1).z);
                Vec3f c = new Vec3f(modelData.cmnVerticies.get(tri.vc - 1).x, modelData.cmnVerticies.get(tri.vc - 1).y, modelData.cmnVerticies.get(tri.vc - 1).z);
                if (a.y<configData.falloutPlane) configData.falloutPlane = a.y;
                if (b.y<configData.falloutPlane) configData.falloutPlane = b.y;
                if (c.y<configData.falloutPlane) configData.falloutPlane = c.y;
                Vec3f ba = new Vec3f(b.x-a.x, b.y-a.y, b.z-a.z);
                Vec3f ca = new Vec3f(c.x-a.x, c.y-a.y, c.z-a.z);
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

                rafCol.writeFloat(a.x);
                rafCol.writeFloat(a.y);
                rafCol.writeFloat(a.z);

                rafCol.writeFloat(normal.x);
                rafCol.writeFloat(normal.y);
                rafCol.writeFloat(normal.z);
                rafCol.writeShort(cnvAngle(rot_x));
                rafCol.writeShort(cnvAngle(rot_y));
                rafCol.writeShort(cnvAngle(rot_z));
                rafCol.write(0);
                rafCol.write(0);
                rafCol.writeFloat(dotrz.x);
                rafCol.writeFloat(dotrz.y);
                rafCol.writeFloat(dotrzrxry.x);
                rafCol.writeFloat(dotrzrxry.y);
                rafCol.writeFloat(n0.x);
                rafCol.writeFloat(n0.y);
                rafCol.writeFloat(n1.x);
                rafCol.writeFloat(n1.y);
            }

        }

        rafCol.close();

        if (SMBWorkshopExporter.verboseLogging) {
            LogHelper.trace(LZExporter.class, "Done exporting tempcol.lz.raw.part");
        }

        //Write complete (uncompressed) file
        RandomAccessFile rafReadCfg = new RandomAccessFile(tempCfgFile, "r");
        RandomAccessFile rafReadCol = new RandomAccessFile(tempColFile, "r");
        RandomAccessFile rafOutRaw = new RandomAccessFile(outputRawFile, "rw");

        int cfgSize = (int) rafReadCfg.length();
        int colSize = (int) rafReadCol.length();
        int realColSize = colSize + 0xB8 + (0x200 * (colSize / 0x40)) + 0x600;

        for (int i = 0; i < 7; i++) { //Write 7x 0
            rafOutRaw.write(0);
        }
        rafOutRaw.write(100);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(1);
        rafOutRaw.writeInt(cfgSize + 256);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(160);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(180);

        //Write goals
        int goalCount = configData.goalList.size();
        if (goalCount > 0) {
            rafOutRaw.writeInt(goalCount);
            rafOutRaw.writeInt(sectOffsets[0]);
            rafOutRaw.writeInt(goalCount);
        } else {
            for (int i = 0; i < 12; i++) { //Write 12x 0
                rafOutRaw.write(0);
            }
        }

        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);

        //Write bumpers
        int bumperCount = configData.bumperList.size();
        if (bumperCount > 0) {
            rafOutRaw.writeInt(bumperCount);
            rafOutRaw.writeInt(sectOffsets[1]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                rafOutRaw.write(0);
            }
        }

        //Write jamabars
        int jamabarCount = configData.jamabarList.size();
        if (jamabarCount > 0) {
            rafOutRaw.writeInt(jamabarCount);
            rafOutRaw.writeInt(sectOffsets[2]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                rafOutRaw.write(0);
            }
        }

        //Write bananas
        int bananaCount = configData.bananaList.size();
        if (bananaCount > 0) {
            rafOutRaw.writeInt(bananaCount);
            rafOutRaw.writeInt(sectOffsets[3]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                rafOutRaw.write(0);
            }
        }

        for (int i = 0; i < 24; i++) { //Write 24x 0
            rafOutRaw.write(0);
        }

        int tallyObjNames = modelData.cmnObjNames.size();
        rafOutRaw.writeInt(tallyObjNames + 1);
        rafOutRaw.writeInt(realColSize + cfgSize + 256); //Diff

        for (int i = 0; i < 31; i++) { //Write 31x 0
            rafOutRaw.write(0);
        }
        rafOutRaw.write(1);
        for (int i = 0; i < 32; i++) { //Write 32x 0
            rafOutRaw.write(0);
        }
        //Write start pos
        Start start = configData.startList.entrySet().iterator().next().getValue();
        rafOutRaw.writeFloat(start.posX);
        rafOutRaw.writeFloat(start.posY);
        rafOutRaw.writeFloat(start.posZ);

        rafOutRaw.writeShort(cnvAngle(start.rotX));
        rafOutRaw.writeShort(cnvAngle(start.rotY));
        rafOutRaw.writeShort(cnvAngle(start.rotZ));

        rafOutRaw.write(0);
        rafOutRaw.write(0);

        //Write fallout pos
        rafOutRaw.writeFloat(configData.falloutPlane);

        rafOutRaw.write(((12 + ((tallyObjNames + 1) * 12) + realColSize + cfgSize + 256) >> 24) & 0xFF);
        rafOutRaw.write(((12 + ((tallyObjNames + 1) * 12) + realColSize + cfgSize + 256) >> 16) & 0xFF);
        rafOutRaw.write(((12 + ((tallyObjNames + 1) * 12) + realColSize + cfgSize + 256) >> 8) & 0xFF);
        rafOutRaw.write((12 + ((tallyObjNames + 1) * 12) + realColSize + cfgSize + 256) & 0xFF);
        rafOutRaw.write(((4 + ((tallyObjNames + 1) * 12) + realColSize + cfgSize + 256) >> 24) & 0xFF);
        rafOutRaw.write(((4 + ((tallyObjNames + 1) * 12) + realColSize + cfgSize + 256) >> 16) & 0xFF);
        rafOutRaw.write(((4 + ((tallyObjNames + 1) * 12) + realColSize + cfgSize + 256) >> 8) & 0xFF);
        rafOutRaw.write((4 + ((tallyObjNames + 1) * 12) + realColSize + cfgSize + 256) & 0xFF);

        for (int i = 0; i < 64; i++) { //Write 64x 0
            rafOutRaw.write(0);
        }

        //Write tempcfg.lz.raw.part into output.lz.raw
        for (int i = 0; i < cfgSize; i++) {
            rafOutRaw.write(rafReadCfg.read());
        }

        int whereAreWe = (int) rafOutRaw.getFilePointer();

        for (int i = 0; i < 27; i++) { //Write 27x 0
            rafOutRaw.write(0);
        }

        rafOutRaw.write(0xB8);

        rafOutRaw.writeInt(whereAreWe + 0xB8);
        rafOutRaw.writeInt(whereAreWe + 0x2B8 + colSize + (0x200 * colSize / 0x40));
        rafOutRaw.write(0xC3);
        rafOutRaw.write(0x80);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0xC3);
        rafOutRaw.write(0x80);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0x42);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0x42);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(16);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(16);

        //Write goals (again)
        if (goalCount > 0) {
            rafOutRaw.writeInt(goalCount);
            rafOutRaw.writeInt(sectOffsets[0]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                rafOutRaw.write(0);
            }
        }

        for (int i = 0; i < 8; i++) { //Write 8x 0
            rafOutRaw.write(0);
        }

        //Write bumpers (again)
        if (bumperCount > 0) {
            rafOutRaw.writeInt(bumperCount);
            rafOutRaw.writeInt(sectOffsets[1]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                rafOutRaw.write(0);
            }
        }

        //Write jamabars (again)
        if (jamabarCount > 0) {
            rafOutRaw.writeInt(jamabarCount);
            rafOutRaw.writeInt(sectOffsets[2]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                rafOutRaw.write(0);
            }
        }

        //Write bananas (again)
        if (bananaCount > 0) {
            rafOutRaw.writeInt(bananaCount);
            rafOutRaw.writeInt(sectOffsets[3]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                rafOutRaw.write(0);
            }
        }

        for (int i = 0; i < 24; i++) { //Write 24x 0
            rafOutRaw.write(0);
        }

        rafOutRaw.writeInt(tallyObjNames + 1 /* - noBgModels */);
        rafOutRaw.writeInt(realColSize + cfgSize + 256);

        for (int i = 0; i < 52; i++) { //Write 52x 0
            rafOutRaw.write(0);
        }

        //Write tempcol.lz.raw.part into output.lz.raw
        for (int i = 0; i < colSize; i++) {
            rafOutRaw.write(rafReadCol.read());
        }

        whereAreWe = (int) rafOutRaw.getFilePointer();

        for (int i = 0; i < 256; i++) {
            for (int j=0; j<(colSize / 64); j++)
            {
                rafOutRaw.write((j >> 8) & 0xFF);
                rafOutRaw.write(j & 0xFF);
            }
            rafOutRaw.write(0xFF);
            rafOutRaw.write(0xFF);
        }

        for (int i = 0; i < 256; i++) {
            rafOutRaw.writeInt(whereAreWe + i * 2 + (i * 2 * (colSize / 0x40)));
        }

        whereAreWe = (int) rafOutRaw.getFilePointer();

        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(1);
        rafOutRaw.writeInt(whereAreWe + 28);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);

        whereAreWe = (int) rafOutRaw.getFilePointer();

        for (int i = 0; i < tallyObjNames; i++) {
            rafOutRaw.write(0);
            rafOutRaw.write(0);
            rafOutRaw.write(0);
            rafOutRaw.write(1);
            rafOutRaw.writeInt(whereAreWe + 24 + (92 * i));
            rafOutRaw.write(0);
            rafOutRaw.write(0);
            rafOutRaw.write(0);
            rafOutRaw.write(0);
        }

        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write('n');
        rafOutRaw.write('u');
        rafOutRaw.write('l');
        rafOutRaw.write('l');
        rafOutRaw.write('2');
        rafOutRaw.write(0);
        rafOutRaw.write(0);
        rafOutRaw.write(0);

        for (int i = 0; i < tallyObjNames; i++) {
            char[] chars = modelData.cmnObjNames.get(i).toCharArray();
            for (char c : chars) {
                rafOutRaw.write(c);
            }
        }

        if (rafOutRaw.getFilePointer() % 8 == 4) {
            rafOutRaw.write(0);
            rafOutRaw.write(0);
            rafOutRaw.write(0);
            rafOutRaw.write(0);
        }

        while (rafOutRaw.getFilePointer() % 4 != 0) {
            rafOutRaw.write(0);
        }

        rafReadCol.close();
        rafReadCfg.close();
        rafOutRaw.close();

        if (SMBWorkshopExporter.verboseLogging) {
            LogHelper.trace(LZExporter.class, "Done exporting output.lz.raw");
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
            if(s < 0.0) {
                a =- a;
            }
        }
        if(a < 0.0) {
            if(a >- 0.001) {
                a = 0.0f;
            }
            else a += 360.0;
        }
        return a;
    }

}
