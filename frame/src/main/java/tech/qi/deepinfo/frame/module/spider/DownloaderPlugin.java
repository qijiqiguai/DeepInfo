package tech.qi.deepinfo.frame.module.spider;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

/**
 * @author qiwang
 *
 * 插件的逻辑非常简单，就是给一个输入，给一个输出。
 */
public interface DownloaderPlugin {
    /**
     *
     * @param task
     * @return
     */
    Page run(Request task);
}
