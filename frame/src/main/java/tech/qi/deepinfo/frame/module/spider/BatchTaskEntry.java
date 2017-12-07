package tech.qi.deepinfo.frame.module.spider;

import com.alibaba.fastjson.JSONObject;
import us.codecraft.webmagic.Request;

/**
 * @author qiwang
 *
 * 用于定义初始化的任务入口，在这个入口执行完成之后会生成其他任务。
 * 例如给定一个入口页，然后解析出子任务的URL。
 * 又如给定一个ID列表，生成需要抓取的一系列子任务。
 *
 * 这个任务本身可能是需要周期性执行的。
 * 如果是周期性任务，需要从DB中重建这个Reqeust。
 */
public class BatchTaskEntry extends Request {

    public BatchTaskEntry(String platform, String taskType, String batchId){
        //插件名是 平台名 + 任务名 + Entry拼接起来的
        String pluginName = platform.toLowerCase() + "_" + taskType + "_" + "Entry.groovy";
        pluginName = pluginName.replaceFirst(
                pluginName.substring(0, 1),
                pluginName.substring(0, 1).toUpperCase()
        );
        this.putExtra(SpiderCons.PluginName, pluginName);
        this.putExtra(SpiderCons.Platform, platform);
        this.putExtra(SpiderCons.TaskType, taskType);
        this.putExtra(SpiderCons.BatchId, batchId);
    }

    public BatchTaskEntry(Request request){
        this.setUrl(request.getUrl());
        this.setExtras(request.getExtras());
    }

    public String getPlatform() {
        Object obj = this.getExtra(SpiderCons.Platform);
        if(null == obj ){
            return null;
        }else {
            return obj.toString();
        }
    }

    public String getTaskType(){
        Object obj = this.getExtra(SpiderCons.TaskType);
        if(null == obj ){
            return null;
        }else {
            return obj.toString();
        }
    }

    @Override
    public String toString(){
        return JSONObject.toJSONString(this);
    }
}
