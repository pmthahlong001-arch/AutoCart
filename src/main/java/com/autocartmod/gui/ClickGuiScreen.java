package com.autocartmod.gui;

import com.autocartmod.client.AutoCartMod;
import com.autocartmod.modules.Module;
import com.autocartmod.settings.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.*;

public class ClickGuiScreen extends Screen {

    // ── Colors matching the dark blue theme in the screenshot ──
    private static final int COL_BG          = 0xE5060A12;  // panel background
    private static final int COL_PANEL_BORDER = 0xFF1A3A6A;  // blue border
    private static final int COL_HEADER_BG   = 0xFF0D1F3C;  // header row bg
    private static final int COL_ENABLED_BG  = 0xFF2255AA;  // enabled module highlight
    private static final int COL_ENABLED_HOV = 0xFF2E6BD4;  // hover on enabled
    private static final int COL_DISABLED_HOV= 0xFF162040;  // hover on disabled
    private static final int COL_TEXT        = 0xFFE0EAFF;
    private static final int COL_TEXT_DIM    = 0xFF7A9AD0;
    private static final int COL_SLIDER_BG   = 0xFF112244;
    private static final int COL_SLIDER_FILL = 0xFF2B6ECC;
    private static final int COL_SLIDER_KNOB = 0xFF5599FF;
    private static final int COL_CHECK_ON    = 0xFF2B6ECC;
    private static final int COL_CHECK_BORDER= 0xFF4477AA;
    private static final int COL_SETTINGS_BG = 0xFF080F1E;

    private static final int PANEL_WIDTH  = 160;
    private static final int ROW_H        = 18;
    private static final int HEADER_H     = 22;
    private static final int PADDING      = 6;
    private static final int SETTING_INDENT = 8;

    private final List<CategoryPanel> panels = new ArrayList<>();

    // Drag state
    private CategoryPanel draggingPanel = null;
    private int dragOffX, dragOffY;

    // Slider drag
    private SliderWidget activeSlider = null;

    public ClickGuiScreen() {
        super(Text.literal("AutoCart ClickGUI"));
    }

    @Override
    protected void init() {
        panels.clear();
        Module.Category[] cats = AutoCartMod.moduleManager.getUsedCategories();
        int startX = 30;
        for (Module.Category cat : cats) {
            List<Module> mods = AutoCartMod.moduleManager.getModulesByCategory(cat);
            CategoryPanel panel = new CategoryPanel(cat, mods, startX, 30);
            panels.add(panel);
            startX += PANEL_WIDTH + 10;
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Dim background
        ctx.fill(0, 0, this.width, this.height, 0x88000000);

        for (CategoryPanel panel : panels) {
            panel.render(ctx, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Slider drag check first
        for (CategoryPanel panel : panels) {
            for (SettingRow row : panel.settingRows) {
                if (row.slider != null) {
                    if (row.slider.isOver((int)mx, (int)my)) {
                        activeSlider = row.slider;
                        activeSlider.onMouseDown((int)mx);
                        return true;
                    }
                }
            }
        }

        for (CategoryPanel panel : panels) {
            // Header click -> drag OR collapse
            if (panel.isOverHeader((int)mx, (int)my)) {
                if (button == 0) {
                    draggingPanel = panel;
                    dragOffX = (int)mx - panel.x;
                    dragOffY = (int)my - panel.y;
                }
                return true;
            }
            // Module row click
            for (ModuleRow row : panel.moduleRows) {
                if (row.isOver((int)mx, (int)my)) {
                    if (button == 0) {
                        row.module.toggle();
                        panel.rebuild();
                    }
                    return true;
                }
            }
            // Setting widgets
            for (SettingRow row : panel.settingRows) {
                if (row.checkBox != null && row.checkBox.isOver((int)mx, (int)my)) {
                    ((BooleanSetting) row.setting).toggle();
                    return true;
                }
                if (row.enumBox != null && row.enumBox.isOver((int)mx, (int)my)) {
                    ((EnumSetting<?>) row.setting).cycle();
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingPanel != null) {
            draggingPanel.x = (int)mx - dragOffX;
            draggingPanel.y = (int)my - dragOffY;
            draggingPanel.rebuildPositions();
            return true;
        }
        if (activeSlider != null) {
            activeSlider.onDrag((int)mx);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingPanel = null;
        activeSlider = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    // ───────────────────────────────────── Inner classes ─────────────────────────────────────

    class CategoryPanel {
        Module.Category category;
        List<Module> modules;
        int x, y;
        final List<ModuleRow> moduleRows = new ArrayList<>();
        final List<SettingRow> settingRows = new ArrayList<>();

        CategoryPanel(Module.Category category, List<Module> modules, int x, int y) {
            this.category = category;
            this.modules = modules;
            this.x = x;
            this.y = y;
            rebuild();
        }

        void rebuild() {
            moduleRows.clear();
            settingRows.clear();
            rebuildPositions();
        }

        void rebuildPositions() {
            moduleRows.clear();
            settingRows.clear();
            int cy = y + HEADER_H;
            for (Module mod : modules) {
                ModuleRow row = new ModuleRow(mod, x, cy, PANEL_WIDTH);
                moduleRows.add(row);
                cy += ROW_H;
                if (mod.isEnabled()) {
                    for (Setting<?> s : mod.getSettings()) {
                        SettingRow sr = new SettingRow(s, x + SETTING_INDENT, cy, PANEL_WIDTH - SETTING_INDENT);
                        settingRows.add(sr);
                        cy += sr.height;
                    }
                }
            }
        }

        boolean isOverHeader(int mx, int my) {
            return mx >= x && mx <= x + PANEL_WIDTH && my >= y && my <= y + HEADER_H;
        }

        int totalHeight() {
            if (settingRows.isEmpty() && moduleRows.isEmpty()) return HEADER_H;
            int last = moduleRows.isEmpty() ? y + HEADER_H :
                    moduleRows.get(moduleRows.size() - 1).y + ROW_H;
            if (!settingRows.isEmpty()) {
                SettingRow sr = settingRows.get(settingRows.size() - 1);
                last = sr.y + sr.height;
            }
            return last - y;
        }

        void render(DrawContext ctx, int mx, int my) {
            int h = totalHeight();

            // Panel background
            fillRounded(ctx, x, y, PANEL_WIDTH, h, COL_BG);

            // Border
            drawBorder(ctx, x, y, PANEL_WIDTH, h, COL_PANEL_BORDER);

            // Header
            ctx.fill(x + 1, y + 1, x + PANEL_WIDTH - 1, y + HEADER_H - 1, COL_HEADER_BG);
            String title = category.displayName;
            int tw = textRenderer.getWidth(title);
            ctx.drawText(textRenderer, Text.literal(title), x + (PANEL_WIDTH - tw) / 2, y + 7, COL_TEXT, false);

            // Module rows
            for (ModuleRow row : moduleRows) {
                row.render(ctx, mx, my);
            }
            // Setting rows
            for (SettingRow row : settingRows) {
                row.render(ctx, mx, my);
            }
        }
    }

    class ModuleRow {
        Module module;
        int x, y, w;

        ModuleRow(Module module, int x, int y, int w) {
            this.module = module;
            this.x = x;
            this.y = y;
            this.w = w;
        }

        boolean isOver(int mx, int my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + ROW_H;
        }

        void render(DrawContext ctx, int mx, int my) {
            boolean hover = isOver(mx, my);
            boolean enabled = module.isEnabled();

            int bg = enabled ? (hover ? COL_ENABLED_HOV : COL_ENABLED_BG)
                             : (hover ? COL_DISABLED_HOV : 0x00000000);
            if (bg != 0) ctx.fill(x + 1, y, x + w - 1, y + ROW_H, bg);

            // Left blue indicator bar when enabled
            if (enabled) {
                ctx.fill(x + 1, y, x + 3, y + ROW_H, 0xFF5599FF);
            }

            ctx.drawText(textRenderer, Text.literal(module.getName()), x + 8, y + 5, COL_TEXT, false);

            String info = module.getDisplayInfo();
            if (info != null) {
                int iw = textRenderer.getWidth(info);
                ctx.drawText(textRenderer, Text.literal(info), x + w - iw - 5, y + 5, COL_TEXT_DIM, false);
            }
        }
    }

    class SettingRow {
        Setting<?> setting;
        int x, y, w;
        int height = ROW_H;
        CheckBox checkBox;
        EnumBox enumBox;
        SliderWidget slider;

        @SuppressWarnings("unchecked")
        SettingRow(Setting<?> setting, int x, int y, int w) {
            this.setting = setting;
            this.x = x;
            this.y = y;
            this.w = w;

            switch (setting.getType()) {
                case BOOLEAN -> {
                    checkBox = new CheckBox((BooleanSetting) setting, x + w - 14, y + (ROW_H - 10) / 2);
                }
                case INTEGER -> {
                    slider = new SliderWidget((IntSetting) setting, x, y + ROW_H, w);
                    height = ROW_H + 14; // label row + slider row
                }
                case ENUM -> {
                    enumBox = new EnumBox((EnumSetting<?>) setting, x + w / 2, y, w / 2);
                }
            }
        }

        void render(DrawContext ctx, int mx, int my) {
            // Setting name
            ctx.drawText(textRenderer, Text.literal(setting.getName()), x + 2, y + 5, COL_TEXT_DIM, false);

            if (checkBox != null) checkBox.render(ctx);
            if (enumBox != null) enumBox.render(ctx);
            if (slider != null) {
                slider.render(ctx, mx);
            }
        }
    }

    class CheckBox {
        BooleanSetting setting;
        int x, y;
        static final int SIZE = 10;

        CheckBox(BooleanSetting setting, int x, int y) {
            this.setting = setting;
            this.x = x;
            this.y = y;
        }

        boolean isOver(int mx, int my) {
            return mx >= x && mx <= x + SIZE && my >= y && my <= y + SIZE;
        }

        void render(DrawContext ctx) {
            ctx.fill(x, y, x + SIZE, y + SIZE, COL_CHECK_BORDER);
            ctx.fill(x + 1, y + 1, x + SIZE - 1, y + SIZE - 1,
                    setting.getValue() ? COL_CHECK_ON : COL_SETTINGS_BG);
            if (setting.getValue()) {
                // Draw checkmark as small filled square
                ctx.fill(x + 2, y + 2, x + SIZE - 2, y + SIZE - 2, COL_TEXT);
            }
        }
    }

    class EnumBox {
        EnumSetting<?> setting;
        int x, y, w;

        EnumBox(EnumSetting<?> setting, int x, int y, int w) {
            this.setting = setting;
            this.x = x;
            this.y = y;
            this.w = w;
        }

        boolean isOver(int mx, int my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + ROW_H;
        }

        void render(DrawContext ctx) {
            ctx.fill(x, y + 2, x + w, y + ROW_H - 2, COL_SLIDER_BG);
            drawBorder(ctx, x, y + 2, w, ROW_H - 4, COL_PANEL_BORDER);
            String val = setting.getValue().name();
            int tw = textRenderer.getWidth(val);
            // Truncate if needed
            ctx.drawText(textRenderer, Text.literal(val), x + (w - tw) / 2, y + 5, COL_TEXT, false);
        }
    }

    class SliderWidget {
        IntSetting setting;
        int x, y, w;
        static final int H = 6;

        SliderWidget(IntSetting setting, int x, int y, int w) {
            this.setting = setting;
            this.x = x;
            this.y = y;
            this.w = w;
        }

        boolean isOver(int mx, int my) {
            return mx >= x && mx <= x + w && my >= y - 4 && my <= y + H + 4;
        }

        void onMouseDown(int mx) { updateValue(mx); }
        void onDrag(int mx) { updateValue(mx); }

        void updateValue(int mx) {
            double ratio = (double)(mx - x) / w;
            ratio = Math.max(0, Math.min(1, ratio));
            int range = setting.getMax() - setting.getMin();
            setting.setValue((int)(setting.getMin() + ratio * range));
        }

        void render(DrawContext ctx, int mx) {
            // Track
            ctx.fill(x, y, x + w, y + H, COL_SLIDER_BG);

            // Fill
            int range = setting.getMax() - setting.getMin();
            double ratio = (double)(setting.getValue() - setting.getMin()) / range;
            int fillW = (int)(ratio * w);
            if (fillW > 0) ctx.fill(x, y, x + fillW, y + H, COL_SLIDER_FILL);

            // Knob
            int knobX = x + fillW - 3;
            ctx.fill(knobX, y - 2, knobX + 6, y + H + 2, COL_SLIDER_KNOB);

            // Value label on right
            String val = String.valueOf(setting.getValue());
            ctx.drawText(textRenderer, Text.literal(val), x + w - textRenderer.getWidth(val), y - 10, COL_TEXT_DIM, false);
        }
    }

    // ───────── Drawing helpers ─────────

    private void fillRounded(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 2, y, x + w - 2, y + h, color);
        ctx.fill(x, y + 2, x + 2, y + h - 2, color);
        ctx.fill(x + w - 2, y + 2, x + w, y + h - 2, color);
    }

    private void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);               // top
        ctx.fill(x, y + h - 1, x + w, y + h, color);       // bottom
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);       // left
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color); // right
    }
}
