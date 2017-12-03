package groovy

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import us.codecraft.webmagic.Page

/**
 */
class Cz88_Proxy implements DownloaderPlugin {

    @Override
    Page run(SpiderJob job) {
        Page resultPage = new Page()
        List list = spiderCz88(job.getBatchId())
        resultPage.putField(Constants.SPIDER_PROXY, list)
        return resultPage
    }

    /**
     * http://www.cz88.net/proxy/ 1页
     */
    private static List spiderCz88(String batchId) {
        List<ProxyInfo> proxyList = new ArrayList<ProxyInfo>();
        try {
            String url = "http://www.cz88.net/proxy/";
            String result = HttpDownloader.instance().download(url, null);

            Document doc = Jsoup.parse(result);
            Elements elements = doc.select("ul").last().children();
            //去掉第一个
            for (int j = 1; j < elements.size(); j++) {
                Element element = elements.get(j);
                ProxyInfo proxyBean = new ProxyInfo();
                proxyBean.setIp(element.select("li div.ip").text());
                proxyBean.setPort(Integer.decode(element.select("li div.port").text()));
                proxyBean.setCreatedDate(new Date())
                proxyBean.setBatchId(batchId)
                proxyList.add(proxyBean);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return proxyList;
    }
}
