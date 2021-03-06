package tech.qi.deepinfo.crawler.spider;

import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.qi.deepinfo.frame.core.ServiceException;
import tech.qi.deepinfo.frame.module.spider.DownloaderPlugin;
import tech.qi.deepinfo.frame.module.spider.SpiderCons;
import tech.qi.deepinfo.frame.module.spider.SpiderPlugin;
import tech.qi.deepinfo.frame.module.spider.SpiderPluginHandler;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;


/**
 * @author qiwang
 * 一个调用器, 调用插件来完成下载和封装数据的任务, 兼顾下载和封装过程的异常处理
 * 插件只会传回来 正确值或者ServiceException
 * TODO, 执行出错的处理(重试机制, 以及异常处理, 通知机制, 日志)
 */
@ThreadSafe
@Component
public class PluginBasedDownloader extends AbstractDownloader {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    SpiderPluginHandler pluginHandler;

    @Override
    public Page download(Request request, Task task) {
        Page result = null;
        try {
            String pluginName = (String)request.getExtra(SpiderCons.PluginName);
            SpiderPlugin plugin = pluginHandler.getPlugin(pluginName);
            if( plugin instanceof DownloaderPlugin ){
                DownloaderPlugin downloaderPlugin = (DownloaderPlugin) plugin;
                result = downloaderPlugin.run(request);
            }else {
                throw new IllegalArgumentException("Invalid Download Plugin");
            }
        } catch (Exception e) {
            result = new Page();
            e.printStackTrace();
            result.setRawText(e.getLocalizedMessage());
            result.setStatusCode(500);
        }
        return result;
    }

    /**
     * 设置下载线程数量，也就是说，下载器原本是设计作为下载线程管理器的，但是本项目中，这只是一个壳。
     * @param threadNum
     */
    @Override
    public void setThread(int threadNum) {
        // Do Nothing
    }
}
