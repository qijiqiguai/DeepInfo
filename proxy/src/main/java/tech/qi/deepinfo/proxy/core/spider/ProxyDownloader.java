package tech.qi.deepinfo.proxy.core.spider;

import java.util.Date;

/**
 */
@ThreadSafe
@Component
public class ProxyDownloader extends AbstractDownloader {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Date lastSpiderDownloadTime;

    @Autowired
    DownloadPluginHandler downloadPluginHandler;

    @Override
    public Page download(Request request, Task task) {
        //把最近一次爬取时间存入liveDownloader中
        this.lastSpiderDownloadTime = new Date();
        SpiderJob spiderJob = null;
        try {
            spiderJob = new SpiderJob(request); // Request -> Job 强制转换不行, Java语言的限制
            Class clazz = downloadPluginHandler.getPlugin(spiderJob.getPluginName());
            if (null == clazz) {
                logger.warn("无法为 Job 找到插件: {}", JSON.toJSONString(spiderJob));
                return new Page().setSkip(true);
            }
            GroovyObject groovyObject = (GroovyObject) clazz.newInstance();

            logger.info("Downloading: " + spiderJob.getUrl());
            Page res = (Page) groovyObject.invokeMethod("run", spiderJob);
            res.setRequest(spiderJob);//Request对象还要塞回去, 后面会用到
            logger.debug("Download Result: " + JSON.toJSONString(res.getResultItems().getAll()));
            return res;
        } catch (Exception e) {
            logger.warn("Download Page Exception: " + request.getUrl() + " Message: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void setThread(int threadNum) {

    }

    public Date getLastSpiderDownloadTime() {
        return lastSpiderDownloadTime;
    }
}
