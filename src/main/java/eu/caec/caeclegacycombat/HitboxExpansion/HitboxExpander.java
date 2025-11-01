package eu.caec.caeclegacycombat.HitboxExpansion;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.event.UserLoginEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.server.*;

import java.util.*;

public class HitboxExpander extends SimplePacketListenerAbstract {
    private final Map<UUID, HitboxExpansionData> expandedData = new HashMap<>();

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        // We only expand for 1.9+ clients.
        if (event.getUser().getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
            return;
        }

        HitboxExpansionData data = this.expandedData.computeIfAbsent(event.getUser().getUUID(), $ -> new HitboxExpansionData(event.getUser()));
        switch (event.getPacketType()) {
            case SPAWN_ENTITY -> {
                WrapperPlayServerSpawnEntity wrapper = new WrapperPlayServerSpawnEntity(event);
                if (wrapper.getEntityType() == EntityTypes.PLAYER) {
                    data.spawn(wrapper);
                }
            }
            case ENTITY_POSITION_SYNC -> {
                WrapperPlayServerEntityPositionSync wrapper = new WrapperPlayServerEntityPositionSync(event);
                data.replicate(wrapper.getId(), expandedEntity -> expandedEntity.sendPositionSync(event.getUser(), wrapper.getValues()));
            }
            case ENTITY_TELEPORT -> {
                WrapperPlayServerEntityTeleport wrapper = new WrapperPlayServerEntityTeleport(event);
                data.replicate(wrapper.getEntityId(), expandedEntity -> expandedEntity.sendTeleport(event.getUser(), wrapper.getPosition()));
            }
            case ENTITY_RELATIVE_MOVE -> {
                WrapperPlayServerEntityRelativeMove wrapper = new WrapperPlayServerEntityRelativeMove(event);
                data.replicate(wrapper.getEntityId(), expandedEntity -> expandedEntity.sendRelativeMove(event.getUser(), new Vector3d(wrapper.getDeltaX(), wrapper.getDeltaY(), wrapper.getDeltaZ())));
            }
            case ENTITY_RELATIVE_MOVE_AND_ROTATION -> {
                WrapperPlayServerEntityRelativeMoveAndRotation wrapper = new WrapperPlayServerEntityRelativeMoveAndRotation(event);
                data.replicate(wrapper.getEntityId(), expandedEntity -> expandedEntity.sendRelativeMove(event.getUser(), new Vector3d(wrapper.getDeltaX(), wrapper.getDeltaY(), wrapper.getDeltaZ())));
            }
            case ENTITY_METADATA -> {
                WrapperPlayServerEntityMetadata wrapper = new WrapperPlayServerEntityMetadata(event);
                data.replicate(wrapper.getEntityId(), expandedEntity -> expandedEntity.sendMetaData(event.getUser(), new ArrayList<>(wrapper.getEntityMetadata())));
            }
            case DESTROY_ENTITIES -> {
                WrapperPlayServerDestroyEntities wrapper = new WrapperPlayServerDestroyEntities(event);
                for (int entityId : wrapper.getEntityIds()) {
                    data.replicate(entityId, expandedEntity -> expandedEntity.sendDestroy(event.getUser()));
                }
                data.destroy(wrapper);
            }
        }
    }

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            HitboxExpansionData data = this.expandedData.get(event.getUser().getUUID());
            if (data == null) {
                return;
            }

            data.entities.forEach((originalId, expandedEntity) -> {
                if (expandedEntity.id() == wrapper.getEntityId()) {
                    wrapper.setEntityId(originalId);
                }
            });
        }
    }

    @Override
    public void onUserLogin(UserLoginEvent event) {
        this.expandedData.put(event.getUser().getUUID(), new HitboxExpansionData(event.getUser()));
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        this.expandedData.remove(event.getUser().getUUID());
    }
}