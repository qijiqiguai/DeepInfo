package groovy

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import us.codecraft.webmagic.Page

/**
 */
class Ipcn_Proxy implements DownloaderPlugin {

    @Override
    Page run(SpiderJob job) {
        Page resultPage = new Page()
        List list = spiderIpcn(job.getBatchId())
        resultPage.putField(Constants.SPIDER_PROXY, list)
        return resultPage
    }

    /**
     * http://proxy.ipcn.org/proxylist.html 1页
     */
    private static List spiderIpcn(String batchId) {
        List<ProxyInfo> proxyList = new ArrayList<ProxyInfo>();
        try {
            String url = "http://proxy.ipcn.org/proxylist.html";
            String result = HttpDownloader.instance().download(url, null);

            Document doc = Jsoup.parse(result);
            Element element = doc.select("table tbody").last();

            String value = element.select("tr td pre").first().text();

            String[] values = value.split("\\n");
            for (int i = 0; i < values.length; i++) {
                if (values[i].contains(":")) {
                    ProxyInfo proxyBean = new ProxyInfo();
                    proxyBean.setIp(values[i].split(":")[0]);
                    proxyBean.setPort(Integer.decode(values[i].split(":")[1]));
                    proxyBean.setCreatedDate(new Date())
                    proxyBean.setBatchId(batchId)
                    proxyList.add(proxyBean);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return proxyList;
    }
}