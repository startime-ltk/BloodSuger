package com.bloodsugar.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * AI 建议模块的配置管理。
 * 配置保存在用户主目录的 ~/.bloodsugar/config.properties，和数据库同目录；
 * 支持智谱 GLM 与 DeepSeek 两个模型各存一套 baseUrl / model / apiKey，
 * 界面上可一键切换当前使用的模型，Key 不写死在代码里。
 */
public class AiConfig {

    public static final String PROVIDER_GLM = "glm";
    public static final String PROVIDER_DEEPSEEK = "deepseek";

    private static final Path CONFIG_FILE = Path.of(
            System.getProperty("user.home"), ".bloodsugar", "config.properties");

    private final Properties props = new Properties();

    private AiConfig() {
    }

    /** 读取配置；文件不存在或读失败时用默认模板，首次使用不会强制写盘 */
    public static AiConfig load() {
        AiConfig cfg = new AiConfig();
        try {
            if (Files.exists(CONFIG_FILE)) {
                try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
                    cfg.props.load(in);
                }
            }
        } catch (IOException ignored) {
            // 读失败就用默认值，不阻塞启动
        }
        cfg.props.putIfAbsent("ai.activeProvider", PROVIDER_GLM);
        cfg.props.putIfAbsent("ai.glm.baseUrl", "https://open.bigmodel.cn/api/paas/v4/chat/completions");
        cfg.props.putIfAbsent("ai.glm.model", "glm-4.6-flash");
        cfg.props.putIfAbsent("ai.glm.apiKey", "");
        cfg.props.putIfAbsent("ai.deepseek.baseUrl", "https://api.deepseek.com/v1/chat/completions");
        cfg.props.putIfAbsent("ai.deepseek.model", "deepseek-v4-flash");
        cfg.props.putIfAbsent("ai.deepseek.apiKey", "");
        return cfg;
    }

    /** 保存到本地配置文件；保存失败抛 RuntimeException 由调用方提示用户 */
    public void save() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            try (OutputStream out = Files.newOutputStream(CONFIG_FILE)) {
                props.store(out, "Blood Sugar AI config. Keys are stored locally only.");
            }
        } catch (IOException e) {
            throw new RuntimeException("无法保存 AI 配置: " + e.getMessage(), e);
        }
    }

    public static Path getConfigFile() {
        return CONFIG_FILE;
    }

    /** 模型显示名，用于 UI 下拉框 */
    public static String displayName(String provider) {
        return PROVIDER_GLM.equals(provider) ? "智谱 GLM-4.6-Flash" : "DeepSeek-V4-Flash";
    }

    public String getActiveProvider() {
        return PROVIDER_DEEPSEEK.equals(props.getProperty("ai.activeProvider")) ? PROVIDER_DEEPSEEK : PROVIDER_GLM;
    }

    public void setActiveProvider(String provider) {
        props.setProperty("ai.activeProvider", provider);
    }

    public String getBaseUrl(String provider) {
        return props.getProperty("ai." + provider + ".baseUrl", "");
    }

    public void setBaseUrl(String provider, String value) {
        props.setProperty("ai." + provider + ".baseUrl", value);
    }

    public String getModel(String provider) {
        return props.getProperty("ai." + provider + ".model", "");
    }

    public void setModel(String provider, String value) {
        props.setProperty("ai." + provider + ".model", value);
    }

    public String getApiKey(String provider) {
        return props.getProperty("ai." + provider + ".apiKey", "");
    }

    public void setApiKey(String provider, String value) {
        props.setProperty("ai." + provider + ".apiKey", value);
    }
}
