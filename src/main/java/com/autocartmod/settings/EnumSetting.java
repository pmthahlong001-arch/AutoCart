package com.autocartmod.settings;

public class EnumSetting<E extends Enum<E>> extends Setting<E> {
    private final E[] values;

    @SuppressWarnings("unchecked")
    public EnumSetting(String name, String description, E defaultValue) {
        super(name, description, defaultValue);
        this.values = (E[]) defaultValue.getClass().getEnumConstants();
    }

    public void cycle() {
        int idx = value.ordinal();
        this.value = values[(idx + 1) % values.length];
    }

    public E[] getValues() { return values; }

    @Override
    public SettingType getType() {
        return SettingType.ENUM;
    }
}
