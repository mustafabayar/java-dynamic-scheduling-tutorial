package com.mbcoder.scheduler.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ConfigItem {

    @Id
    String  configKey;

    String  configValue;

    public ConfigItem() {
    }

    public ConfigItem(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
}
