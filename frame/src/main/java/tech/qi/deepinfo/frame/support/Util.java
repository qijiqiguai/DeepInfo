package tech.qi.deepinfo.frame.support;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author qiwang
 * @time 16/5/31
 */
public class Util {
    private static Logger logger = LoggerFactory.getLogger(Util.class);

    public static String getExceptionMessage(Exception e) {
        StringBuffer result =  new StringBuffer(
                "Type:" + e.getClass().getTypeName() + " Message: " + e.getLocalizedMessage() + "\n");
        if( e.getStackTrace().length > 0 ) {
            for(StackTraceElement st : e.getStackTrace() ) {
                String message = st.getClassName() + ":" + st.getMethodName() + ":" + st.getLineNumber() + "\n";
                result.append(message);
            }
        }
        return result.toString();
    }

    public static long stringToLong(String input){
        if( input.toLowerCase().endsWith("w")  ){
            return (long)Double.parseDouble(input.toLowerCase().replace("w",""))*10000;
        }if( input.toLowerCase().endsWith("k") ){
            return (long)Double.parseDouble(input.toLowerCase().replace("k",""))*1000;
        }if( input.toLowerCase().endsWith("万")  ){
            return (long)Double.parseDouble(input.toLowerCase().replace("万",""))*10000;
        }if( input.contains(",")  ){
            return (long)Double.parseDouble(input.replace(",",""));
        }else {
            return Long.valueOf(input);
        }
    }

    public static boolean isEmpty(String s){
        if(s==null||s.trim().isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    public static boolean isEmpty(List s){
        if(s==null || s.size()==0){
            return true;
        }else{
            return false;
        }
    }

    public static InetAddress getInetAddress(){
        try{
            return InetAddress.getLocalHost();
        }catch(UnknownHostException e){
            logger.error("Unknown Host", e);
        }
        return null;

    }

    public static String getHostIp(InetAddress netAddress){
        if(null == netAddress){
            return null;
        }
        //get the ip address
        String ip = netAddress.getHostAddress();
        return ip;
    }

    public static String getHostName(InetAddress netAddress){
        if(null == netAddress){
            return null;
        }
        //get the host address
        String name = netAddress.getHostName();
        return name;
    }

    public static int randomInt(int bound){
        // 定义随机类
        Random random = new Random();
        // 返回[0, bound)集合中的整数，注意不包括右界
        int result = random.nextInt(bound);
        return result + 1;
    }
}
