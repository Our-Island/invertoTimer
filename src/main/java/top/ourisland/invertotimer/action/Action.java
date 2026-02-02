package top.ourisland.invertotimer.action;

/**
 * Represents a generic executable action in the timer system.
 * <p>
 * An {@code Action} defines a unit of behavior that can be triggered by the runtime. Each action has a unique name, a
 * human-readable description, and executable logic.
 * </p>
 *
 * <p>
 * Implementations are expected to be stateless or manage their own internal state safely. The {@link #execute()} method
 * should contain the actual behavior performed when the action is triggered.
 * </p>
 *
 * @author Our-Island
 */
public interface Action {

    /**
     * Returns the unique identifier of this action.
     * <p>
     * This name is typically used for configuration lookup
     * and action registration.
     * </p>
     *
     * @return the action name, never {@code null}
     */
    String name();

    /**
     * Returns a human-readable description of this action.
     * <p>
     * The description is usually localized and intended
     * for display in user interfaces or help messages.
     * </p>
     *
     * @return the action description, never {@code null}
     */
    String description();

    /**
     * Executes the action.
     * <p>
     * The concrete behavior depends on the implementation.
     * This method is called by the runtime when the action
     * is triggered.
     * </p>
     */
    void execute();
}
