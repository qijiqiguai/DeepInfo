package tech.qi.deepinfo.frame.support;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by wangqi on 2017/6/16.
 */
public class FileUtil {

    public static void save2File(String input, String file) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(input);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String searchLineInFile(String key, String fileName){
        BufferedReader reader = null;
        String result = null;
        try {
            File file = getFile(fileName);
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                if(tempString.contains(key)){
                    return tempString;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return result;
    }

    public static String readFile(String fileName) {
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            File file = getFile(fileName);
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                sb.append(tempString + "\n");
                line++;
            }
            System.out.println(fileName + " 文件一共 " + line + " 行。。。");
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return sb.toString();
    }

    public static File getFile(String fileName) throws URISyntaxException {
        if(fileName.contains("classpath:")){
            ClassLoader classLoader = FileUtil.class.getClassLoader();
            URL url = classLoader.getResource(fileName.replaceAll("classpath:", ""));
            return new File(url.toURI().getPath());
        }else{
            return new File(fileName);
        }
    }
}
