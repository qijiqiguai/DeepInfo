package tech.qi.deepinfo.proxy.core.spider;


/**
 */
@Component
public class ProxyProcessor implements PageProcessor {

    private static Logger logger = LoggerFactory.getLogger(PageProcessor.class);

    //Site 可以设 RequestHeader | Proxy | ProxyPool | Cookie, 参加 Downloader 中对 Request 的包装
    private Site site = Site.me().setRetryTimes(1).setSleepTime(1000).setCycleRetryTimes(1).setCharset("UTF-8");

    @Override
    public void process(Page page) {
        if (!page.getResultItems().isSkip()) {
            SpiderJob spiderJob = (SpiderJob) page.getRequest();
            TaskType task = spiderJob.getTaskType();
            switch (task) {
                case PROXY:
                    proxy(page);
                    break;
                default:
                    logger.error("TaskType Unmatched Error!", JSON.toJSONString(page.getRequest()) );
                    page.setSkip(true); //一旦出现异常, 后续的Pipeline不做处理
                    break;
            }
        }
    }

    private void proxy(Page page) {
        // 没有要处理的内容
    }

    @Override
    public Site getSite() {
        return site;
    }
}
