package meetion.rc.manager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendManager {

    private final Set<String> friends = new HashSet<>();

    public void add(String name) { friends.add(name.toLowerCase()); }
    public void remove(String name) { friends.remove(name.toLowerCase()); }
    public boolean is(String name) { return friends.contains(name.toLowerCase()); }
    public boolean is(Entity entity) {
        if (entity instanceof PlayerEntity p) return is(p.getGameProfile().getName());
        return false;
    }
    public List<String> list() { return List.copyOf(friends); }
}
