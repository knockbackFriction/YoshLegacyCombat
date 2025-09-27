package eu.caec.caeclegacycombat;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class PlayerRegeneration {
    private final Map<UUID, Long> healTimes = new WeakHashMap<>();

    public void removePlayerEntry(Player p) {
        healTimes.remove(p.getUniqueId());
    }

    public void doRegenLogic(Player p) { //took OCM code then i hardcoded values lol
        final UUID playerId = p.getUniqueId();

        // Get exhaustion & saturation values before healing modifies them
        final float previousExhaustion = p.getExhaustion();

        // Check that it has been at least x seconds since last heal
        final long currentTime = System.currentTimeMillis();
        final boolean hasLastHealTime = healTimes.containsKey(playerId);
        final long lastHealTime = healTimes.computeIfAbsent(playerId, id -> currentTime);

        // If we're skipping this heal, we must fix the exhaustion in the following tick
        if (hasLastHealTime && currentTime - lastHealTime <= 4000L) {
            Bukkit.getScheduler().runTaskLater(CLC.getInstance(), () -> p.setExhaustion(previousExhaustion), 1L);
            return;
        }

        final double maxHealth = p.getAttribute(Attribute.MAX_HEALTH).getValue();
        final double playerHealth = p.getHealth();

        if (playerHealth < maxHealth) {
            p.setHealth(Math.min(playerHealth + 1f, maxHealth));
            healTimes.put(playerId, currentTime);
        }

        Bukkit.getScheduler().runTaskLater(CLC.getInstance(), () -> {
            // We do this in the next tick because bukkit doesn't stop the exhaustion change when cancelling the event
            p.setExhaustion(previousExhaustion + 3f);
        }, 1L);
    }
}
