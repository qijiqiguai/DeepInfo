package tech.qi.deepinfo.frame.core;

/**
 *
 * @author wangqi
 * @date 2017/11/24 下午7:59
 */
public final class LifecycleException extends Exception {
    private static final long serialVersionUID = 1L;

    public LifecycleException() { }

    public LifecycleException(String message) {
        super(message);
    }

    public LifecycleException(Throwable throwable) {
        super(throwable);
    }

    public LifecycleException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
