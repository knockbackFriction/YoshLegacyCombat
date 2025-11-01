package eu.caec.caeclegacycombat;

import com.github.retrooper.packetevents.PacketEvents;
import eu.caec.caeclegacycombat.HitboxExpansion.HitboxExpander;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import java.util.HashMap;

public final class CLC extends JavaPlugin implements Listener {
    public static CLC instance;

    public static CLC getInstance() {
        return instance;
    }

    KnockbackManager knockbackManager;
    SwordBlocking swordBlocking;
    PlayerRegeneration playerRegeneration;
    DamageManager damageManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        knockbackManager = new KnockbackManager();
        swordBlocking = new SwordBlocking();
        playerRegeneration = new PlayerRegeneration();
        damageManager = new DamageManager();

        PacketEvents.getAPI().getEventManager().registerListener(new PacketEventsListener());
        PacketEvents.getAPI().getEventManager().registerListener(new HitboxExpander());
        PacketEvents.getAPI().init();
    }

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        //Are all listeners read only?
        PacketEvents.getAPI().getSettings().reEncodeByDefault(true)
                .checkForUpdates(true);
        PacketEvents.getAPI().load();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.getPlayer().getAttribute(Attribute.ATTACK_SPEED).setBaseValue(255);
    }

    HashMap<Player, Vector> kbHashMap = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerVelocityEvent(PlayerVelocityEvent event) {
        if (!kbHashMap.containsKey(event.getPlayer())) return;
        event.setVelocity(kbHashMap.get(event.getPlayer()));
        kbHashMap.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        double dmg = event.getDamage();
        if (event.getDamager() instanceof Player attacker && event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            kbHashMap.put( victim, knockbackManager.calculateMeleeKnockback(victim, attacker) );

            Material attackerMat = attacker.getInventory().getItemInMainHand().getType();
            if (damageManager.shouldToolHaveDifferentDamage(attackerMat)) {
                dmg = damageManager.damageWithOldToolValue(dmg, attackerMat);
            }
        }

        if (victim.hasActiveItem() && swordBlocking.isSword(victim.getInventory().getItemInMainHand().getType())) {
            dmg /= 2;
        }
        event.setDamage(dmg);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (swordBlocking.isSword(item.getType())) {
            if (item.getData(DataComponentTypes.CONSUMABLE) == null) {
                item = swordBlocking.makeSwordBlockable(item);
                event.getPlayer().getInventory().setItemInMainHand(item);
            }
        }
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent event) {
        if (event.getEntityType() != EntityType.PLAYER
                || event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED)
            return; //we only want to alter the behaviour of player satiation regen

        event.setCancelled(true); //cancel, so we can use our own logic instead
        playerRegeneration.doRegenLogic((Player) event.getEntity());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerRegeneration.removePlayerEntry(event.getPlayer());
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getHitEntity() instanceof Player victim)) return;
        if (event.getEntity() instanceof FishHook || event.getEntity() instanceof Snowball || event.getEntity() instanceof Egg) {
            if (!(event.getEntity().getShooter() instanceof Player shooter)) return;
            if (victim.getNoDamageTicks() > victim.getMaximumNoDamageTicks() / 2f) return;
            victim.setVelocity(knockbackManager.calculateSimpleKnockback(victim, shooter.getLocation()));
            victim.damage(0.001d);
        }
    }

    @EventHandler
    public void onReelIn(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) return;
        if (!(event.getCaught() instanceof Player)) return;
        event.getHook().remove();
        event.setCancelled(true);
    }
}
