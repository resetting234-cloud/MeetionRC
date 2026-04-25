package meetion.rc.modules.visual;

import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.AttackEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.ModeSetting;
import meetion.rc.core.setting.impl.NumberSetting;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class CustomHitSound extends Module {

    private final ModeSetting sound = register(new ModeSetting("Sound", "BlockGlass",
            "BlockGlass", "Anvil", "Bell", "Note", "ItemBreak"));
    private final NumberSetting volume = register(new NumberSetting("Volume", 1.0, 0.0, 2.0, 0.1));
    private final NumberSetting pitch = register(new NumberSetting("Pitch", 1.0, 0.5, 2.0, 0.05));

    public CustomHitSound() {
        super("CustomHitSound", "Plays a sound on hit", Category.VISUAL);
    }

    @EventHandler
    public void onAttack(AttackEvent ev) {
        if (mc().player == null || mc().getSoundManager() == null) return;
        var event = switch (sound.getValue()) {
            case "Anvil" -> SoundEvents.BLOCK_ANVIL_LAND;
            case "Bell" -> SoundEvents.BLOCK_BELL_USE;
            case "Note" -> SoundEvents.BLOCK_NOTE_BLOCK_PLING.value();
            case "ItemBreak" -> SoundEvents.ENTITY_ITEM_BREAK;
            default -> SoundEvents.BLOCK_GLASS_BREAK;
        };
        mc().getSoundManager().play(PositionedSoundInstance.master(event, pitch.asFloat(), volume.asFloat()));
    }
}
