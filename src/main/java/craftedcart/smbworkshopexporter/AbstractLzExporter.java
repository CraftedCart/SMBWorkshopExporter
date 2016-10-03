package craftedcart.smbworkshopexporter;

import craftedcart.smbworkshopexporter.util.EnumLZExportTask;
import craftedcart.smbworkshopexporter.util.TaskDoneAction;

import java.io.File;
import java.io.IOException;

/**
 * @author CraftedCart
 *         Created on 03/10/2016 (DD/MM/YYYY)
 */
public abstract class AbstractLzExporter {

    private TaskDoneAction taskDoneAction;

    public EnumLZExportTask currentTask = EnumLZExportTask.EXPORT_CONFIG;
    public int cfgBytesToWrite = 0;
    public int cfgBytesWritten = 0;
    public int colBytesToWrite = 0;
    public int colBytesWritten = 0;
    public int lzBytesToWrite = 0;
    public int lzBytesWritten = 0;

    public abstract void writeRawLZ(ModelData modelData, ConfigData configData, File outFile) throws IOException;

    public void setCurrentTask(EnumLZExportTask currentTask) {
        if (taskDoneAction != null) {
            taskDoneAction.execute(this.currentTask);
        }

        this.currentTask = currentTask;
    }

    public void setTaskDoneAction(TaskDoneAction taskDoneAction) {
        this.taskDoneAction = taskDoneAction;
    }

}
