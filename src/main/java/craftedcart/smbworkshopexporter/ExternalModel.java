package craftedcart.smbworkshopexporter;

import java.io.File;

/**
 * @author CraftedCart
 *         Created on 20/03/2017 (DD/MM/YYYY)
 */
public class ExternalModel {

    public File file;
    public ModelType type;

    public ExternalModel(File file, ModelType type) {
        this.file = file;
        this.type = type;
    }

}
