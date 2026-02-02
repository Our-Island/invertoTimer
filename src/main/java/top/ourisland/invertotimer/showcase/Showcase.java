package top.ourisland.invertotimer.showcase;

/**
 * Represents a display unit that can present information to players.
 * <p>
 * A {@code Showcase} defines a way to "show" some content to the current audience (e.g. chat message, action bar,
 * title, boss bar). Implementations are expected to fetch any dynamic content (such as the current timer text) at
 * display time.
 * </p>
 *
 * <p>
 * Implementations should typically respect runtime limitations (e.g. permission checks), and avoid throwing exceptions
 * from {@link #show()}.
 * </p>
 *
 * @author Our-Island
 */
public interface Showcase {
    /**
     * Returns the unique identifier of this showcase type.
     * <p>
     * This name is commonly used for configuration lookup and showcase registration. Examples include {@code "text"},
     * {@code "actionbar"}, {@code "bossbar"}, and {@code "title"}.
     * </p>
     *
     * @return the showcase name, never {@code null}
     */
    String name();

    /**
     * Returns a human-readable description of this showcase.
     * <p>
     * The description is typically localized and intended for help messages or UIs.
     * </p>
     *
     * @return the showcase description, never {@code null}
     */
    String description();

    /**
     * Displays the showcase content to the intended audience.
     * <p>
     * The concrete behavior depends on the implementation (e.g. sending a chat message, action bar text,
     * title/subtitle, or showing a boss bar). Implementations should perform necessary filtering (such as permission
     * checks) before displaying.
     * </p>
     */
    void show();
}
