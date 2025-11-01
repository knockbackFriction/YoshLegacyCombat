package eu.caec.caeclegacycombat.HitboxExpansion;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;

import java.util.UUID;
import java.util.function.Consumer;

public class HitboxExpansionData {
    private final User user;
    public final Multimap<Integer, ExpandedEntity> entities = ArrayListMultimap.create();

    public HitboxExpansionData(User user) {
        this.user = user;
    }

    public void spawn(WrapperPlayServerSpawnEntity spawnEntity) {
        this.entities.put(spawnEntity.getEntityId(), new ExpandedEntity("++", SpigotReflectionUtil.generateEntityId(), UUID.randomUUID(), new Vector3d(0.05D, 0, 0.05D)));
        this.entities.put(spawnEntity.getEntityId(), new ExpandedEntity("-+", SpigotReflectionUtil.generateEntityId(), UUID.randomUUID(), new Vector3d(-0.05D, 0, 0.05D)));
        this.entities.put(spawnEntity.getEntityId(), new ExpandedEntity("+-", SpigotReflectionUtil.generateEntityId(), UUID.randomUUID(), new Vector3d(0.05D, 0, -0.05D)));
        this.entities.put(spawnEntity.getEntityId(), new ExpandedEntity("--", SpigotReflectionUtil.generateEntityId(), UUID.randomUUID(), new Vector3d(-0.05D, 0, -0.05D)));

        for (ExpandedEntity expandedEntity : this.entities.get(spawnEntity.getEntityId())) {
            expandedEntity.sendSpawn(user, spawnEntity.getPosition());
        }
    }

    public void destroy(WrapperPlayServerDestroyEntities destroyEntities) {
        for (int entityId : destroyEntities.getEntityIds()) {
            this.entities.removeAll(entityId);
        }
    }

    public void replicate(int id, Consumer<ExpandedEntity> consumer) {
        this.entities.get(id).forEach(consumer);
    }
}