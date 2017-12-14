import tech.qi.deepinfo.frame.module.spider.AbstractDownloaderPlugin
import tech.qi.deepinfo.frame.module.spider.HttpDownloader
import us.codecraft.webmagic.Page
import us.codecraft.webmagic.Request

/**
 * Created by wangqi on 2017/12/7 下午8:28.
 */

class xueqiu_people_entry extends AbstractDownloaderPlugin {
    final String entryUrl = "https://xueqiu.com/people"

    @Override
    Page run(Request task) {
        Page resultPage = new Page()
        String page = HttpDownloader.instance().download(entryUrl, null)
        resultPage.setRawText(page)
        resultPage.setRequest(task)
        resultPage.setStatusCode(200)
        resultPage.setUrl(entryUrl)
        return resultPage
    }

}