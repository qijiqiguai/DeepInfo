package tech.qi.deepinfo.frame.context;

/**
 * 是否可关闭的，便于程序回收资源
 */
public interface Stoppable {
    public void stopMe();
}
