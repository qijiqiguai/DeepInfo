package groovy

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import us.codecraft.webmagic.Page

/**
 */
class Sixip_Proxy implements DownloaderPlugin {

    @Override
    Page run(SpiderJob job) {
        Page resultPage = new Page()
        List list = spider66ip(job.getBatchId())
        resultPage.putField(Constants.SPIDER_PROXY, list)
        return resultPage
    }

    /**
     * http://www.66ip.cn 500多页
     */

    private static List spider66ip(String batchId) {
        List<ProxyInfo> proxyList = new ArrayList<ProxyInfo>();
        for (int i = 1; i <= 10; i++) {
            try {
                String url = "http://www.66ip.cn/" + i + ".html";
                String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36"
                String result = HttpDownloader.instance().download(url, null,userAgent);

                Document doc = Jsoup.parse(result);
                Elements elements = doc.select("table tbody").get(2).children();
                //去掉第一个
                for (int j = 1; j < elements.size(); j++) {
                    Element element = elements.get(j);
                    ProxyInfo proxyBean = new ProxyInfo();
                    proxyBean.setIp(element.select("tr td").get(0).text());
                    proxyBean.setPort(Integer.decode(element.select("tr td").get(1).text()));
                    proxyBean.setCreatedDate(new Date())
                    proxyBean.setBatchId(batchId)
                    proxyList.add(proxyBean);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return proxyList;
    }

}
