package http;

import java.util.HashMap;
import java.util.Map;

public class HttpClientBean
{
    private String                  url;
    private MethodEnum              method           = MethodEnum.Get;
    /**
     * For Post Method
     */
    private String                  postBody;
    private String                  encoding         = "utf-8";
    private boolean                 isSkipParaEncode = false;
    /**
     * !+key
     */
    private Map<String, String>     requestHeaders   = new HashMap<String, String>();
    private StringBuffer            requestHeader    = new StringBuffer();
    /**
     * ?+key
     */
    private Map<String, String>     requestParas     = new HashMap<String, String>();
    private int                     responseStatus;

    /**
     * !Content-Length
     */
    private long                    contentLength;
    private String                  redirect;
    private String                  responseHeader;
    private String                  responseBody;
    private HashMap<String, String> responseHeaders  = new HashMap<String, String>();

    public HttpClientBean()
    {

    }

    public HttpClientBean(String url)
    {
        this.setUrl(url);
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public MethodEnum getMethod()
    {
        return method;
    }

    public void setMethod(MethodEnum method)
    {
        this.method = method;
    }

    public String getPostBody()
    {
        return postBody;
    }

    public StringBuffer getRequestHeader()
    {
        return requestHeader;
    }

    public void setPostBody(String body)
    {
        this.postBody = body;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    /**
     * Remove append ! in new httpClientUtils
     * 
     * @param key
     *            will append ! as prefix which is fetchWeb legacy issue
     * @param value
     */
    public void addRequestHeader(String key, String value)
    {
        if (key.startsWith("!"))
        {
            key = key.substring(1);
        }
        requestHeaders.put(key, value);
        requestHeader.append(key + " = " + value + "\n");
    }

    public void addRequestHeaderAfterClear(String key, String value)
    {
        refreshRequestHeaders();
        addRequestHeader(key, value);
    }

    public Map<String, String> getRequestHeaders()
    {
        return requestHeaders;
    }

    public void refreshRequestHeaders()
    {
        requestHeaders.clear();
        addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        requestHeader = new StringBuffer();
    }

    public void refreshRequestParas()
    {
        this.requestParas.clear();
    }

    public void clearRequestContent()
    {
        this.setPostBody(null);
        this.refreshRequestHeaders();
        this.refreshRequestParas();

    }

    public void putResponseHeader(String key, String value)
    {
        responseHeaders.put(key, value);
    }

    /**
     * Will assert when Cookie is null
     * 
     * @throws Exception
     */
    public String getLoginCookie() throws Exception
    {
        return getLoginCookie(false);

    }

    /**
     * @param isExceptionMode
     *            Set true when need throw exception when cookie is null
     * @return
     * @throws Exception
     */
    public String getLoginCookie(boolean isExceptionMode) throws Exception
    {
        String errorInfo = "Cannot get Set-Cookie in Http Response headers!\n";
        String cookie = this.getResponsHeaderValue("Set-Cookie");
        if (isExceptionMode && isNullOrEmpty(cookie))
        {
            throw new Exception(errorInfo + this.getResponseHeader());
        }

        StringBuffer sb = new StringBuffer();
        for (LoginCookieEnum cookieEnum : regularLoginCookies)
        {
            String cookieText = getLoginCookie(cookie, cookieEnum);
            if (!isNullOrEmpty(cookieText))
            {
                sb.append(cookieText + ";");
            }
        }

        return sb.toString();
    }

    private LoginCookieEnum[] regularLoginCookies = { LoginCookieEnum.JSESSIONID, LoginCookieEnum.BigIpGeneral };

    private String getLoginCookie(String source, LoginCookieEnum cookieEnum)
    {
        String result = null;
        if (source.contains(cookieEnum.toString()))
        {
            result = source.replaceAll(".*(" + cookieEnum.toString() + ".*?);.*", "$1");
        }
        return result;
    }

    /**
     * Support legacy httpclient, it will append ! in header
     * 
     * @param key
     * @return
     */
    public String getResponsHeaderValue(String key)
    {
        String result = responseHeaders.get(key);
        if (isNullOrEmpty(result))
        {
            if (key.startsWith("!"))
            {
                return responseHeaders.get(key.substring(1));
            } else
            {
                return responseHeaders.get("!" + key);
            }
        } else
        {
            return result;
        }
    }

    /**
     * Same Cookie will in a key value pair separated with ;
     * 
     * @return
     */
    public Map<String, String> getResponseHeaders()
    {
        return responseHeaders;
    }

    /**
     * return null if not contain the cookie key
     * 
     * @param cookieEnum
     * @return
     */
    public String getCookieValue(LoginCookieEnum cookieEnum)
    {
        String cookie = this.getResponsHeaderValue("Set-Cookie");
        if (cookie.contains(cookieEnum.toString()))
        {
            return cookie.replaceAll(".*?" + cookieEnum.toString() + "=(.*?);.*", "$1");
        } else
        {
            return null;
        }
    }

    public void refreshResponseHeaders()
    {
        responseHeaders.clear();
    }

    public void addPara(String key, String value)
    {
        requestParas.put(key, value);
    }

    public Map<String, String> getRequestParas()
    {
        return requestParas;
    }

    public int getResponseStatus()
    {
        return responseStatus;
    }

    public void setResponseStatus(int responseStatus)
    {
        this.responseStatus = responseStatus;
    }

    public long getContentLength()
    {
        return contentLength;
    }

    public void setContentLength(long contentLength)
    {
        this.contentLength = contentLength;
    }

    public String getRedirect()
    {
        return redirect;
    }

    public void setRedirect(String redirect)
    {
        this.redirect = redirect;
    }

    /**
     * this Header will list all cookie line by line; Same name cookie list in
     * different line<br>
     * Set-Cookie:
     * JSESSIONID=855BDA1688AC6B2D8C84848961237A96.DC13QAAUTOCANDCFAPP02; Path=/
     * <br>
     * Set-Cookie: loginMethodCookieKey=SSO; Version=1; path=/;Expires=
     * "Wed Mar 04 09:28:05 UTC 2065";Max-Age=1576800000000; Secure<br>
     * Set-Cookie: assertingPartyCookieKey=defaultIDP; Version=1;
     * path=/;Expires="Wed Mar 04 09:28:05 UTC 2065";Max-Age=1576800000000;
     * 
     * @return
     */
    public String getResponseHeader()
    {
        return responseHeader;
    }

    public void setResponseHeader(String responseHeader)
    {
        this.responseHeader = responseHeader;
    }

    public String getResponseBody()
    {
        return responseBody;
    }

    public void setResponseBody(String responseBody)
    {
        this.responseBody = responseBody;
    }

    public boolean isSkipParaEncode()
    {
        return isSkipParaEncode;
    }

    public void setSkipParaEncode(boolean isSkipParaEncode)
    {
        this.isSkipParaEncode = isSkipParaEncode;
    }

    /**
     * Get fetch Info after fetched
     */
    public String getFetchInfo()
    {
        StringBuffer execResult = new StringBuffer();
        execResult.append("\nRequest Url: " + getUrl() + "\n");
        if (!isNullOrEmpty(getRedirect()))
        {
            execResult.append("Response Url: [" + getRedirect() + "]\n");
        } else
        {
            execResult.append("No Response Url\n");
        }
        execResult
                .append("Http Method = [" + getMethod().toString() + "]; Resp status: [" + getResponseStatus() + "]\n");
        if (!isNullOrEmpty(getPostBody()))
        {
            execResult.append("Post body = [" + getPostBody() + "]\n");
        }
        if (getRequestHeader().length() > 0)
        {
            execResult.append("Request header = [" + getRequestHeader().toString() + "]\n");
        }
        if (this.getResponseHeader() != null)
        {
            execResult.append("Response Header = [" + getResponseHeader() + "]\n");
        } else
        {
            execResult.append("No Response Header\n");
        }
        if (this.getResponseBody() != null)
        {
            execResult.append("Response body = [" + getResponseBody() + "]\n");
        } else
        {
            execResult.append("No Response Body\n");
        }
        return execResult.toString();
    }

    public void clearResponseContentBeforeFetch()
    {
        this.setRedirect(null);
        this.setContentLength(0);
        this.setResponseBody(null);
        this.setResponseHeader(null);
        this.refreshResponseHeaders();
    }

    public enum MethodEnum
    {
        Post, Delete, Get;
    }

    private boolean isNullOrEmpty(String text)
    {
        return text == null || text.equals("");
    }
}
