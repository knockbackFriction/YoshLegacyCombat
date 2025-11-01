package eu.caec.caeclegacycombat.HitboxExpansion;

import com.github.retrooper.packetevents.protocol.entity.EntityPositionData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import net.kyori.adventure.text.Component;

import java.util.*;

public record ExpandedEntity(String name, int id, UUID uuid, Vector3d operation) {
    private void sendPlayerInfo(User user, UUID uuid, String name) {
        UserProfile userProfile = new UserProfile(uuid, name);
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                userProfile,
                false,
                301,
                GameMode.SURVIVAL,
                Component.text(name),
                null
        );
        WrapperPlayServerPlayerInfoUpdate wrapper = new WrapperPlayServerPlayerInfoUpdate(EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED), playerInfo);
        user.writePacketSilently(wrapper);
    }

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument") // Needs to be modifiable
    public void sendSpawn(User user, Vector3d location) {
        this.sendPlayerInfo(user, uuid, name);

        WrapperPlayServerSpawnEntity spawn = new WrapperPlayServerSpawnEntity(id, uuid, EntityTypes.PLAYER, new Location(location.add(this.operation), 0, 0), 0, 0, null);
        user.writePacketSilently(spawn);
        this.sendMetaData(user, Arrays.asList(new EntityData<>(0, null, null)));
    }

    public void sendPositionSync(User user, EntityPositionData values) {
        EntityPositionData clone = new EntityPositionData(values.getPosition().add(this.operation), values.getDeltaMovement(), values.getYaw(), values.getPitch());
        user.writePacketSilently(new WrapperPlayServerEntityPositionSync(this.id, clone, true));
    }

    public void sendTeleport(User user, Vector3d position) {
        user.writePacketSilently(new WrapperPlayServerEntityTeleport(this.id, new Location(position.add(this.operation), 0, 0), true));
    }

    public void sendRelativeMove(User user, Vector3d position) {
        user.writePacketSilently(new WrapperPlayServerEntityRelativeMove(this.id, position.x, position.y, position.z, true));
    }

    public void sendMetaData(User user, List<EntityData<?>> entityMetadata) {
        ListIterator<EntityData<?>> iterator = entityMetadata.listIterator();
        while (iterator.hasNext()) {
            EntityData<?> next = iterator.next();
            if (next.getIndex() == 0) {
                byte mask = 0x00;
                // invisible
                mask |= 0x20;
                iterator.set(new EntityData<>(0, EntityDataTypes.BYTE, mask));
            }
        }
        user.writePacketSilently(new WrapperPlayServerEntityMetadata(this.id, entityMetadata));
    }

    public void sendDestroy(User user) {
        user.writePacketSilently(new WrapperPlayServerDestroyEntities(this.id));
        user.writePacket(new WrapperPlayServerPlayerInfoRemove(this.uuid));
    }
}