package eu.caec.caeclegacycombat;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class PacketEventsListener extends PacketListenerAbstract {
    public PacketEventsListener() {super(PacketListenerPriority.HIGH);}

    private static final Map<ResourceLocation, Boolean> BLACKLISTED_SOUNDS = new HashMap<>();
    private static final Map<ResourceLocation, Boolean> BLACKLISTED_PARTICLES = new HashMap<>();
    static {
        Stream.of(
                "entity.player.attack.crit",
                "entity.player.attack.knockback",
                "entity.player.attack.nodamage",
                "entity.player.attack.strong",
                "entity.player.attack.sweep",
                "entity.player.attack.weak"
        ).forEach(s -> BLACKLISTED_SOUNDS.put(ResourceLocation.minecraft(s), Boolean.TRUE));

        Stream.of(
                "sweep_attack",
                "damage_indicator"
        ).forEach(s -> BLACKLISTED_PARTICLES.put(ResourceLocation.minecraft(s), Boolean.TRUE));
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        switch(event.getPacketType()) {
            case PacketType.Play.Server.SOUND_EFFECT:
                WrapperPlayServerSoundEffect soundPacket = new WrapperPlayServerSoundEffect(event);
                ResourceLocation soundId = soundPacket.getSound().getSoundId();
                if (BLACKLISTED_SOUNDS.containsKey(soundId)) {
                    event.setCancelled(true);
                }
                break;
            case PacketType.Play.Server.PARTICLE:
                WrapperPlayServerParticle particlePacket = new WrapperPlayServerParticle(event);
                ResourceLocation particleId = particlePacket.getParticle().getType().getName();
                if (BLACKLISTED_PARTICLES.containsKey(particleId)) {
                    event.setCancelled(true);
                }
                break;
            default:
        }
    }
}