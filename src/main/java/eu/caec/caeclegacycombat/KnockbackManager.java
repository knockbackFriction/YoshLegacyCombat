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
    float verticalPlayerMotionInfluence = (float) config.getDouble("vertical-player-motion-influence");
    float horizontalPlayerMotionInfluence = (float) config.getDouble("horizontal-player-motion-influence");
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

        Vector velo = victim.getVelocity();
        velo.multiply(new Vector(horizontalPlayerMotionInfluence,
                verticalPlayerMotionInfluence,
                horizontalPlayerMotionInfluence)
        );

        velo.add( new Vector(-(Math.sin(angle * 3.1415927F / 180.0F) * totalHorizontal),
                totalVertical,
                -Math.cos(angle * 3.1415927F / 180.0F) * totalHorizontal)
        );

        if (velo.getY() > verticalLimit) {
            velo.setY(verticalLimit);
        }

        return velo;
    }

    //for things like projectiles
    public Vector calculateSimpleKnockback(Player victim, Location source) {
        float angle = (float) Math.toDegrees(Math.atan2(source.getX() - victim.getX(), source.getZ() - victim.getZ()));

        Vector velo = victim.getVelocity();
        velo.multiply(new Vector(horizontalPlayerMotionInfluence,
                verticalPlayerMotionInfluence,
                horizontalPlayerMotionInfluence)
        );

        velo.add( new Vector(-(Math.sin(angle * 3.1415927F / 180.0F) * horizontalBase),
                verticalBase,
                -Math.cos(angle * 3.1415927F / 180.0F) * horizontalBase)
        );

        if (velo.getY() > verticalLimit) {
            velo.setY(verticalLimit);
        }

        return velo;
    }
}
