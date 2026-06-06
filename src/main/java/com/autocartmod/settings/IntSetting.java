package com.autocartmod.settings;

public class IntSetting extends Setting<Integer> {
    private final int min;
    private final int max;

    public IntSetting(String name, String description, int defaultValue, int min, int max) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
    }

    public int getMin() { return min; }
    public int getMax() { return max; }

    @Override
    public void setValue(Integer value) {
        this.value = Math.max(min, Math.min(max, value));
    }

    @Override
    public SettingType getType() {
        return SettingType.INTEGER;
    }
}
