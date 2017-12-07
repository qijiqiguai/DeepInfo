import tech.qi.deepinfo.frame.module.spider.DownloaderPlugin
import us.codecraft.webmagic.Page
import us.codecraft.webmagic.Request

/**
 * Created by wangqi on 2017/12/7 下午8:28.
 */

class Xueqiu_people_entry implements DownloaderPlugin {
    final String entryUrl = "https://xueqiu.com/people"

    @Override
    Page run(Request task) {
        Page resultPage = new Page()
        return resultPage
    }
}