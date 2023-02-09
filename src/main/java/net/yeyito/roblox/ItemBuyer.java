package net.yeyito.roblox;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.yeyito.Main;
import net.yeyito.StringFilter;
import net.yeyito.VirtualBrowser;
import net.yeyito.WebsiteScraper;
import net.yeyito.util.JSON;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class ItemBuyer {
    static VirtualBrowser virtualBrowser = new VirtualBrowser();
    @SuppressWarnings({"unchecked"})
    public static void buyItem() {
//        // First call - Redirect Incoming.
//        HashMap<String,String> login_headers = new HashMap<>();
//        login_headers.put("connection","keep-alive");
//        login_headers.put("host","roblox.com");
//        String[] remove_login_headers = new String[]{"sec-ch-ua","sec-ch-ua-mobile","sec-ch-ua-platform","sec-fetch-dest","sec-fetch-mode","sec-fetch-site","sec-fetch-user"};
//        HashMap<String,Object> returnArgs = virtualBrowser.openWebsite("http://roblox.com/login","GET",virtualBrowser.buildHeaders("roblox-login",login_headers,remove_login_headers), null,null,true,false);
//        // Second call - Permanent Redirect Incoming.
//        if (((int) returnArgs.get("responseCode")) != 307) {System.err.println("First call login didn't return response code: " + returnArgs.get("responseCode") + " instead of expected code: 307"); return;}
//        HashMap<String,Object> returnArgsRedirect = virtualBrowser.openWebsite(((Map<String, List<String>>) returnArgs.get("responseHeaders")).get("location").get(0), "GET",virtualBrowser.buildHeaders("roblox-login",null,null), null,null,true,false);
//        // Third call - Login Site
//        if (((int) returnArgsRedirect.get("responseCode")) != 308) {System.err.println("Second call login didn't return response code: " + returnArgs.get("responseCode") + " instead of expected code: 308"); return;}
//        HashMap<String,Object> returnArgsPermanent = virtualBrowser.openWebsite(((Map<String, List<String>>) returnArgsRedirect.get("responseHeaders")).get("location").get(0), "GET",virtualBrowser.buildHeaders("roblox-login",null,null), null,null,true,false);
//        String token = StringFilter.parseStringUsingRegex(returnArgsPermanent.get("response").toString(), "<meta name=\"csrf-token\" data-token=\"(.*?)\"");
//        // Fourth call - RBXSource or _gcl_au not needed. (check disc)
//        HashMap<String,String> loginHeaders = new HashMap<>();
//        loginHeaders.put("accept","application/json, text/plain, */*");
//        loginHeaders.put("content-type","application/json;charset=UTF-8");
//        loginHeaders.put("referer","https://www.roblox.com/");
//        loginHeaders.put("sec-fetch-site","same-site");
//        loginHeaders.put("x-csrf-token",token);
//        virtualBrowser.openWebsite("https://auth.roblox.com/v2/login","POST",virtualBrowser.buildHeaders("roblox-POST",loginHeaders,new String[]{"accept","referer","sec-fetch-site"}), virtualBrowser.buildCookies(virtualBrowser.cookies,null),
//                "{\"ctype\":\"Username\",\"cvalue\":\"BeepBoop_DeniedReq\",\"password\":\"WatchThisAccGetBannedinUnderAMonthBecauseOfSuspiciousBehaviour\"}", true,true);
        HashMap<String,Object> args = virtualBrowser.curlToOpenWebsite("curl \"https://www.roblox.com/login\" ^\n" +
                "  -H \"authority: www.roblox.com\" ^\n" +
                "  -H \"accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9\" ^\n" +
                "  -H \"accept-language: es-PA,es;q=0.9\" ^\n" +
                "  -H \"sec-ch-ua: ^\\^\"Not_A Brand^\\^\";v=^\\^\"99^\\^\", ^\\^\"Google Chrome^\\^\";v=^\\^\"109^\\^\", ^\\^\"Chromium^\\^\";v=^\\^\"109^\\^\"\" ^\n" +
                "  -H \"sec-ch-ua-mobile: ?0\" ^\n" +
                "  -H \"sec-ch-ua-platform: ^\\^\"Windows^\\^\"\" ^\n" +
                "  -H \"sec-fetch-dest: document\" ^\n" +
                "  -H \"sec-fetch-mode: navigate\" ^\n" +
                "  -H \"sec-fetch-site: none\" ^\n" +
                "  -H \"sec-fetch-user: ?1\" ^\n" +
                "  -H \"upgrade-insecure-requests: 1\" ^\n" +
                "  -H \"user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36\" ^\n" +
                "  --compressed");

        System.out.println(args);
    }
}