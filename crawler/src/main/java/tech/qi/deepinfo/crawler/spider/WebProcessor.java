package tech.qi.deepinfo.crawler.spider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * 从 Job 返回结果到这来来处理, 那么就只有两种情况:
 * 1: Job运行失败, 并给出Status封装类并设置 Skip
 * 2: Job运行成功, 那么就已经得到了完整的 HotList, 或者完整的 Anchor 信息, 做对应的处理就行了
 *
 * @author wangqi
 */

@Component
public class WebProcessor implements PageProcessor {
    /**
     * Site 可以设 RequestHeader | Proxy | ProxyPool | Cookie, 参加 Downloader 中对 Request 的包装
     */
    private Site site = Site.me().setRetryTimes(1).setSleepTime(1000).setCycleRetryTimes(1).setCharset("UTF-8");

    @Value("${spider.enable.max.queue.size}")
    private boolean enableMaxQ;

    @Value("${spider.max.queue.size}")
    private int maxQueueSize;

    @Autowired
    SpiderHandler spiderHandler;

    @Override
    public void process(Page page) {
        System.out.println(page.getRawText());
    }

    @Override
    public Site getSite() {
        return site;
    }
}
