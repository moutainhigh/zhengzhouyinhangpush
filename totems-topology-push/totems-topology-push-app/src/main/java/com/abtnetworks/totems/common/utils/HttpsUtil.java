package com.abtnetworks.totems.common.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @desc    https请求工具类
 * @author liuchanghao
 * @date 2021-02-22 10:21
 */
public class HttpsUtil {
    public static CloseableHttpClient createSSLClientDefault() throws Exception{
        //如果下面的方法证书还是不过，报错的话试试下面第二种
        /* SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy(){
        //信任所有
        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        return true;
        }
        }).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
        return HttpClients.custom().setSSLSocketFactory(sslsf).build();*/

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
                NoopHostnameVerifier.INSTANCE);
        return HttpClients.custom().setSSLSocketFactory(sslsf).build();

    }

    public static String httpsPost(String url, String json) throws Exception{

        CloseableHttpClient hp = createSSLClientDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type","application/json");
        httpPost.setEntity(new StringEntity(json));
        CloseableHttpResponse response = hp.execute(httpPost);
        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity,"UTF-8");
        hp.close();
        return content;
    }

    public static void main(String[] args){
        try {
            Map<String,String> json = new HashMap<String,String>();
          String s =   HttpsUtil.httpsPost("https://192.168.215.126:443/topology-api/sdnx/getFile?access_token=2989f8cc-d20f-47cb-8dfe-2c931ea7d5a1","");
       System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
