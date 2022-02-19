package dssp.hashidate.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Properties;

import dssp.brailleLib.Util;

public class TempConfig {

    private static String TEMP_CONFIG = ".temp_config";
    private static DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public static String checkLastPath(String path) {
        if (Objects.isNull(path)) {
            Properties prop = openProperty();
            String lpath = prop.getProperty("lastPath");
            if (Objects.isNull(lpath)) {
                return path;
            }
            return lpath;
        }
        return path;

    }

    public static void storePath(String path, File tfile) {
        if (Objects.isNull(tfile)) {
            return;
        }
        String parent = tfile.getParent();
        if (Objects.isNull(parent)) {
            return;
        }
        if (parent.equals(path)) {
            return;
        }
        Properties prop = openProperty();
        prop.setProperty("lastPath", parent);
        saveProperty(prop);
    }

    private static void saveProperty(Properties prop) {
        File cfile = Paths.get(System.getProperty("user.dir"), TEMP_CONFIG).toFile();
        try (FileWriter writer = new FileWriter(cfile.getPath())) {
            prop.store(writer, LocalDateTime.now().format(FORMAT));
        } catch (IOException e) {
            Util.logException(e);
        }
    }

    private static Properties openProperty() {
        Properties prop = new Properties();
        File cfile = Paths.get(System.getProperty("user.dir"), TEMP_CONFIG).toFile();
        if (cfile.exists()) {
            try (Reader reader = new FileReader(cfile.getPath())) {
                prop.load(reader);
            } catch (IOException e) {
                Util.logException(e);
            }
        }
        return prop;
    }

}
