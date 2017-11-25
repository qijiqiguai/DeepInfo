package tech.qi.deepinfo.frame.module.spider;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.qi.deepinfo.frame.core.ServiceException;
import tech.qi.deepinfo.frame.core.StatusCode;
import tech.qi.deepinfo.frame.support.Util;
import us.codecraft.webmagic.utils.HttpConstant;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

/**
 * @author qiwang
 * @time 16/7/6
 */
public class HttpDownloader {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private volatile static HttpDownloader singleton;
    private final int timeOutMs = 5000;
    private final Set<Integer> defaultAcceptStatCode = Sets.newHashSet(200);


    private HttpDownloader(){ }

    public static HttpDownloader instance() {
        if (singleton == null) {
            synchronized (HttpDownloader.class) {
                if (singleton == null) {
                    singleton = new HttpDownloader();
                }
            }
        }
        return singleton;
    }

    private CloseableHttpClient getHttpClient(
            String domain ,String userAgent, boolean isUseGzip, Map<String, String> cookie) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        Registry<CookieSpecProvider> reg = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.BROWSER_COMPATIBILITY, new BrowserCompatSpecFactory())
                .register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())
                .register("easy", new BestMatchSpecFactory())
                .build();
        SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setTcpNoDelay(true).build();
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).build();
        httpClientBuilder.setDefaultSocketConfig(socketConfig)
                .setDefaultCookieSpecRegistry(reg)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(new BasicCookieStore())
                .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true));

        if (userAgent != null) {
            httpClientBuilder.setUserAgent(userAgent);
        } else {
            /*
             * 不能设置默认的 UserAgent, 可能导致数据不对. 要根据不同平台来实际设置
             * Mozilla/5.0 (Linux; U; Android 5.1.1; zh-cn; Build/KTU84P) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30
             */
            httpClientBuilder.setUserAgent("");
        }

        if (isUseGzip) {
            httpClientBuilder.addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
                String accEc = "Accept-Encoding";
                if ( !request.containsHeader(accEc) ) {
                    request.addHeader(accEc, "gzip");
                }
            });
        }

        if( null != cookie && cookie.size()>0 ){
            CookieStore cookieStore = new BasicCookieStore();
            for (Map.Entry<String, String> cookieEntry : cookie.entrySet()) {
                BasicClientCookie basicClientCookie =
                        new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
                basicClientCookie.setDomain(domain);
                cookieStore.addCookie(basicClientCookie);
            }
            httpClientBuilder.setDefaultCookieStore(cookieStore);
        }

        return httpClientBuilder.build();
    }

    /**
     * @param method 只能是 "get"或者是"post"
     */
    protected HttpUriRequest getHttpRequest(
            String method, String url, Map<String, String> headers,
            HttpHost proxy, NameValuePair[] postParameters, byte[] bytesParam
    ) {
        RequestBuilder requestBuilder;
        //default get
        if (method == null || method.equalsIgnoreCase(HttpConstant.Method.GET)) {
            requestBuilder = RequestBuilder.get();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.POST)) {
            requestBuilder = RequestBuilder.post();
            if (postParameters != null && postParameters.length > 0) {
                requestBuilder.addParameters(postParameters);
            }
            if (bytesParam != null && bytesParam.length!= 0){
                ByteArrayEntity byteArrayEntity = new ByteArrayEntity(bytesParam);
                requestBuilder.setEntity(byteArrayEntity);
            }
        }else {
            throw new IllegalArgumentException("Illegal HTTP Method " + method);
        }

        requestBuilder.setUri(url);

        if (headers != null) {
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
            }
        }

        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.BEST_MATCH)
                .setConnectionRequestTimeout(timeOutMs).setSocketTimeout(timeOutMs).setConnectTimeout(timeOutMs);

        if (proxy != null) {
            requestConfigBuilder.setProxy(proxy);
        }
        requestBuilder.setConfig(requestConfigBuilder.build());
        return requestBuilder.build();
    }

    /**
     * 在此之前都是下载开始之前的相关初始化
     * @param url url
     * @param proxy proxy
     */
    public String download( String url, HttpHost proxy ){
        return download(url, proxy,null, null, null, null,
                false, null, null, null, null,null);
    }

    public String download( String url, HttpHost proxy ,String userAgent){
        return download(url, proxy, null, null, null, userAgent,
                false, null, null, null, null,null);
    }

    public String download (
            String url,  HttpHost proxy, String domain,
            Map<String, String> headers, Map<String, String> cookie, String userAgent, boolean isUseGzip,
            String charset, Set<Integer> acceptStatCode, String method, NameValuePair[] postParameters,byte[] bytes
    ) {
        logger.debug("Downloading  Url: " + url + "StartTime:" + System.currentTimeMillis());
        CloseableHttpResponse httpResponse = null;
        int statusCode;

        try {
            HttpUriRequest httpUriRequest = getHttpRequest(method, url, headers, proxy, postParameters,bytes);
            httpResponse = getHttpClient(domain, userAgent, isUseGzip, cookie).execute(httpUriRequest);
            statusCode = httpResponse.getStatusLine().getStatusCode();

            if(statusAccept(acceptStatCode, statusCode)) {
                logger.debug("Downloading  Url Finished: " + url + "EndTime:" + System.currentTimeMillis());
                return handleResponse(charset, httpResponse);
            } else {
                logger.debug("Status Code Error:" + statusCode
                        + " URL:" + url + " Content:" + JSON.toJSONString(httpResponse));
                throw new ServiceException(
                        StatusCode.UNACCEPTED_HTTP_CODE,
                        url + " UnAccepted HTTP Code:" + statusCode,
                        JSON.toJSONString(httpResponse)
                );
            }
        } catch (IOException e) {
            logger.debug("download page " + url + " error", e);
            throw new ServiceException(
                StatusCode.DOWNLOAD_ERROR,
                url + " Error:" + e.getMessage(),
                Util.getExceptionMessage(e)
            );
        } finally {
            try {
                if (httpResponse != null) {
                    //ensure the connection is released back to pool
                    EntityUtils.consume(httpResponse.getEntity());
                }
            } catch (IOException e) {
                logger.warn("close response fail", e);
            }
        }
    }

    protected boolean statusAccept(Set<Integer> acceptStatCode, int statusCode) {
        if( null == acceptStatCode ){
            return defaultAcceptStatCode.contains(statusCode);
        }else {
            return acceptStatCode.contains(statusCode);
        }
    }

    protected String handleResponse(String charset, HttpResponse httpResponse) throws IOException {
        String content;
        // TODO, use tools cpdetector for auto content decode
        if (charset == null) {
            logger.debug("Charset autodetect failed, use {} as charset. " +
                    "Please specify charset", Charset.defaultCharset());
            content = new String(IOUtils.toByteArray(httpResponse.getEntity().getContent()));
        } else {
            content = IOUtils.toString(httpResponse.getEntity().getContent(), charset);
        }
        return content;
    }
}