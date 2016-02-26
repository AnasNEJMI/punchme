package android.feetme.fr.punchme.dao;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by Anas on 26/02/2016.
 */
public class DaoGenerator {

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(17, "android.feetme.fr.punchme.dao");
        schema.enableKeepSectionsByDefault();

        Entity glove = addGlove(schema);

        String workingDir = System.getProperty("user.dir");
        new de.greenrobot.daogenerator.DaoGenerator().generateAll(schema, workingDir + "/app/src/main/java");
    }

    private static Entity addGlove(Schema schema) {
        Entity glove = schema.addEntity("Glove");
        glove.addIdProperty();
        glove.addStringProperty("name").notNull();
        glove.addStringProperty("address").notNull().unique();
        glove.addIntProperty("side");
        glove.addIntProperty("sensorNb");
        glove.addStringProperty("serialNumber");

        return glove;
    }


}
