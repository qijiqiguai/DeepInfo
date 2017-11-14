package tech.qi.deepinfo.frame.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author wangqi
 * @date 2017/11/14 下午8:09
 */
public class HttpUtil {
    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    public static Map<String, String[]> getURLParamsMap(final String query, final String encoding) {
        String url = query;
        if( url.contains("?") ){
            url = url.split("\\?")[1];
        }
        String enc = null==encoding ? "UTF-8" : encoding;

        Map paramsMap = new HashMap();
        if (url != null && url.length() > 0) {
            int ampersandIndex, lastAmpersandIndex = 0;
            String subStr, param, value;
            String[] paramPair, values, newValues;
            do {
                ampersandIndex = url.indexOf('&', lastAmpersandIndex) + 1;
                if (ampersandIndex > 0) {
                    subStr = url.substring(lastAmpersandIndex, ampersandIndex - 1);
                    lastAmpersandIndex = ampersandIndex;
                } else {
                    subStr = url.substring(lastAmpersandIndex);
                }
                paramPair = subStr.split("=");
                param = paramPair[0];
                value = paramPair.length == 1 ? "" : paramPair[1];
                try {
                    value = URLDecoder.decode(value, enc);
                } catch (UnsupportedEncodingException ignored) {
                    logger.error("Get URL Parameter Error!", ignored);
                }
                if (paramsMap.containsKey(param)) {
                    values = (String[])paramsMap.get(param);
                    int len = values.length;
                    newValues = new String[len + 1];
                    System.arraycopy(values, 0, newValues, 0, len);
                    newValues[len] = value;
                } else {
                    newValues = new String[] { value };
                }
                paramsMap.put(param, newValues);
            } while (ampersandIndex > 0);
        }
        return paramsMap;
    }
}
