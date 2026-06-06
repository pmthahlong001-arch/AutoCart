package com.autocartmod.modules;

import com.autocartmod.modules.combat.AutoCart;
import net.minecraft.client.MinecraftClient;

import java.util.*;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();
    public final AutoCart autoCart;

    public ModuleManager() {
        autoCart = new AutoCart();
        modules.add(autoCart);
    }

    public void onTick(MinecraftClient client) {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick(client);
            }
        }
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Module.Category category) {
        List<Module> result = new ArrayList<>();
        for (Module m : modules) {
            if (m.getCategory() == category) result.add(m);
        }
        return result;
    }

    public Module.Category[] getUsedCategories() {
        Set<Module.Category> cats = new LinkedHashSet<>();
        for (Module m : modules) cats.add(m.getCategory());
        return cats.toArray(new Module.Category[0]);
    }
}
