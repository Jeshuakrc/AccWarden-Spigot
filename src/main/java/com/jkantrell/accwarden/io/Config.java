package com.jkantrell.accwarden.io;

import com.jkantrell.yamlizer.yaml.AbstractYamlConfig;
import com.jkantrell.yamlizer.yaml.ConfigField;

public class Config extends AbstractYamlConfig {
    public Config(String filePath) {
        super(filePath);
    }

    @ConfigField
    public boolean crossPlatformSessions = false;

    @ConfigField
    public int sessionHoldTime = 300;
}
