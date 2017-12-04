package tech.qi.deepinfo.crawler.spider.pipline;

import com.alibaba.fastjson.JSON;
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
    @Override
    public void process(ResultItems resultItems, Task task) {
        System.out.println("Start Pipeline Result: " + JSON.toJSONString(resultItems));
    }
}
