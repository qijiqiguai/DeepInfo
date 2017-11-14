package tech.qi.deepinfo.frame.support;

/**
 *
 * @author wangqi
 * @date 2017/11/14 下午7:38
 */
public class Constants {

    public final static String UID = "uid:";
    /**
     * [List] 存放 JobLog 的队列
     */
    public final static String SPIDER_JOB_LOG_QUEUE = "spider_job_log_queue";
    /**
     * [Prefix + [List]: 因为有多个优先级, 每个优先级一个列表] 任务队列：
     * 存放需要执行的任务队列，根据优先级不同建立不同队列，值为request的url
     */
    public static final String SPIDER_SCHEDULER_TASKS = "spider_tasks_todo_url:";
    /**
     * [Map] 任务具体内容：存放所有任务的具体执行内容 key为任务索引的hash值，value为整个 request 的 JSON
     */
    public static final String SPIDER_SCHEDULER_TASK_DETAILS = "spider_task_details:";
    /**
     * Prefix + [List] 任务索引：存放所有任务索引 值为request的url, 用于去重
     */
    public static final String SPIDER_SCHEDULER_HISTORY_URL = "spider_task_history_url:";

    public static final String CACHE_KEY_SEPARATOR = ":";
}
