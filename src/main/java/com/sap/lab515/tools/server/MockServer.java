package com.sap.lab515.tools.server;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
// @author mpeng
// not thread safe, actually nothing will work as a real "web server"
public class MockServer implements HttpHandler{
    private LinkedHashMap<String,Object> settings = new LinkedHashMap<String,Object>();
    private HttpServer server = null;
    private ArrayList<Handler> bindObjects = new ArrayList<Handler>();
    // for response
    
    public void clearSettings(){
        settings.clear();
    }
    
    private LinkedHashMap<String,String> rheaders = null;
    private String rcontent = null;
    private int rseq = 0;
    
    
    private synchronized int getSeq(){
        return ++rseq;
    }
    
    private String getValidName(String name){
        if(name != null)name = name.trim();//.toLowerCase()
        return (name == null || name.length() < 1) ? null : name;
    }
    
    private boolean enableStuff(String prefix,String name,boolean enable,Object val){
        name = getValidName(name);
        if(name == null)return false;
        name = prefix + name;
        if(enable){
            if(val == null)val = "true";
            settings.put(name,val);
        }
        else if(settings.containsKey(name))
            settings.remove(name);
        else
            return false;
        return true;
    }
    private synchronized void setResponseHeader(String h,String v){
        if(rheaders == null)rheaders = new LinkedHashMap<String,String>();
        rheaders.put(h, v);
    }
    
    private synchronized void clearResponseHeader(){
        rheaders = null;
        
    }
    
    private synchronized void setResponseContent(String v){
        rcontent = v;
    }
    
    public synchronized LinkedHashMap<String,String> getLastHeaders(){
        return rheaders != null ? (LinkedHashMap<String,String>)rheaders.clone() : null;
    }
    
    public synchronized String getLastResponse(){
        return rcontent;
    }
    
    public boolean enableHeader(String hdrName,boolean enable){
        return enableStuff("h-",hdrName,enable, null);
    }
    
    public boolean enableVerb(String verb,boolean enable){
        return enableStuff("v-",verb,enable, null);
    }
    
    public boolean setPathHeaders(String path,LinkedHashMap<String,String> headers,boolean enable){
        if(headers == null)return false;
        return true;////enableStuff("h2-",path.trim(),enable,headers);
    }
    
    public boolean setPathResponse(String path,String response,boolean enable){
        if(response ==  null)response = "";
        return enableStuff("r-",path.trim(),enable,response);
    }
    
    public boolean setPathHandler(String path,Handler handler,boolean enable){
        if(handler == null && enable)return false;
        path = getValidName(path);
        if(path == null)return false;
        String name = "hd-" + path;
        int idx = -1;
        if(settings.containsKey(name))
            idx = (Integer)settings.get(name);
        if(enable){
            if(idx < 0)
            {
                idx = bindObjects.size();
                bindObjects.add(handler);
                settings.put(name,idx);
            }else
                bindObjects.set(idx, handler);
        }
        else if(idx > 0){
            //settings.remove(name) // do not remove it, for idx reuse
            bindObjects.set(idx, null);
        }
        else
            return false;
        return true;
    }
    
    public boolean setPathCapture(String path,boolean enable){
        return enableStuff("pc-",path.trim(),enable, null);
    }
    
    public boolean setPathStatus(String path,String status,boolean enable){
        if(status ==  null)status = "200";
        return enableStuff("st-",path.trim(),enable,status.toString());
    }

    
    public boolean setRequestEncoding(String encoding){
        if(encoding == null)encoding = "utf-8";
        return enableStuff("s-","request_encoding",true,encoding);
    }
    
    public boolean setResponseEncoding(String encoding){
        if(encoding == null)encoding = "utf-8";
        return enableStuff("s-","response_encoding",true,encoding);
    }
    
    public boolean setHeaders(LinkedHashMap<String,String> hdrs){
        return enableStuff("h2-","*",true,hdrs);
    }
    
    public boolean setResponse(String cont){
        return enableStuff("r-","*",true,cont);
    }
    
    public boolean setStatus(String status){
        return enableStuff("st-","*",true,status);
    }
    
    
    
    private void responseStuff(int status,HttpExchange exchange,Object response,LinkedHashMap<String,String> headers,String encoding ,boolean bin) throws IOException{
        Headers rhdrs = exchange.getResponseHeaders();
        if(encoding == null)encoding = "utf-8";
        boolean ctset = false;
        if(headers != null){
            for(String key : headers.keySet()){
                rhdrs.set(key,headers.get(key));
                if(!ctset)ctset = (key.equalsIgnoreCase("Content-Type"));
            }
        }
        if(!ctset)
            rhdrs.set("Content-Type", "text/plain; charset=" + encoding);
        exchange.sendResponseHeaders(status, 0);
        
        OutputStream resp = exchange.getResponseBody();
        try{
            if(bin){
                if(response != null){
                    resp.write((byte[])response);
                }
            }else if(response != null && ((String)response).length() > 0)
                resp.write(((String)response).getBytes(encoding));
        }finally{
            resp.close();
        }
    }
    
    private synchronized String processHeaders(Headers hdrs, int seq,String reqUrl){
        if(seq != rseq)return null;
        rheaders = new LinkedHashMap<String,String>();
        for(String key : hdrs.keySet()){
            if("true".equalsIgnoreCase((String)settings.get("h-" + key)) || "true".equalsIgnoreCase((String)settings.get("h-*"))){
                String h = "";
                for(String s : hdrs.get(key)){
                    h += s + ";";
                }
                rheaders.put(key, h);
            }else
                return key;
        }
        rheaders.put("url", reqUrl);
        return null;
    }
    
    private synchronized String processContent(String content, int seq){
        //println "expect seq: " + rseq + ", seq:"+ seq
        if(seq != rseq)return null;
        return rcontent = content;
    }
    
    public void handle(HttpExchange exchange) throws IOException{
        try{
            _handle(exchange);
        }catch(Exception e){
            e.printStackTrace();
            responseStuff(500,exchange,"Http Error During Processing: " + e.getMessage(), null,null, false);
        }
    }
    
    private Object match(String key,String key2AsQuery){
        //println "matching:" + key + "ss" + settings
        Object ret = null;
        if(!key.endsWith("/"))
            key += "/";
        int len = 0;
        while(ret == null){
            len = key.lastIndexOf("/");
            if(len < 0)break;
            key = key.substring(0,len);
            //println "macthing:" + key
            if(key2AsQuery != null){
                key2AsQuery = key + "?" + key2AsQuery;
                if(settings.containsKey(key2AsQuery))
                    ret = settings.get(key2AsQuery);
                key2AsQuery = null;
            }
            if(ret == null && settings.containsKey(key))
                ret = settings.get(key);
            if(ret == null && settings.containsKey(key + "/"))
                ret = settings.get(key + "/");
            if(ret == null && settings.containsKey(key + "/*"))
                ret = settings.get(key + "/*");
        }
        if(ret == null)
            ret = settings.get(key + "*"); // global 
        return ret;
    }
    
    public void _handle(HttpExchange exchange) throws IOException{
        String url = exchange.getRequestURI().getPath();
        if(url.equalsIgnoreCase("/_shutdown")){
            System.out.println("Trying to stop server...");
            responseStuff(200,exchange,"shutdown server is in progress now!", null,null,false);
            stopped = true; // prepare to shutdown
        }
        else if(url.equalsIgnoreCase("/_clr")){
            setResponseContent(null);
            clearResponseHeader();
            responseStuff(200,exchange,"last response cleared!", null,null, false);
        }
        else if(url.equalsIgnoreCase("/_last")){ // get the last stuff
            responseStuff(200,exchange,rcontent == null ? "no content yet" : rcontent, null,null,false);
        }else if(url.equalsIgnoreCase("/_last_headers")){
            StringBuilder sb = new StringBuilder();
            if(rheaders != null){
                for(String key : rheaders.keySet()){
                    sb.append(key + ": " + rheaders.get(key) + "\r\n");
                }
            }
            responseStuff(200,exchange,sb.toString(),null,null,false);
        }else{
            int seq = -1;
            if( "true".equalsIgnoreCase((String)match("pc-" + url,null)))
                seq = getSeq();
            MockResponse resp = new MockResponse();
            String status = null;
            String verb = exchange.getRequestMethod();
            if(!("true".equalsIgnoreCase((String)settings.get("v-" + verb))  || "true".equalsIgnoreCase((String)settings.get("v-*")))){
                responseStuff(500,exchange,"Http Verb: " + verb + " Not Allowed!",null,null,false);
                return;
            }
            resp.method = verb;
            // check all stuff
            resp.req_headers = exchange.getRequestHeaders();
            resp.req_query = exchange.getRequestURI().getQuery();
            
            String k = processHeaders(resp.req_headers,seq,exchange.getRequestURI().toString());
            if(k != null){
                responseStuff(500,exchange,"Http Header: " + k + " Not Allowed!",null,null,false);
                return;
            }
            
            // get the content
            InputStream ins = exchange.getRequestBody();
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            try{
                int len = 0;
                byte[] buf = new byte[8192];
                while((len = ins.read(buf,0,8192)) > 0){
                    bas.write(buf,0,len);
                }
                resp.req_data = "";
                resp.req_encoding = (String)settings.get("s-request_encoding");
                if(resp.req_encoding == null)resp.req_encoding = "utf-8";
                if(bas.size() > 0){
                    buf = bas.toByteArray();
                    resp.req_data = new String(buf,0,buf.length,resp.req_encoding);
                }
                
            }finally{
                ins.close();
                bas.close();
            }
            processContent(resp.req_data,seq);
            resp.encoding = (String)settings.get("s-response_encoding");
            if(resp.encoding == null)resp.encoding = "utf-8";
            resp.url = url;
            // ok, now let's care about what we need to render here
            Handler handler = null;
            // first, check the path response
            String wildCardUrl = url;
            
            if(!wildCardUrl.endsWith("/"))
                wildCardUrl += "/";
            while(handler == null){
                int len = wildCardUrl.lastIndexOf("/");
                if(len < 0)break;
                wildCardUrl = wildCardUrl.substring(0,len);
                if(settings.containsKey("hd-" + wildCardUrl + "/"))
                    handler = bindObjects.get((Integer)settings.get("hd-" + wildCardUrl + "/"));
                if(handler == null && settings.containsKey("hd-" + wildCardUrl + "/*"))
                    handler = bindObjects.get((Integer)settings.get("hd-" + wildCardUrl + "/*"));
            }
            if(handler == null && settings.containsKey("hd-*"))
                handler = bindObjects.get((Integer)settings.get("hd-*"));

            if(handler != null){
                handler.handle(resp);
            }else{
                resp.headers = (LinkedHashMap<String,String>)match("h2-" + url,null);
                resp.data = (String)match("r-" + url,resp.req_query);
                status = (String)match("st-" + url,null);
                if(status == null){
                    if(resp.data != null)resp.status = 200;
                    else resp.status = 404;
                }else
                    resp.status = Integer.parseInt(status);
            }
            System.out.println("Processing: " + url + ", query: " + resp.req_query + ", encoding: " + resp.req_encoding + ", status:" + resp.status + ", data: " + resp.req_data.length());
            responseStuff(resp.status,exchange,resp.data,resp.headers,resp.encoding,resp.bin);
        }
    }
    private ExecutorService threadPool = null;
    private boolean stopped = true;
    
    public boolean hasStarted(){
        return !stopped;
    }
    
    public void start(int port)  throws IOException{
        if(threadPool != null)return;
        stopped = false;
        InetSocketAddress addr = new InetSocketAddress(port);
        server = HttpServer.create(addr, 0);
        server.createContext("/", this);
        server.setExecutor(threadPool = Executors.newCachedThreadPool());
        server.start();
    }
    
    public void stop(){
        if(server == null)return;
        server.stop(1);
        threadPool.shutdownNow();
        server = null;
        threadPool = null;
    }
    
    
    private boolean _sleep(int ms){
        try{
            Thread.sleep(ms);
        }catch(Throwable e){
        }
        return true;
    }
    
    public void loopTillStop(){
        while(!stopped && _sleep(1000));
        System.out.println("Stopping...");
        stop();
        System.out.println("Stopped server completed");
    }
    
    public static void main(String[] args) throws IOException{
        MockServer ahs = new MockServer();
        ahs.enableVerb("*",true);
        ahs.enableHeader("*",true);

        ahs.setResponse("ok this is a test");
        ahs.setPathCapture("/",true);
        ahs.setPathStatus("/err","500",true);
        ahs.setPathResponse("/err","error occured, no idea",true);
        ahs.setPathResponse("/v1/auth/token","{\"access_token\":\"test_token\"}",true);
        int port = 8080;
        if(args != null && args.length > 0)
            port = Integer.parseInt(args[0]);
        if(port < 80)port = 8080;
        ahs.start(port);
        ahs.loopTillStop();
    }
}
