package tech.qi.deepinfo.frame.support;

/**
 * 错误代码定义
 * 负数表示 errors and exceptions
 * 9 Used for extension, say 91, 92 ... 98. But 99 will be reserved for future use.
 * 2 storage related
 * 3 system status
 * 4 plugin status
 * 8 used for unmatched system condition
 *
 * @author wangqi
 */
public class StatusCode {
    public final static int STORE_DATA_EXCEPTION = -2001;

    public final static int SUCCESS = 3001;
    public final static int ERROR = -3002;
    public final static int NOT_FOUND = -3003;
    public final static int CONFIG_WRONG = -3004;
    public final static int SPIDER_JOB_FAIL = -3005;
    public final static int SPIDER_STATUS_ERROR = -3006;

    public final static int GET_DATA_OK = 4001;
    public final static int DOWNLOAD_ERROR = -4002;
    public final static int API_FORMAT_ERROR = -4003;
    public final static int API_STATUS_ERROR = -4004;
    public final static int HTML_FORMAT_ERROR = -4005;
    public final static int UNACCEPTED_HTTP_CODE = -4006;
    public final static int NEED_RENEW = -4007;

    public final static int TASK_TYPE_NOT_FOUND = -8001;
}
