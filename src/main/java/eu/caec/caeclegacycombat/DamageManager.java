package eu.caec.caeclegacycombat;

import org.bukkit.Material;

import java.util.HashMap;

public class DamageManager { //formulas taken from: https://tiagofar78.github.io/MinecraftDamageCalculator/
    /*
    * WD: Weapon Damage
    * SL: Sharpness Level
    * C: Critical Hit (0 or 1)
    * SPL: Strength Potion Level
    * */
    public double calculateBaseDamage(float wd, int sl, int c, int spl) { //currently unused
        return (1 + 0.5 * c) * (1 + wd + 1.25 * sl) * (1 + 1.3 * spl);
    }

    /*
    * D: Base Damage
    * B: Blocking (0 or 1)
    * A: Armor Points (shown above hearts)
    * P: Total Protection Enchantment Level (sum of all protection levels in all pieces of armor)
    * R: Resistance Effect Level
    * */
    public double calculateDamageReduction(float d, int b, int a, int p, int r) { //currently unused
        return (d - b * (d - 1) / 2) * (1 - 0.04 * a) * (1 - 0.04 * p) * (1 - 0.2 * r);
    }

    static HashMap<Material, Integer> oldToolDamages = new HashMap<>();
    static HashMap<Material, Integer> newToolDamages = new HashMap<>();
    static {
        oldToolDamages.put(Material.WOODEN_AXE, 4);
        oldToolDamages.put(Material.STONE_AXE, 5);
        oldToolDamages.put(Material.IRON_AXE, 6);
        oldToolDamages.put(Material.GOLDEN_AXE, 4);
        oldToolDamages.put(Material.DIAMOND_AXE, 6);
        oldToolDamages.put(Material.NETHERITE_AXE, 7);

        newToolDamages.put(Material.WOODEN_AXE, 7);
        newToolDamages.put(Material.STONE_AXE, 9);
        newToolDamages.put(Material.IRON_AXE, 9);
        newToolDamages.put(Material.GOLDEN_AXE, 7);
        newToolDamages.put(Material.DIAMOND_AXE, 9);
        newToolDamages.put(Material.NETHERITE_AXE, 10);
    }

    public boolean shouldToolHaveDifferentDamage(Material mat) {
        return oldToolDamages.containsKey(mat);
    }

    public double damageWithOldToolValue(double damage, Material tool) {
        return (damage - newToolDamages.get(tool) + oldToolDamages.get(tool));
    }
}
