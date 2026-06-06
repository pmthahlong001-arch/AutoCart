package com.autocartmod.settings;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    public void toggle() {
        this.value = !this.value;
    }

    @Override
    public SettingType getType() {
        return SettingType.BOOLEAN;
    }
}
