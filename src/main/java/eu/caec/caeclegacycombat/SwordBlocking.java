package eu.caec.caeclegacycombat;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Stream;

public class SwordBlocking {
    private static final Map<Material, Boolean> SWORDS = new HashMap<>();
    static {
        Stream.of(
                Material.WOODEN_SWORD, Material.STONE_SWORD, Material.GOLDEN_SWORD, Material.IRON_SWORD,
                Material.DIAMOND_SWORD, Material.NETHERITE_SWORD
        ).forEach(s -> SWORDS.put(s, Boolean.TRUE));
    }

    // 9999 ticks should be good lol
    Consumable newDataConsumable = Consumable.consumable()
            .animation(ItemUseAnimation.BLOCK)
            .consumeSeconds(9999f)
            .hasConsumeParticles(false)
            .build();

    public boolean isSword(Material mat) {
        return SWORDS.containsKey(mat);
    }

    public ItemStack makeSwordBlockable(ItemStack item) {
        item.setData(DataComponentTypes.CONSUMABLE, newDataConsumable);
        return item;
    }
}
