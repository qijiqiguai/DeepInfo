package tech.qi.deepinfo.frame.module.spider;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpHost;
import us.codecraft.webmagic.Request;

/**
 * @author qiwang
 *
 * 最小的任务单位，即执行一条数据获取。
 * 在执行的时候，需要设置 Cookie + Header + Proxy, 这样插件里就包含了完善的执行信息，而不需要向外围要什么。
 * 这里包含了URL信息。
 *
 *
 * Scheduler中无法对 Request 做序列化存储, 然后再反序列话 Request 本身 ( 而不是子类 )。
 * 所以功能性字段只能存储在 Request.extras 中。
 * 同时, 反序列化之后, 得到的也只是 Request 本身(无法强制转换), 所以从 Scheduler 中拿到 Request 之后, 还是需要新建 Task。
 */
public class AtomTask extends Request {

    /**
     * @param batchId 用于查找来源
     * @param taskUid 用于记录执行日志
     * @param url 核心的下载路径，不一定是现实的Http链接。
     * @param plugin 插件名
     * @param extInfo 这个值可以是JSONObject, 因为入口信息可能比较丰富。可能有Token，用户名密码，Cookie等
     */
    public AtomTask(String batchId, String taskUid, String url, String plugin, Object extInfo){
        this.setUrl(url);
        this.putExtra(SpiderCons.PluginName, plugin);
        this.putExtra(SpiderCons.BatchId, batchId);
        this.putExtra(SpiderCons.TaskUid, taskUid);
        this.putExtra(SpiderCons.ExtInfo, extInfo);
    }

    public AtomTask(Request request){
        this.setUrl(request.getUrl());
        this.setExtras(request.getExtras());
    }

    public void setProxy(HttpHost proxy) {
        this.putExtra(SpiderCons.Proxy, proxy);
    }

    public HttpHost getProxy() {
        Object obj = this.getExtra(SpiderCons.Proxy);
        if(null == obj ){
            return null;
        }else {
            return (HttpHost)obj;
        }
    }

    public void setCookie(JSONObject cookies) {
        this.putExtra(SpiderCons.Cookie, cookies);
    }

    public JSONObject getCookie() {
        Object obj = this.getExtra(SpiderCons.Cookie);
        if(null == obj ){
            return null;
        }else {
            return (JSONObject)obj;
        }
    }

    public void setHeader(JSONObject header) {
        this.putExtra(SpiderCons.Header, header);
    }

    public JSONObject getHeader() {
        Object obj = this.getExtra(SpiderCons.Header);
        if(null == obj ){
            return null;
        }else {
            return (JSONObject)obj;
        }
    }

    @Override
    public String toString(){
        return JSONObject.toJSONString(this);
    }
}
