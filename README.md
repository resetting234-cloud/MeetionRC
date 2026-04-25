# MeetionRC

Minecraft **1.21.8** Fabric utility client. Java 21.

## Modules (initial set)

**Combat**
- KillAura — target search, FOV/range, silent rotations, anticheat presets (Grim, Verus, Vulcan, Matrix, Themis, Spartan, Negativity, Other) with extra modifiers (RandomGCD, Smooth, PreMotion, etc.)
- TriggerBot
- AutoClicker (humanised CPS range)

**Movement**
- NoSlow (Eat / Block / Bow + bypass preset)
- AutoSprint (Always / Forward / Omni)
- InvMove (move while inventory or chat is open)

**Player**
- AutoArmor (best protection + toughness)
- AutoPotion (auto-throw splash heal at low HP)
- NoJumpDelay

**Visual**
- HUD (Watermark, ArrayList, Notifications, TargetHUD)
- SwingAnimation
- AspectRatio (FOV multiplier)
- CustomHitSound
- Particless (filter particle types)
- Fps (graphics tuning preset)
- AutoCommand (timer-based commands)

## Controls

- `Right Shift` — open ClickGUI
- `.help` in chat — list commands
- `.toggle <module>` — toggle a module
- `.bind <module> <key|none>` — bind a hotkey
- `.config <save|load|list> [name]` — manage profiles (stored under `<minecraft>/MeetionRC/`)
- `.friend <add|remove|list> [name]` — friend list

## Build / Run

```bash
./gradlew build             # produces build/libs/meetionrc-<version>.jar
./gradlew runClient         # launches dev client
```

Open the project in IntelliJ IDEA via `File → Open → build.gradle`. Fabric Loom auto-generates a `Minecraft Client` run configuration.

## Stack
- Fabric Loader 0.16.x, Fabric API 0.133.4+1.21.8
- Yarn mappings 1.21.8+build.1
- Mixin (SpongePowered)
- Lombok (compileOnly), Gson 2.10.1

## Status

This is the initial foundation PR. Roadmap (next PRs):
- ElytraTarget, AimAssist, SuperFirework, NoWeb modules
- Full ClickGUI polish (drag, search, color picker, bind picker)
- TargetHUD renderer
- More refined anticheat profile tuning per module
