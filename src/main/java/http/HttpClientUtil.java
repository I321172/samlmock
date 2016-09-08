package http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpClientUtil
{
    public HttpClientBean fetchWeb(String url) throws Exception
    {
        return fetchWeb(url, true);
    }

    public HttpClientBean fetchWeb(String url, boolean needProxy) throws Exception
    {
        HttpClientBean httpClientBean = new HttpClientBean(url);
        return fetchWeb(httpClientBean, needProxy);
    }

    public HttpClientBean fetchWeb(String url, String proxy) throws Exception
    {
        return fetchWeb(new HttpClientBean(url), proxy);
    }

    public HttpClientBean fetchWeb(HttpClientBean httpClientBean) throws Exception
    {
        return fetchWeb(httpClientBean, true);
    }

    public HttpClientBean fetchWeb(HttpClientBean httpClientBean, String customProxy) throws Exception
    {
        return fetchWeb(httpClientBean, customProxy != null, customProxy, true);
    }

    /**
     * Provided to handle without proxy scenario like fetch rot ip
     * DC13QAAUTOCANDCFAPP01.lab.od.sap.biz
     */
    public HttpClientBean fetchWeb(HttpClientBean httpClientBean, boolean isNeedProxy) throws Exception
    {
        return fetchWeb(httpClientBean, isNeedProxy, null, true);
    }

    public HttpClientBean fetchWeb(HttpClientBean httpClientBean, boolean isNeedProxy, String customProxy,
            boolean isDisableRedirect) throws Exception
    {
        httpClientBean.clearResponseContentBeforeFetch();
        Map<String, String> paras = httpClientBean.getRequestParas();
        String url = httpClientBean.getUrl();
        String encoding = httpClientBean.getEncoding();
        if (url == null)
        {
            throw new Exception("Must specify Url! ");
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig httpConfig = getRequestConfig(isNeedProxy, customProxy, isDisableRedirect);
        HttpRequestBase method = null;
        switch (httpClientBean.getMethod())
        {
            case Get:
                url = appendParasToUrl(url, paras, encoding);
                method = new HttpGet(url);
                break;
            case Delete:
                url = appendParasToUrl(url, paras, encoding);
                method = new HttpDelete(url);
                break;
            case Post:
                // for SAML, must set below content type
                // String contentType =
                // SSOProperties.getProperty("content.type.url.encoded");
                // headers.put("Content-Type", contentType);
                HttpEntity entity = this.getHttpEntity(httpClientBean);
                if (entity == null)
                {
                    url = appendParasToUrl(url, paras, encoding);
                }
                method = new HttpPost(url);
                ((HttpPost) method).setEntity(entity);
                break;
        }
        method.setConfig(httpConfig);
        addHeader(method, httpClientBean);

        CloseableHttpResponse response = httpClient.execute(method);
        httpClientBean.setResponseStatus(response.getStatusLine().getStatusCode());

        setResponseHeaders(response, httpClientBean);
        setResposneBody(response, httpClientBean);
        try
        {
            response.close();
            httpClient.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return httpClientBean;
    }

    private void addHeader(HttpRequestBase method, HttpClientBean httpBean)
    {
        Map<String, String> headers = httpBean.getRequestHeaders();
        for (String header : headers.keySet())
        {
            method.addHeader(header, headers.get(header));

        }
    }

    /**
     * Get response from httpClient and set in httpClientBean
     * 
     * @param response
     */
    private void setResponseHeaders(HttpResponse response, HttpClientBean httpBean)
    {
        Header[] headers = response.getAllHeaders();
        StringBuilder fixBuf = new StringBuilder(response.getStatusLine().toString() + "\n");
        for (Header head : headers)
        {
            fixBuf.append(head.getName() + ":" + head.getValue());
            fixBuf.append("\r\n");
            String name = head.getName();
            if (name.equalsIgnoreCase("location"))
            {
                // ok
                httpBean.setRedirect(head.getValue());
                continue;
            }
            String headerValue = httpBean.getResponsHeaderValue(name);
            if (headerValue == null)
            {
                headerValue = head.getValue();
            } else
            {
                headerValue = headerValue + "; " + head.getValue();
            }
            httpBean.putResponseHeader(name, headerValue);
        }
        httpBean.setResponseHeader(fixBuf.toString());
    }

    /**
     * Get response from httpClient and set in httpClientBean
     * 
     * @param response
     */
    private void setResposneBody(HttpResponse response, HttpClientBean httpBean)
    {
        try
        {
            httpBean.setResponseBody(EntityUtils.toString(response.getEntity(), httpBean.getEncoding()));
        } catch (Exception e)
        {
            // TODO Auto-generated catch block

        }
    }

    /**
     * Set handle redirect or not; default no redirect <br>
     * isNeedProxy is to set system proxy<br>
     * If set customProxy, it will override isNeedProxy<br>
     * isDisableRedirect default as true to capture the first response of the
     * request;
     * 
     * @return
     */
    private RequestConfig getRequestConfig(boolean isNeedProxy, String customProxy, boolean isDisableRedirect)
    {
        /**
         * On rot, if without proxy[proxy.successfactors.com:8080], unknown host
         * exception occurs;<br>
         * if use rot IP, don't need proxy
         */
        RequestConfig config = null;
        if (customProxy != null)
        {
            String proxy[] = customProxy.split(":");
            config = RequestConfig.custom().setProxy(new HttpHost(proxy[0], Integer.parseInt(proxy[1]))).build();
        } else
        {
            if (isNeedProxy)
            {
                // set system proxy
                config = RequestConfig.custom().setProxy(getSystemProxy()).build();
            } else
            {
                config = RequestConfig.custom().build();
            }
        }
        if (isDisableRedirect)
        {
            config = RequestConfig.copy(config).setRedirectsEnabled(false).setCircularRedirectsAllowed(false)
                    .setRelativeRedirectsAllowed(false).build();

        }
        return config;
    }

    private HttpEntity getHttpEntity(HttpClientBean httpBean) throws UnsupportedEncodingException
    {
        HttpEntity entity = null;
        if (httpBean.getPostBody() != null)
        {
            entity = new StringEntity(httpBean.getPostBody(), Charset.forName(httpBean.getEncoding()));

        } else if (httpBean.getUrl().indexOf("?") < 0)
        {
            // means form parameter only
            StringBuffer body = new StringBuffer();
            String encoding = httpBean.getEncoding();
            Map<String, String> paras = httpBean.getRequestParas();
            httpBean.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            for (String para : paras.keySet())
            {
                body.append(URLEncoder.encode(para, encoding)).append("=")
                        .append(URLEncoder.encode(paras.get(para), encoding)).append("&");
            }
            if (body.length() > 1)
                entity = new StringEntity(body.substring(0, body.length() - 1), Charset.forName(encoding));
        } else
        {
            //
        }
        return entity;
    }

    private String appendParasToUrl(String url, Map<String, String> paras, String encoding)
            throws UnsupportedEncodingException
    {
        // first, check the paramters
        if (paras.keySet().size() > 0)
        {
            if (url.indexOf("?") < 0)
            {
                url += "?";
            } else
            {
                url += "&";
            }
            for (String para : paras.keySet())
            {
                url += URLEncoder.encode(para, encoding) + "=" + URLEncoder.encode(paras.get(para), encoding) + "&";
            }
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private HttpHost getSystemProxy()
    {
        return new HttpHost("proxy.wdf.sap.corp", 8080);
    }

    public static void main(String[] args) throws Exception
    {
        // TODO Auto-generated method stub
        String url = "http://dc13qaautocandcfapp07.lab.od.sap.biz:8080/login?company=datPLT11&username=admin&password=pwd";
        HttpClientUtil util = new HttpClientUtil();
        HttpClientBean httpBean = new HttpClientBean(url);
        util.fetchWeb(httpBean, false);

        util.fetchWeb(httpBean, "proxy.wdf.sap.corp:8080");

    }

}
