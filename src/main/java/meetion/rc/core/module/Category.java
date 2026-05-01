package meetion.rc.core.module;

/**
 * Categorises modules for ClickGUI tabs.
 * <p>The {@code glyph} field is a short tab indicator drawn next to the name in the
 * Contextual Dock layout (e.g. an icon char from the Material Icons resource font, or
 * a simple emoji as a placeholder until we ship a glyph atlas).
 */
public enum Category {
    COMBAT  ("Combat",   "⚔"),
    MOVEMENT("Movement", "↗"),
    PLAYER  ("Player",   "★"),
    VISUAL  ("Visual",   "◧"),
    MISC    ("Misc",     "✦");

    private final String name;
    private final String glyph;

    Category(String name, String glyph) {
        this.name = name;
        this.glyph = glyph;
    }

    public String getName()  { return name; }
    public String getGlyph() { return glyph; }
}
