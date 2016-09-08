package com.sap.lab515.tools.server;

import java.util.LinkedHashMap;

import com.sun.net.httpserver.Headers;

public class MockResponse {
    public LinkedHashMap<String,String> headers = null;
    public Object data = null;
    public int status = 200;
    public String encoding = null;
    public boolean bin = false;
    
    public String url = null;
    public Headers req_headers = null;
    public String req_data = null;
    public String req_query = null;
    public String req_encoding = null;
    public String method = null;
}