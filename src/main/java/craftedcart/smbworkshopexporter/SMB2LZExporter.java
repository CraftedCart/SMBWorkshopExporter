package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.util.EnumLZExportTask;
import craftedcart.smbworkshopexporter.util.LogHelper;
import craftedcart.smbworkshopexporter.util.TaskDoneAction;
import craftedcart.smbworkshopexporter.util.Vec3f;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

/**
 * @author CraftedCart
 *         Created on 01/10/2016 (DD/MM/YYYY)
 */
public class SMB2LZExporter extends AbstractLzExporter {

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

        int noBgModels = 0;

        cfgBytesToWrite =
                        20 * configData.goalList.size() +
                        32 * configData.bumperList.size() +
                        32 * configData.jamabarList.size() +
                        16 * configData.bananaList.size();

        for (int i = 0; i < modelData.cmnObjs.size(); i++) {
            Obj obj = modelData.cmnObjs.get(i);

            for (int j = 0; j < obj.tris.size(); j++) {
                colBytesToWrite += 64;
            }
        }

        lzBytesToWrite = 4946 + cfgBytesToWrite + colBytesToWrite +
                (512 * Math.floorDiv(colBytesToWrite, 64)) +
                ((modelData.cmnObjNames.size() - noBgModels) * 12) +
                ((modelData.cmnObjNames.size() - noBgModels) * 4) +
                ((modelData.cmnObjNames.size() - noBgModels) * 7) +
                ((modelData.cmnObjNames.size() - noBgModels) * 5) +
                ((modelData.cmnObjNames.size() - noBgModels) * 4);
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
        sectOffsets[0] = 0x8B4;

        //Write goals
        for (Map.Entry<String, Goal> entry : configData.goalList.entrySet()) {
            Goal goal = entry.getValue();

            int type = 'B' << 8;

            if (goal.type == 0) {
                type = 1;
            } else if (goal.type == 1) {
                type = 0x0101;
            } else if (goal.type == 2) {
                type = 0x0201;
            }

            //Write position
            cfgWriteFloat(rafConfig, goal.posX);
            cfgWriteFloat(rafConfig, goal.posY);
            cfgWriteFloat(rafConfig, goal.posZ);

            //Write rotation
            cfgWriteShort(rafConfig, (cnvAngle(goal.rotX)));
            cfgWriteShort(rafConfig, (cnvAngle(goal.rotY)));
            cfgWriteShort(rafConfig, (cnvAngle(goal.rotZ)));

            //Write type
            cfgWriteShort(rafConfig, type);
        }

        //Bumpers
        sectOffsets[1] = (int) (rafConfig.getFilePointer() + 0x8B4);
        for (Map.Entry<String, Bumper> entry : configData.bumperList.entrySet()) {
            Bumper bumper = entry.getValue();

            //Write position
            cfgWriteFloat(rafConfig, bumper.posX);
            cfgWriteFloat(rafConfig, bumper.posY);
            cfgWriteFloat(rafConfig, bumper.posZ);

            //Write rotation
            cfgWriteShort(rafConfig, (cnvAngle(bumper.rotX)));
            cfgWriteShort(rafConfig, (cnvAngle(bumper.rotY)));
            cfgWriteShort(rafConfig, (cnvAngle(bumper.rotZ)));

            cfgWrite(rafConfig, 0);
            cfgWrite(rafConfig, 0);

            //Write scale
            cfgWriteFloat(rafConfig, bumper.sclX);
            cfgWriteFloat(rafConfig, bumper.sclY);
            cfgWriteFloat(rafConfig, bumper.sclZ);
        }

        //Jamabars
        sectOffsets[2] = (int) (rafConfig.getFilePointer() + 0x8B4);
        for (Map.Entry<String, Jamabar> entry : configData.jamabarList.entrySet()) {
            Jamabar jamabar = entry.getValue();
            //Write position
            cfgWriteFloat(rafConfig, jamabar.posX);
            cfgWriteFloat(rafConfig, jamabar.posY);
            cfgWriteFloat(rafConfig, jamabar.posZ);

            //Write rotation
            cfgWriteShort(rafConfig, (cnvAngle(jamabar.rotX)));
            cfgWriteShort(rafConfig, (cnvAngle(jamabar.rotY)));
            cfgWriteShort(rafConfig, (cnvAngle(jamabar.rotZ)));

            cfgWrite(rafConfig, 0);
            cfgWrite(rafConfig, 0);

            //Write scale
            cfgWriteFloat(rafConfig, jamabar.sclX);
            cfgWriteFloat(rafConfig, jamabar.sclY);
            cfgWriteFloat(rafConfig, jamabar.sclZ);
        }

        //Bananas
        sectOffsets[3] = (int) (rafConfig.getFilePointer() + 0x8B4);
        for (Map.Entry<String, Banana> entry : configData.bananaList.entrySet()) {
            Banana banana = entry.getValue();
            //Write position
            cfgWriteFloat(rafConfig, banana.posX);
            cfgWriteFloat(rafConfig, banana.posY);
            cfgWriteFloat(rafConfig, banana.posZ);
            //Write type
            cfgWriteInt(rafConfig, banana.type);
        }

        rafConfig.close();

        if (SMBWorkshopExporter.verboseLogging) {
            LogHelper.trace(SMB2LZExporter.class, "Done exporting tempcfg.lz.raw.part");
        }

        setCurrentTask(EnumLZExportTask.EXPORT_COLLISION);

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
//                if (a.y < configData.falloutPlane) configData.falloutPlane = a.y;
//                if (b.y < configData.falloutPlane) configData.falloutPlane = b.y;
//                if (c.y < configData.falloutPlane) configData.falloutPlane = c.y;
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
            LogHelper.trace(SMB2LZExporter.class, "Done exporting tempcol.lz.raw.part");
        }

        setCurrentTask(EnumLZExportTask.EXPORT_LZ);

        //Write complete (uncompressed) file
        RandomAccessFile rafReadCfg = new RandomAccessFile(tempCfgFile, "r");
        RandomAccessFile rafReadCol = new RandomAccessFile(tempColFile, "r");
        RandomAccessFile rafOutRaw = new RandomAccessFile(outFile, "rw");

        int cfgSize = (int) rafReadCfg.length();
        int colSize = (int) rafReadCol.length();
        int realColSize = colSize + 0x49C + (0x200 * (colSize / 0x40)) + 0x600;

        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 'D');
        lzWrite(rafOutRaw, 'z');
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 1);
        lzWriteInt(rafOutRaw, cfgSize + 0x8B4);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 8);
        lzWrite(rafOutRaw, 0x9C);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 8);
        lzWrite(rafOutRaw, 0xB0);

        //Write goals
        int goalCount = configData.goalList.size();
        if (goalCount > 0) {
            lzWriteInt(rafOutRaw, goalCount);
            lzWriteInt(rafOutRaw, sectOffsets[0]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                lzWrite(rafOutRaw, 0);
            }
        }

        //Write bumpers
        int bumperCount = configData.bumperList.size();
        if (bumperCount > 0) {
            lzWriteInt(rafOutRaw, bumperCount);
            lzWriteInt(rafOutRaw, sectOffsets[1]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                lzWrite(rafOutRaw, 0);
            }
        }

        //Write jamabars
        int jamabarCount = configData.jamabarList.size();
        if (jamabarCount > 0) {
            lzWriteInt(rafOutRaw, jamabarCount);
            lzWriteInt(rafOutRaw, sectOffsets[2]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                lzWrite(rafOutRaw, 0);
            }
        }

        //Write bananas
        int bananaCount = configData.bananaList.size();
        if (bananaCount > 0) {
            lzWriteInt(rafOutRaw, bananaCount);
            lzWriteInt(rafOutRaw, sectOffsets[3]);
        } else {
            for (int i = 0; i < 8; i++) { //Write 8x 0
                lzWrite(rafOutRaw, 0);
            }
        }

        for (int i = 0; i < 32; i++) { //Write 32x 0
            lzWrite(rafOutRaw, 0);
        }

        lzWriteInt(rafOutRaw, noBgModels);

        int tallyObjNames = modelData.cmnObjNames.size();
        lzWriteInt(rafOutRaw, ((tallyObjNames - noBgModels) * 112) + realColSize + cfgSize + 0x8B4);

        for (int i = 0; i < 15; i++) { //Write 15x 0
            lzWrite(rafOutRaw, 0);
        }
        lzWrite(rafOutRaw, 1);
        for (int i = 0; i < 28; i++) { //Write 28x 0
            lzWrite(rafOutRaw, 0);
        }

        lzWriteInt(rafOutRaw, tallyObjNames - noBgModels);
        lzWriteInt(rafOutRaw, ((tallyObjNames - noBgModels) * 16) + realColSize + cfgSize + 0x8B4);
        lzWriteInt(rafOutRaw, tallyObjNames - noBgModels);
        lzWriteInt(rafOutRaw, ((tallyObjNames - noBgModels) * 28) + realColSize + cfgSize + 0x8B4);

        for (int i = 0; i < 2048; i++) { //Write 2048x 0
            lzWrite(rafOutRaw, 0);
        }

        //Write start pos
        Start start = configData.startList.entrySet().iterator().next().getValue();
        lzWriteFloat(rafOutRaw, start.posX);
        lzWriteFloat(rafOutRaw, start.posY);
        lzWriteFloat(rafOutRaw, start.posZ);

        lzWriteShort(rafOutRaw, (cnvAngle(start.rotX)));
        lzWriteShort(rafOutRaw, (cnvAngle(start.rotY)));
        lzWriteShort(rafOutRaw, (cnvAngle(start.rotZ)));

        lzWrite(rafOutRaw, 0);
        lzWrite(rafOutRaw, 0);

        //Write fallout pos
        lzWriteFloat(rafOutRaw, configData.falloutPlane);

        //Write tempcfg.lz.raw.part into output.lz.raw
        for (int i = 0; i < cfgSize; i++) {
            lzWrite(rafOutRaw, rafReadCfg.read());
        }

        int whereAreWe = (int) rafOutRaw.getFilePointer();

        for (int i = 0; i < 36; i++) { //Write 36x 0
            lzWrite(rafOutRaw, 0);
        }

        lzWriteInt(rafOutRaw, whereAreWe + 0x49C);
        lzWriteInt(rafOutRaw, whereAreWe + 0x69C + colSize + (0x200 * (colSize / 0x40)));
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

        for (int i = 0; i < 48; i++) { //Write 48x 0
            lzWrite(rafOutRaw, 0);
        }

        lzWriteInt(rafOutRaw, tallyObjNames - noBgModels);
        lzWriteInt(rafOutRaw, ((tallyObjNames - noBgModels) * 28) + realColSize + cfgSize + 0x8B4);

        for (int i = 0; i < 1024; i++) { //Write 1024x 0
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

        for (int i = 0; i < tallyObjNames - noBgModels; i++) {
            for (int j = 0; j < 12; j++) { //Write 12x 0
                lzWrite(rafOutRaw, 0);
            }
            lzWriteInt(rafOutRaw, whereAreWe + ((tallyObjNames - noBgModels) * 32) + (80 * i));
        }

        for (int i = 0; i < tallyObjNames - noBgModels; i++) {
            for (int j = 0; j < 7; j++) { //Write 7x 0
                lzWrite(rafOutRaw, 0);
            }
            lzWrite(rafOutRaw, 1);
            lzWriteInt(rafOutRaw, whereAreWe + 8 + (16 * i));
        }

        for (int i = 0; i < tallyObjNames - noBgModels; i++) {
            lzWriteInt(rafOutRaw, whereAreWe + ((tallyObjNames - noBgModels) * 16) + (12 * i));
        }

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

//        whereAreWe = (int) rafOutRaw.getFilePointer();
//
//        for (int i = 0; i < noBgModels; i++) {
//            lzWrite(rafOutRaw, 0);
//            lzWrite(rafOutRaw, 0);
//            lzWrite(rafOutRaw, 0);
//            lzWrite(rafOutRaw, 0x1F);
//            lzWrite(rafOutRaw, ((whereAreWe + (noBgModels * 0x38) + (i * 80)) & 0xFF000000) >> 24);
//            lzWrite(rafOutRaw, ((whereAreWe + (noBgModels * 0x38) + (i * 80)) & 0xFF0000) >> 16);
//            lzWrite(rafOutRaw, ((whereAreWe + (noBgModels * 0x38) + (i * 80)) & 0xFF00) >> 8);
//            lzWrite(rafOutRaw, (whereAreWe + (noBgModels * 0x38) + (i * 80)) & 0xFF);
//            for (int j = 0; j < 24; j++) { //Write 24x 0
//                lzWrite(rafOutRaw, 0);
//            }
//            lzWrite(rafOutRaw, 0x3F);
//            lzWrite(rafOutRaw, 0x80);
//            lzWrite(rafOutRaw, 0);
//            lzWrite(rafOutRaw, 0);
//            lzWrite(rafOutRaw, 0x3F);
//            lzWrite(rafOutRaw, 0x80);
//            lzWrite(rafOutRaw, 0);
//            lzWrite(rafOutRaw, 0);
//            lzWrite(rafOutRaw, 0x3F);
//            lzWrite(rafOutRaw, 0x80);
//            lzWrite(rafOutRaw, 0);
//            lzWrite(rafOutRaw, 0);
//            for (int j = 0; j < 12; j++) { //Write 12x 0
//                lzWrite(rafOutRaw, 0);
//            }
//        }
//
//        for (int i = 0; i < tallyObjNames; i++) {
//            char[] chars = modelData.cmnObjNames.get(i).toCharArray();
//            for (char c : chars) {
//                lzWrite(rafOutRaw, c);
//            }
//        }

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
            LogHelper.trace(SMB2LZExporter.class, "Done exporting output.lz.raw");
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
