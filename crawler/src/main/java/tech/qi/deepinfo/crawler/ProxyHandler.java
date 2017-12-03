package tech.qi.deepinfo.crawler;

import org.slf4j.Logger;

/**
 * 代理池管理
 * webmagic有自动检查代理的机制，最开始时会发送经常连不上情况
 * 启动时会去{java.io.tmpdir}中（windows下为：C:\Users\{当前用户名}\AppData\Local\Temp\webmagic）
 * 读取上次已经成功的代理配置文件lastUse.proxy,如果以前的代理不使用，需要删除lastUse.proxy文件
 * 如果采用定时去刷新代理设置，会导致经常连不上
 */
@Component
public class ProxyHandler extends AbstractHandler {

    private static Logger logger = LoggerFactory.getLogger(ProxyHandler.class);

    @Autowired
    ProxyConfigService proxyConfigService;

    @Value("${sys.node.name}")
    private String nodeId;

    //是否需要使用启动之前持久化的 Proxy 信息
    @Value("${sys.use.last.proxy}")
    private boolean isUseLastProxy;

    @Value("${sys.enable.proxy}")
    private boolean enableProxy;

    //采用自己的连接池（改写了webmagic的池）
    private ProxyPool proxyPool;

    public ProxyHandler() {
        super("ProxyHandler");
    }

//    @PostConstruct
    @Override
    public void init() {
        logger.info(this.getName() + " begin init...");
        if (enableProxy) {
            proxyPool = new ProxyPool(isUseLastProxy);
            proxyPool.addProxy(getProxyConfigs());
        } else {
            logger.warn("不启动代理");
        }
    }

    @Override
    public void reload() {
        if (enableProxy && proxyPool != null) {
            proxyPool.reloadProxy(getProxyConfigs());
        }
    }

    private String[][] getProxyConfigs() {
        List<ProxyConfig> list = proxyConfigService.selectEnabledByNodeId(nodeId);
        if (list == null || list.isEmpty()) {
            logger.warn("没有在DB配置的爬虫代理, 检查 NodeId和Enable状态 或者 确实没有代理");
            return null;
        } else {
            int size = list.size();
            logger.info(this.getName() + " Get Proxy Form DB, Proxy List Size: " + size);
            String[][] pcs = new String[size][];
            for (int i = 0; i < size; i++) {
                pcs[i] = new String[]{list.get(i).getServerIp(), String.valueOf(list.get(i).getPort())};
            }
            return pcs;
        }
    }

    public void disableProxy(String serverIp, String port) {
//        proxyConfigService.disbaleProxyByServerIpAndPort(serverIp, port); //如果代理出问题了, 那不是简单禁用而是
        proxyPool.disableProxy(serverIp, port);
        logger.warn("Disable proxy, Server Ip = " + serverIp + ", Port = " + port);
    }

    /**
     * 获取连接池
     *
     * @return
     */
    public HttpHost getProxy() {
        return proxyPool == null ? null : proxyPool.getProxy();
    }

    /**
     * 返回代理连接池
     *
     * @param host
     * @param statusCode
     */
    public void returnProxy(HttpHost host, int statusCode) {
        if (host != null && proxyPool != null) {
            logger.debug("回收代理池, serverIp:" + host.getHostName() + ", port:" + host.getPort() + ", statusCode=" + statusCode);
            //todo 需要重写,因为如果不是200类型，ProxyPool会设置delay时间的
            statusCode = Proxy.SUCCESS;
            proxyPool.returnProxy(host, statusCode);
        }
    }
}
