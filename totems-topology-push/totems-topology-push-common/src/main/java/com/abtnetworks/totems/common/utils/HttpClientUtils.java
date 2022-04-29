package com.abtnetworks.totems.common.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Author: myl
 */
public class HttpClientUtils {
    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class); // 日志记录

    private static final int TIMEOUT_TIME = 30*60*1000; //毫秒

    /**
     * post请求传输String参数
     * 例如：name=Jack&sex=1&type=2
     * Content-type:application/json
     *
     * @param url      url地址
     * @param strParam 参数
     *                 payload
     * @return
     */
    public static JSONObject httpPost(String url, String strParam) {
        // post请求返回结果
        CloseableHttpClient httpClient = HttpClients.createDefault();
        JSONObject jsonResult = null;
        HttpPost httpPost = new HttpPost(url);
        // 设置请求和传输超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(TIMEOUT_TIME).setConnectTimeout(TIMEOUT_TIME).build();
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.146 Safari/537.36");
        try {
            if (null != strParam) {
                StringEntity stringEntity = new StringEntity(strParam, "UTF-8");
//                stringEntity.setContentEncoding("UTF-8");
                httpPost.setEntity(stringEntity);
            }
            CloseableHttpResponse result = httpClient.execute(httpPost);
            String str = EntityUtils.toString(result.getEntity(), "utf-8");
            //把json字符串转换成json对象
            jsonResult = JSONObject.parseObject(str);
            if (result.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("Post请求提交失败(" + result.getStatusLine().getStatusCode() + "):" + url);
            }
        }
        catch (Exception e) {
            logger.error("post请求提交失败:" + url, e);
        } finally {
            httpPost.releaseConnection();
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonResult;
    }

    /**
     * 发送get请求
     *
     * @param url 路径
     * @return
     */
    public static JSONObject httpGet(String url) {
        JSONObject jsonResult = null;
        CloseableHttpClient client = HttpClients.createDefault();
        logger.debug("HTTPGET:" + url);
        HttpGet request = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(TIMEOUT_TIME).setConnectTimeout(TIMEOUT_TIME).build();
        request.setConfig(requestConfig);
        try {
            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String strResult = EntityUtils.toString(entity, "utf-8");
            jsonResult = JSONObject.parseObject(strResult);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("get请求提交失败:" + url);
            }
        } catch (IOException e) {
            logger.error("get请求提交失败:" + url, e);
        } finally {
            request.releaseConnection();
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonResult;
    }

    public static JSONObject httpDelete(String url) {
        JSONObject jsonResult = null;
        CloseableHttpClient client = HttpClients.createDefault();
        logger.debug("HTTPDelete:" + url);
        HttpDelete request = new HttpDelete(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(TIMEOUT_TIME).setConnectTimeout(TIMEOUT_TIME).build();
        request.setConfig(requestConfig);
        try {
            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String strResult = EntityUtils.toString(entity, "utf-8");
            jsonResult = JSONObject.parseObject(strResult);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("delete请求提交失败:" + url);
            }
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
            logger.error("delete请求提交失败:" + url, e);
        } finally {
            request.releaseConnection();
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonResult;
    }

    public static JSONObject httpPut(String url, String strParam) {
        JSONObject jsonResult = null;
        String responseContent = null;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(TIMEOUT_TIME).setConnectTimeout(TIMEOUT_TIME).build();
        httpPut.setConfig(requestConfig);
        httpPut.setHeader("Content-Type", "application/json");
        httpPut.setHeader("Accept", "*/*");
        try {
            if (null != strParam) {
                StringEntity stringEntity = new StringEntity(strParam, "utf-8");
                stringEntity.setContentEncoding("UTF-8");
                httpPut.setEntity(stringEntity);
            }
            CloseableHttpResponse response = client.execute(httpPut);
            responseContent = EntityUtils.toString(response.getEntity(), "utf-8");
            jsonResult = JSONObject.parseObject(responseContent);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("put请求提交失败:" + url);
            }
        } catch (IOException e) {
            logger.error("put请求提交失败:" + url, e);
        } finally {
            httpPut.releaseConnection();
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonResult;
    }

    public static JSONObject httpPutStream(String url, String strParam) {
        JSONObject jsonResult = null;
        String responseContent = null;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(TIMEOUT_TIME).setConnectTimeout(TIMEOUT_TIME).build();
        httpPut.setConfig(requestConfig);
        httpPut.setHeader("Content-Type", "application/octet-stream");
        try {
            if (null != strParam) {
                StringEntity stringEntity = new StringEntity(strParam);
                stringEntity.setContentEncoding("UTF-8");
                httpPut.setEntity(stringEntity);
            }
            CloseableHttpResponse response = client.execute(httpPut);
            responseContent = EntityUtils.toString(response.getEntity(), "utf-8");
            jsonResult = JSONObject.parseObject(responseContent);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("put请求提交失败:" + url);
            }
        } catch (IOException e) {
            logger.error("put请求提交失败:" + url, e);
        } finally {
            httpPut.releaseConnection();
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonResult;
    }

    public static String httpPost(String url, String data, Map<String, String> headerMap) {
        String response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        // 设置请求和传输超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(TIMEOUT_TIME).setConnectTimeout(TIMEOUT_TIME).build();
        httpPost.setConfig(requestConfig);
        if (headerMap != null) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        try {
            if (null != data) {
                StringEntity stringEntity = new StringEntity(data, "UTF-8");
                httpPost.setEntity(stringEntity);
            }
            CloseableHttpResponse result = httpClient.execute(httpPost);
            if (result.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.info("post请求提交不成功:" + url);
                logger.info("post data:" + data);
            }
            response = EntityUtils.toString(result.getEntity(), "utf-8");
        } catch (Exception e) {
            logger.info("post请求提交失败:" + url, e);
        } finally {
            httpPost.releaseConnection();
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    public static String httpGet(String url, Map<String, String> headerMap) {
        String response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        if (headerMap != null) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpGet.setHeader(entry.getKey(), entry.getValue());
            }
        }
        try {
            CloseableHttpResponse result = httpClient.execute(httpGet);
            if (result.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("get请求提交失败:" + url);
            }
            response = EntityUtils.toString(result.getEntity(), "utf-8");
        } catch (Exception e) {
            logger.error("get请求提交失败:" + url, e);
        } finally {
            httpGet.releaseConnection();
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }
}
