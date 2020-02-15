package base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author kikyou
 */
@Slf4j
public class IOUtil {


    public static Config getSettings(String filePath) {
        try {
            return JSON.parseObject(readFromConfigFile(filePath), new TypeReference<Config>() {
            });
        } catch (IOException e) {
            log.error("Exception occurred when read configuration from config file from path: " + filePath, e);
        }
        log.warn("Read Settings Failed");
        return null;
    }

    public static String readFromConfigFile(String filePath) throws IOException {
        String config = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        log.info("base.Config:" + config);
        return config;
    }

    public static void updateConfigFile(String key, Object value) {
        //temp
        Path filePath;
        if (AbstractProxy.CLIENT_MODE)
            filePath = Paths.get(Config.CLIENT_CONFIG_FILE_PATH);
        else
            filePath = Paths.get(Config.SERVER_CONFIG_FILE_PATH);
        try {
            String s = Files.readString(filePath,StandardCharsets.UTF_8);
            s = s.replaceFirst("\"key\":[0-9]*", "\"" + key + "\"" + ":" + String.valueOf(value));
            Files.write(filePath, s.getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            log.error("Update configuration fle failed", e);
        }
    }

    public static String getStringFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            result.append(line);
            line = br.readLine();
        }

        return result.toString();
    }
}
