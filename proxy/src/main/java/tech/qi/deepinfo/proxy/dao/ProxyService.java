package tech.qi.deepinfo.proxy.dao;



/**
 */
@Service
public class ProxyService {
    @Autowired
    ProxyInfoMapper proxyInfoMapper;
    @Autowired
    ProxyConfigMapper proxyConfigMapper;

    public void saveProxyInfoList(List<ProxyInfo> proxyInfoList) {
        proxyInfoMapper.insertBatch(proxyInfoList);
    }

    public void saveProxyInfo(ProxyInfo proxyInfo) {
        proxyInfoMapper.insert(proxyInfo);
    }
    public void saveProxyConfig(ProxyInfo proxyInfo) {
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setPort(proxyInfo.getPort());
        proxyConfig.setServerIp(proxyInfo.getIp());
        proxyConfig.setEnabled(true);
        proxyConfig.setNodeId("scrapper01");
        proxyConfig.setName("SSH代理");
        proxyConfigMapper.insert(proxyConfig);
    }

    public void cleanProxyInfo(){
        proxyConfigMapper.deleteAll();
        proxyInfoMapper.deleteAll();
    }
}
