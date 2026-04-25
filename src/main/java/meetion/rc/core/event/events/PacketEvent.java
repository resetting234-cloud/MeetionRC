package meetion.rc.core.event.events;

import meetion.rc.core.event.Event;
import net.minecraft.network.packet.Packet;

public class PacketEvent extends Event {
    private Packet<?> packet;
    private final Direction direction;

    public PacketEvent(Packet<?> packet, Direction direction, Era era) {
        this.packet = packet;
        this.direction = direction;
        setEra(era);
    }

    public Packet<?> getPacket() { return packet; }
    public void setPacket(Packet<?> packet) { this.packet = packet; }
    public Direction getDirection() { return direction; }

    public enum Direction {
        IN, OUT
    }
}
