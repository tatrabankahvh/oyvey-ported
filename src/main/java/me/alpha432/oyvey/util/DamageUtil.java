package me.alpha432.oyvey.util;

import me.alpha432.oyvey.util.traits.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class DamageUtil implements Util {
    
    /**
     * Calculate explosion damage at a given position for a player
     */
    public static float calculateDamage(BlockPos pos, Player player) {
        if (mc.level == null || mc.player == null) return 0f;
        
        Vec3 explosionPos = Vec3.atCenterOf(pos);
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);
        
        double distance = playerPos.distanceTo(explosionPos);
        
        // Crystal explosion has radius of 6 blocks
        float maxDistance = 6.0f;
        if (distance > maxDistance) return 0f;
        
        // Get block exposure (simple check)
        float exposure = 1.0f;
        try {
            Explosion explosion = new Explosion(mc.level, null, explosionPos.x, explosionPos.y, explosionPos.z, 6.0f, false, Explosion.BlockInteraction.DESTROY);
            exposure = explosion.getBlockInteraction() != null ? 1.0f : 0.5f;
        } catch (Exception e) {
            // Fallback to simple calculation
        }
        
        // Basic damage formula: 4 * (1 - distance/6) * exposure + 0.5
        float damage = Math.max(0.5f, (7.0f / 2.0f) * (1.0f - (distance / maxDistance)) * exposure);
        
        // Account for armor
        damage = applyArmorReduction(player, damage);
        
        // Account for resistance effect
        damage = applyResistanceReduction(player, damage);
        
        return damage;
    }
    
    private static float applyArmorReduction(Player player, float damage) {
        float armor = (float) player.getArmorValue();
        float reduction = armor * 0.04f;
        return damage * (1.0f - Math.min(reduction, 0.8f));
    }
    
    private static float applyResistanceReduction(Player player, float damage) {
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE)) {
            int level = player.getEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE).getAmplifier();
            damage *= Math.max(1.0f - ((float) (level + 1) * 0.2f), 0.0f);
        }
        return damage;
    }
    
    /**
     * Check if the player can take damage
     */
    public static boolean canTakeDamage(boolean suicideMode) {
        if (mc.player == null) return false;
        
        boolean holding_totem = mc.player.getOffhandItem().is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);
        
        if (holding_totem) return true;
        if (suicideMode) return true;
        
        return mc.player.getHealth() > 1.0f;
    }
    
    /**
     * Check if a player has low armor
     */
    public static boolean isArmorLow(Player player, int threshold) {
        int armorValue = 0;
        for (int i = 0; i < 4; i++) {
            net.minecraft.world.item.ItemStack stack = player.getInventory().getArmor(i);
            if (!stack.isEmpty()) {
                armorValue += stack.getMaxDamage() - stack.getDamageValue();
            }
        }
        return armorValue < threshold;
    }
    
    /**
     * Check if a player is naked (no armor)
     */
    public static boolean isNaked(Player player) {
        for (int i = 0; i < 4; i++) {
            if (!player.getInventory().getArmor(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get player health
     */
    public static float getPlayerHealth(Player player) {
        return player.getHealth();
    }
}
