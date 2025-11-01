package eu.caec.caeclegacycombat;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KnockbackManager {
    FileConfiguration config = CLC.getInstance().getConfig();

    float horizontalBase = (float) config.getDouble("horizontal-base");
    float horizontalExtra = (float) config.getDouble("horizontal-extra");
    float verticalBase = (float) config.getDouble("vertical-base");
    float verticalExtra = (float) config.getDouble("vertical-extra");
    float verticalFriction = (float) config.getDouble("vertical-friction");
    float horizontalFriction = (float) config.getDouble("horizontal-friction");
    float verticalLimit = (float) config.getDouble("vertical-limit");

    public Vector calculateMeleeKnockback(Player victim, Player attacker) {
        float angle = (float) Math.toDegrees(Math.atan2(attacker.getLocation().getX() - victim.getLocation().getX(), attacker.getLocation().getZ() - victim.getLocation().getZ()));
        float totalVertical;

        float totalHorizontal = horizontalBase + (attacker.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.KNOCKBACK) * horizontalExtra);
        if (attacker.isSprinting()) {
            totalVertical = verticalBase + verticalExtra;
            totalHorizontal += horizontalExtra;
        } else {
            totalVertical = verticalBase;
        }

        double finalX = Math.sin(angle * 3.1415927F / 180.0F) * totalHorizontal;
        double finalZ = Math.cos(angle * 3.1415927F / 180.0F) * totalHorizontal;

        Vector velo = victim.getVelocity();
        velo.setX((velo.getX() / horizontalFriction) - finalX);
        velo.setY((velo.getY() / verticalFriction) + totalVertical);
        velo.setZ((velo.getZ() / horizontalFriction) - finalZ);

        if (velo.getY() > verticalLimit) {
            velo.setY(verticalLimit);
        }

        return velo;
    }

    //for things like projectiles
    public Vector calculateSimpleKnockback(Player victim, Location source) {
        float angle = (float) Math.toDegrees(Math.atan2(source.getX() - victim.getX(), source.getZ() - victim.getZ()));

        double finalX = Math.sin(angle * 3.1415927F / 180.0F) * horizontalBase;
        double finalZ = Math.cos(angle * 3.1415927F / 180.0F) * horizontalBase;

        Vector velo = victim.getVelocity();
        velo.setX((velo.getX() / horizontalFriction) - finalX);
        velo.setY((velo.getY() / verticalFriction) + verticalBase);
        velo.setZ((velo.getZ() / horizontalFriction) - finalZ);

        if (velo.getY() > verticalLimit) {
            velo.setY(verticalLimit);
        }

        return velo;
    }
}
