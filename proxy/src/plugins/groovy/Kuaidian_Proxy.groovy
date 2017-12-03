package groovy

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import us.codecraft.webmagic.Page

/**
 */
class Kuaidian_Proxy implements DownloaderPlugin {

    @Override
    Page run(SpiderJob job) {
        Page resultPage = new Page()
        List list = spiderKuaidaili(job.getBatchId())
        resultPage.putField(Constants.SPIDER_PROXY, list)
        return resultPage
    }

    /**
     * http://www.kuaidaili.com 一共10页
     */
    private List spiderKuaidaili(String batchId) {
        List<ProxyInfo> proxyList = new ArrayList<ProxyInfo>();
        for (int i = 1; i <= 10; i++) {
            try {
                String url = "http://www.kuaidaili.com/proxylist/" + i + "/";
                String result = HttpDownloader.instance().download(url, null);

                Document doc = Jsoup.parse(result);
                Elements elements = doc.select("table tbody").first().children();

                for (Element element : elements) {
                    ProxyInfo proxyInfo = new ProxyInfo();
                    proxyInfo.setIp(element.select("tr td").get(0).text());
                    proxyInfo.setPort(Integer.decode(element.select("tr td").get(1).text()));
                    proxyInfo.setCreatedDate(new Date())
                    proxyInfo.setBatchId(batchId)
//                    if (!ProxyUtil.isConnectionTimeOut(proxyInfo)) {
                    proxyList.add(proxyInfo);
//                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return proxyList;
    }
}
