package com.aliyun.appserver.sample.upload;// src/main/java/ConfigReader.java
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;

public class ConfigReader {

    private static Map<String, Object> config;

    static {
        Yaml yaml = new Yaml();
        try (InputStream in = ConfigReader.class.getClassLoader()
                .getResourceAsStream("application.yml")) {
            if (in == null) {
                throw new RuntimeException("❌ 配置文件 application.yml 未找到！");
            }
            config = yaml.loadAs(in, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("❌ 读取配置文件失败", e);
        }
    }

    /**
     * 根据 key 获取配置值，支持嵌套如 "vod.ak"
     */
    public static String get(String key) {
        String[] parts = key.split("\\.");
        Map<String, Object> current = config;
        for (int i = 0; i < parts.length - 1; i++) {
            Object obj = current.get(parts[i]);
            if (obj instanceof Map) {
                current = (Map<String, Object>) obj;
            } else {
                return null;
            }
        }
        return (String) current.get(parts[parts.length - 1]);
    }
}
