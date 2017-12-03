package groovy

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import us.codecraft.webmagic.Page

/**
 */
class Xicidaili_Proxy implements DownloaderPlugin {

    @Override
    Page run(SpiderJob job) {
        Page resultPage = new Page()
        List list = spiderXicidaili(job.getBatchId())
        resultPage.putField(Constants.SPIDER_PROXY, list)
        return resultPage
    }

    /**
     * http://www.xicidaili.com/nn/1 1000多页
     */
    private static List spiderXicidaili(String batchId) {
        List<ProxyInfo> proxyList = new ArrayList<ProxyInfo>();
        for (int i = 1; i <= 10; i++) {
            try {
                String url = "http://www.xicidaili.com/nn/" + i;
                String result = HttpDownloader.instance().download(url, null);

                Document doc = Jsoup.parse(result);
                Elements elements = doc.select("table tbody").get(2).children();
                for (int j = 0; j < elements.size(); j++) {
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
