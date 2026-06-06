package com.autocartmod.modules.combat;

import com.autocartmod.modules.Module;
import com.autocartmod.settings.*;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;

public class AutoCart extends Module {

    public enum Mode { Bow, CrossBow }
    public enum ReFillMode { None, Normal, Legit }
    public enum Target { Players, Mobs, Both }

    // Settings
    public final EnumSetting<Mode> mode = new EnumSetting<>("Mode", "Attack mode", Mode.CrossBow);
    public final IntSetting delay = new IntSetting("Delay", "Delay between shots (ticks)", 7, 0, 100);
    public final IntSetting cartAuraDelay = new IntSetting("Cart Aura Delay", "Delay for cart aura (ticks)", 3, 0, 20);
    public final IntSetting slot = new IntSetting("Slot", "Hotbar slot for refill (1-9)", 4, 1, 9);
    public final BooleanSetting swapBack = new BooleanSetting("Swap Back", "Swap back to original slot after shooting", true);
    public final BooleanSetting changeLook = new BooleanSetting("Change Look", "Silently rotate to target", false);
    public final BooleanSetting cartAura = new BooleanSetting("Cart Aura", "Auto place and shoot cart", true);
    public final EnumSetting<ReFillMode> reFill = new EnumSetting<>("ReFill", "Refill mode", ReFillMode.Legit);
    public final EnumSetting<Target> target = new EnumSetting<>("Target", "Who to target", Target.Players);

    private int tickCounter = 0;
    private boolean active = false;

    public AutoCart() {
        super("AutoCart", "Automates TNT minecart combat setups.", Category.COMBAT);
        settings.add(mode);
        settings.add(delay);
        settings.add(cartAuraDelay);
        settings.add(slot);
        settings.add(swapBack);
        settings.add(changeLook);
        settings.add(cartAura);
        settings.add(reFill);
        settings.add(target);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
        active = false;
    }

    @Override
    public void onDisable() {
        tickCounter = 0;
        active = false;
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (fullNullCheck()) return;
        tickCounter++;

        if (mode.getValue() == Mode.CrossBow && cartAura.getValue()) {
            if (tickCounter >= delay.getValue()) {
                tickCounter = 0;
                executeCartAura(client);
            }
        }
    }

    private void executeCartAura(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        // Find nearest player target
        PlayerEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) continue;
            double dist = client.player.squaredDistanceTo(player);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }

        if (nearest == null) return;

        // Find loaded crossbow in hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (isCrossbowCharged(stack)) {
                int prev = client.player.getInventory().selectedSlot;
                client.player.getInventory().selectedSlot = i;

                if (changeLook.getValue()) {
                    lookAt(client, nearest.getPos().add(0, nearest.getStandingEyeHeight(), 0));
                }

                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);

                if (swapBack.getValue()) {
                    client.player.getInventory().selectedSlot = prev;
                }
                break;
            }
        }
    }

    private void lookAt(MinecraftClient client, Vec3d target) {
        if (client.player == null) return;
        Vec3d eyes = client.player.getEyePos();
        Vec3d diff = target.subtract(eyes);
        double dist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float yaw = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, dist));
        client.player.setYaw(yaw);
        client.player.setPitch(pitch);
    }

    private boolean isCrossbowCharged(ItemStack stack) {
        if (stack.getItem() != Items.CROSSBOW) return false;
        ChargedProjectilesComponent charged = stack.get(DataComponentTypes.CHARGED_PROJECTILES);
        return charged != null && !charged.isEmpty();
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValue().name();
    }
}
