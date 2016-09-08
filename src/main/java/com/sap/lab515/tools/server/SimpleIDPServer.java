package com.sap.lab515.tools.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import saml.provider.SamlResponseProvider;

import org.apache.commons.codec.binary.Base64;

import http.HttpUtil;

public class SimpleIDPServer implements Handler
{
    private MockServer ahs       = null;
    private int        lsnPort   = 0;
    private String     staticDir = null;

    private SimpleIDPServer(int port, String dir)
    {
        lsnPort = port;
        staticDir = dir;
    }

    private void start() throws IOException
    {
        if (ahs == null)
            return;
        if (lsnPort < 80 || ahs.hasStarted())
            return; // already started. this is one shot usage
        ahs.start(lsnPort);
    }

    public static SimpleIDPServer startServer(int port, String resourceDir) throws IOException
    {
        SimpleIDPServer ss = new SimpleIDPServer(port, resourceDir);
        MockServer ahs = ss.ahs = new MockServer();
        ahs.enableVerb("*", true);
        ahs.enableHeader("*", true);
        ahs.setPathHandler("/html/*", ss, true);

        ahs.setPathStatus("/err", "500", true);
        ahs.setPathResponse("/err", "error occured, no idea", true);

        ahs.setPathCapture("/echo", true);
        ahs.setPathResponse("/echo", "hello!", true);
        ss.start();
        return ss;
    }

    public void hang()
    {
        ahs.loopTillStop();
    }

    public static void main(String[] args) throws Exception
    {
        int port = 0; // default port number is 8848
        try
        {
            if (args != null && args.length > 0)
                port = Integer.parseInt(args[0]);
        } catch (Exception e)
        {
        }
        if (port < 80)
            port = 8848;

        SimpleIDPServer ss = startServer(port, "/res/");
        ss.hang();
    }

    // only for text
    private static String readTextFile(String filePath, String enc) throws Exception
    {
        InputStream fs = SimpleIDPServer.class.getResourceAsStream(filePath);
        try
        {
            BufferedReader ins = new BufferedReader(new InputStreamReader(fs, enc));
            char[] chars = new char[4096];
            int len = 0;
            StringBuilder ret = new StringBuilder();
            while ((len = ins.read(chars)) > 0)
            {
                if (chars[0] == (char) 65279 || chars[0] == (char) 65534)
                    ret.append(chars, 1, len - 1);
                else
                    ret.append(chars, 0, len);
            }
            ins.close();
            return ret.toString();
        } catch (Exception e)
        {
            return null;
        } finally
        {
            try
            {
                fs.close();
            } catch (Exception e)
            {
            }
        }
    }

    private static byte[] readBinFile(String filePath) throws IOException
    {
        InputStream file = SimpleIDPServer.class.getResourceAsStream(filePath);
        ByteArrayOutputStream bto = new ByteArrayOutputStream();
        try
        {
            byte[] bytes = new byte[65536];
            int l = file.read(bytes, 0, 65536);
            while (l > 0)
            {
                bto.write(bytes, 0, l);
                l = file.read(bytes, 0, 65536);
            }
            file.close();
            bytes = bto.toByteArray();
            return bytes;
        } catch (Exception e)
        {
            return null;
        } finally
        {
            try
            {
                file.close();
                bto.close();
            } catch (Exception e)
            {
            }

        }
    }

    static String[] unpack(String value)
    {
        if (value == null || value.length() < 1)
            return null;
        int len = value.length();
        if (len < 2)
            return null;
        int pos = 0;
        ArrayList<String> ret = new ArrayList<String>();
        int segLen = 0;
        int startPos = 0;
        while (pos < len - 1)
        {
            // try to get the next index
            segLen = value.indexOf(":", pos);
            if (segLen <= pos)
                break;
            startPos = segLen + 1;
            // Fix, could be a exception for parseInt
            if (value.charAt(pos) >= '0' && value.charAt(pos) <= '9')
            {
                try
                {
                    segLen = Integer.parseInt(value.substring(pos, segLen));
                } catch (Exception e)
                {
                    segLen = -1;
                }
            } else
                segLen = -1;
            if (segLen >= 0 && startPos + segLen <= len)
            {
                ret.add(value.substring(startPos, segLen + startPos));
                pos = startPos + segLen;
            } else
                break;
        }
        if (pos != len || ret.size() < 1)
            return null;
        String[] ret2 = new String[ret.size()];
        ret.toArray(ret2);
        ret.clear();
        return ret2;
    }

    static String pack(String... strings)
    {
        return packArr(strings);
    }

    static String packArr(String[] arr)
    {
        if (arr == null || arr.length < 1)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++)
        {
            sb.append(arr[i] != null ? arr[i].length() : "0");
            sb.append(":");
            if (arr[i] != null)
                sb.append(arr[i]);
        }
        return sb.toString();
    }

    @Override
    public void handle(MockResponse resp)
    {
        LinkedHashMap<String, String> hdrs = new LinkedHashMap<String, String>();
        resp.headers = hdrs;
        resp.headers.put("Content-Type", "text/html; charset=utf-8");
        if (resp.url.equalsIgnoreCase("/html/service"))
        {
            // handle the api
            String[] arr = unpack(resp.req_data);
            String[] rets = null;
            // depend on what's the code
            if (arr == null || arr.length < 2)
            {
                resp.status = 500;
                resp.data = "invalid request!";
                return;
            }
            String cmd = arr[0];
            arr = unpack(arr[1]);

            if (cmd.equalsIgnoreCase("test"))
            {
                // OK is a must
                rets = new String[] { "OK", "you have said: " + arr[0] };
            } else if (cmd.equalsIgnoreCase("env"))
            {
                rets = new String[] { "OK", "TODO" };
            } else if (cmd.equalsIgnoreCase("GENSSO"))
            {
                String url = this.getSsoRecipient(arr[0], arr[4], arr[1].equalsIgnoreCase("saml2"));
                boolean displayMode = Boolean.valueOf(arr[9]);
                String saml = getSamlResponse(arr);
                if (!displayMode)
                    saml = Base64.encodeBase64String(saml.getBytes());
                rets = toOK(pack(saml, url));
            } else if (cmd.equalsIgnoreCase("getid"))
            {
                String[] resps;
                try
                {
                    resps = HttpUtil.getResponse(arr[0], false);
                } catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    resps = new String[] { e.getMessage(), "", "" };
                }
                rets = toOK(pack(resps));
            }
            resp.status = 200;
            if (rets != null)
            {
                resp.data = packArr(rets);
            } else
            {
                resp.data = pack("ERROR", "unknown command:" + arr[0]);
            }

        } else if (resp.url.startsWith("/html/"))
        {
            String url = staticDir + resp.url.substring(6);
            try
            {
                resp.bin = url.endsWith(".gif") || url.endsWith(".jpg") || url.endsWith(".jpeg");
                if (resp.bin)
                    resp.data = readBinFile(url);
                else
                    resp.data = readTextFile(url, "utf-8");
                if (resp.data == null)
                {
                    resp.data = "error reading the resource of url: " + resp.url;
                    resp.status = 500;
                } else
                {
                    if (url.endsWith(".js"))
                    {
                        resp.headers.put("Content-Type", "text/javascript; charset=utf-8");
                    } else if (resp.bin)
                    {
                        resp.headers.put("Content-Type", "image/gif");
                        resp.headers.put("Cache-Control", "max-age=2592000");
                    }

                    resp.status = 200;
                }

            } catch (Exception e)
            {
                resp.status = 404;
                resp.data = "file not existed!";
            }
        } else
        {
            resp.data = "url not existed:" + resp.url;
            resp.status = 404;
        }
    }

    private String[] toOK(String... resp)
    {
        String result[] = new String[resp.length + 1];
        result[0] = "OK";

        System.arraycopy(resp, 0, result, 1, resp.length);
        return result;
    }

    private String getSamlResponse(String... para)
    {
        Map<String, String> params = new HashMap<String, String>();
        boolean isSaml2 = para[1].equalsIgnoreCase("saml2");
        String recipient = getSsoRecipient(para[0], para[4], isSaml2);
        boolean isEnc = Boolean.valueOf(para[2]);
        if (isEnc)
        {
            params.put("encryptTarget", "Assertion");
        }
        boolean signAssertion = Boolean.valueOf(para[3]);
        boolean signResp = Boolean.valueOf(para[8]);
        String sign;
        if (signAssertion && signResp)
        {
            sign = "Both";
        } else if (signResp)
        {
            sign = "Response";
        } else if (signAssertion)
        {
            sign = "Assertion";
        } else
        {
            sign = "Neither";
        }
        params.put("sign", sign);
        params.put("destination", recipient);
        params.put("type", para[1]);
        params.put("BothIssuer", para[7]);
        params.put("Recipient", recipient);
        params.put("User", para[5]);
        params.put("Audience", para[10]);
        String responseId = para[11];
        if (responseId != null && responseId.length() > 0)
        {
            params.put("responseId", responseId);
        }
        String response = null;
        try
        {
            response = SamlResponseProvider.execute(params);
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return response;
    }

    private String getSsoRecipient(String site, String company, boolean isSaml2)
    {
        return site + (isSaml2 ? "/saml2/SAMLAssertionConsumer?company=" : "/saml/samllogin?company=") + company;
    }
}