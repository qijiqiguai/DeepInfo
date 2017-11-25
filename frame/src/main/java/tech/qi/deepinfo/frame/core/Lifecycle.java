package tech.qi.deepinfo.frame.core;

/**
 *
 * @author wangqi
 * @date 2017/11/24 下午7:57
 */
public interface Lifecycle {
    enum State {
        NEW(false, "new"),

        INITIALIZING(false, "initializing"),
        INITIALIZED(false, "initialized"),

        STARTING(false, "starting"),
        STARTED(true, "started"),

        STOPPING(false, "stopping"),
        STOPPED(false, "stopped"),

        DESTROYING(false, "destroying"),
        DESTROYED(false, "destroyed"),

        FAILED(false, "failed");

        private final boolean available;
        private final String lifecycleEvent;

        State(boolean available, String lifecycleEvent) {
            this.available = available;
            this.lifecycleEvent = lifecycleEvent;
        }

        public boolean isAvailable() {
            return this.available;
        }

        public String getLifecycleEvent() {
            return this.lifecycleEvent;
        }
    }

    void init() throws LifecycleException;

    void start() throws LifecycleException;

    void stopMe() throws LifecycleException;

    void destroy() throws LifecycleException;

    State getState();

    String getStateName();
}
