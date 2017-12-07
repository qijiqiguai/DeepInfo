package tech.qi.deepinfo.frame.module.spider;

import groovy.lang.GroovyClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tech.qi.deepinfo.frame.core.AbstractHandler;
import tech.qi.deepinfo.frame.core.Background;
import tech.qi.deepinfo.frame.core.LifecycleException;
import tech.qi.deepinfo.frame.support.FileUtil;
import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wangqi
 */
@Component
@ConditionalOnProperty(name="spider.enable", havingValue="true")
public class SpiderPluginHandler extends AbstractHandler implements Background {
    private Logger logger = LoggerFactory.getLogger(SpiderPluginHandler.class);

    private long lastRunAt = System.currentTimeMillis();

    @Value("${spider.plugin.base.path}")
    private String pluginBasePath;

    private final int RELOAD_MS = 15*1000;

    private final Map<String, SpiderPlugin> plugins = new ConcurrentHashMap<>();

    public SpiderPluginHandler() {
        super("SpiderPluginHandler");
    }

    public SpiderPlugin getPlugin(String name) {
        return plugins.get(name);
    }

    @PostConstruct
    @Override
    public void init() {
        if (null == pluginBasePath || "".equals(pluginBasePath.trim())) {
            throw new IllegalArgumentException("Plugin base path can't be empty.");
        }
        logger.info("Start Init SpiderPluginHandler @ " + pluginBasePath);
        reload();
    }

    @Override
    public void start() throws LifecycleException {

    }

    /**
     * 扫描 BasePath 文件夹下的所有文件并加载 Groovy 脚本
     */
    /**
     * 扫描 BasePath 文件夹下的所有文件并加载 Groovy 脚本
     */
    @Override
    public void reload() {
        ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader();
        GroovyClassLoader groovyLoader = new GroovyClassLoader(parentClassLoader);

        String basePath = pluginBasePath;
        logger.debug("Load Plugin Scripts, BasePath=" + basePath);
        String currentFile = null;
        try {
            File dir = FileUtil.getFile(basePath);
            logger.debug("Load Plugin Scripts, loaded path=" + dir.getAbsolutePath());
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException(basePath + " is not directory!");
            } else if (dir.isDirectory()) {
                String[] fileList = dir.list();
                for( int i=0; i<fileList.length; i++ ){
                    if (fileList[i].endsWith(".groovy")) {
                        currentFile = fileList[i];
                        String pluginPath = basePath + "/" + currentFile;
                        File readFile = FileUtil.getFile(pluginPath);
                        if (!readFile.isDirectory()) {
                            long lastModifiedGap = System.currentTimeMillis() - readFile.lastModified();
                            if( lastModifiedGap <= RELOAD_MS || !plugins.containsKey(currentFile)){
                                plugins.put(
                                        // 删除 .groovy 后缀
                                        currentFile.substring(0, currentFile.lastIndexOf(".")),
                                        // 新建对象的实例并存储
                                        (SpiderPlugin)groovyLoader.parseClass(readFile).newInstance()
                                );
                                logger.info("Load plugin:" + currentFile + " success ...");
                            }else {
                                logger.debug("Do not load plugin: " + currentFile +
                                        ", Last modified time gap = " + lastModifiedGap);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn( "Load Groovy Script Failed @ " + basePath + ": " + currentFile, e );
        }
    }

    @Override
    public void stopMe() {

    }

    @Override
    public void destroy() throws LifecycleException {
        this.pluginBasePath = null;
        this.plugins.clear();
    }

    @Override
    public Status getStatus() {
        return null;
    }

    @Override
    public void backgroundProcess() {
        reload();
    }

    @Override
    public long getLastRunTime() {
        return lastRunAt;
    }

    @Override
    public void setLastRunTime(long time) {
        this.lastRunAt = time;
    }

    @Override
    public long getRunIntervalMs() {
        return this.RELOAD_MS;
    }
}
