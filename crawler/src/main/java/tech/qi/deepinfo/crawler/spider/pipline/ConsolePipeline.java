package tech.qi.deepinfo.crawler.spider.pipline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;


/**
 * 不做不同平台的结果集合并之后在批量存储, 因为当一个平台挂掉之后, 如果需要合并存储, 那么其他平台永远也无法更新了, 这样影响范围较大.
 * 而这样做唯一获得的好处就是效率稍高, 因为是批量写入的, 其他没什么好处.
 * @author wangqi
 */
@Component
public class ConsolePipeline implements Pipeline {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void process(ResultItems resultItems, Task task) {
        logger.info("Start Pipeline Result: " + resultItems.getRequest().getExtra("TaskID"));
    }
}