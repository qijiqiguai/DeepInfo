package tech.qi.deepinfo.proxy.core;


import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Component
public class DownloadPluginHandler extends AbstractHandler {
    private static Logger logger = LoggerFactory.getLogger(DownloadPluginHandler.class);

    @Value("${spider.plugin.base.path}")
    private String pluginBasePath;

    @Value("${spider.env.reload.time}")
    private int envReloadTime;

    private static final Map<String, Class> plugins = new ConcurrentHashMap<>();

    public DownloadPluginHandler() {
        super("DownloadPluginHandler");
    }

    public Class getPlugin(String name) {
        return plugins.get(name);
    }


    @Override
    public void init() {
        reload();
    }

    /**
     * 扫描 BasePath 文件夹下的所有文件并加载 Groovy 脚本
     */
    @Override
    public void reload() {
        ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader();
        GroovyClassLoader groovyLoader = new GroovyClassLoader(parentClassLoader);

        String basePath = pluginBasePath + "/groovy";
        logger.debug("Load Plugin Scripts, BasePath=" + basePath);
        File dir = new File(basePath);

        if (!dir.isDirectory()) {
            logger.error( "Plugin Base Path Should Be a Directory, Or Can't Access For Some Reason" );
            System.exit( -1 ); //如果载入不了插件, 那么直接退出, 更不不用跑
        } else if (dir.isDirectory()) {
            String[] fileList = dir.list();
            IntStream.range(0, fileList.length).forEach(i -> {
                if (fileList[i].endsWith("groovy")) {
                    String pluginPath = basePath + "/" + fileList[i];
                    File readFile = new File(pluginPath);
                    if (!readFile.isDirectory()) {
                        try {
                            long lastModifiedGap = System.currentTimeMillis() - readFile.lastModified();
                            if (lastModifiedGap <= envReloadTime || !plugins.containsKey(fileList[i])) {
                                plugins.put(fileList[i], groovyLoader.parseClass(readFile));
                                logger.info("Load plugin:" + fileList[i] + " success ...");
                            } else {
                                logger.debug("Do not load plugin: " + fileList[i] + ", Last modified time gap = " + lastModifiedGap);
                            }
                        } catch (Exception e) {
                            logger.warn("Load Groovy Script Failed: " + pluginPath, e);
                        }
                    }
                }
            });
        }
    }
}
