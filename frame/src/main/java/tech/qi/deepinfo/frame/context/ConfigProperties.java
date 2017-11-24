package tech.qi.deepinfo.frame.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 通常, Handler 表示对一种 外部资源或者模块 的初始化和操作过程, 所以通常继承自AbstractVerticle,
 * 但是唯独 PropertyHandler
 */
public class ConfigProperties extends PropertyPlaceholderConfigurer {

    private static Map<String, String> ctxPropertiesMap;

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
        super.processProperties(beanFactoryToProcess, props);
        ctxPropertiesMap = new HashMap<>();
        for (Object key : props.keySet()) {
            String keyStr = key.toString();
            String value = props.getProperty(keyStr);
            ctxPropertiesMap.put(keyStr, value);
        }
    }

    public static String getProperty(String name) {
        return ctxPropertiesMap.get(name);
    }

    public static int getIntProperty(String name) {
        String pro = ctxPropertiesMap.get(name).trim();
        if( null == pro || "".equals(pro) ) {
            return Integer.MIN_VALUE;
        }else {
            return Integer.parseInt(pro);
        }
    }

    public static long getLongProperty(String name) {
        String pro = ctxPropertiesMap.get(name).trim();
        if( null == pro || "".equals(pro) ) {
            return Long.MIN_VALUE;
        }else {
            return Long.parseLong(pro);
        }
    }

    public static String getOrDefaultProperty(String name,String defaultValue) {
        return ctxPropertiesMap.getOrDefault(name,defaultValue);
    }


    public static void putProperty(String name,String value) {
        ctxPropertiesMap.put(name, value);
    }


}
