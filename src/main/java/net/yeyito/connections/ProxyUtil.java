package net.yeyito.connections;

import net.yeyito.Main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ProxyUtil {
    private static final HashMap<Proxy,Integer> proxyCodes = new HashMap<>();
    private static final HashMap<Proxy,String> authenticatedProxyToSecCookie = new HashMap<>();

    public static List<Proxy> initAvailableProxies() {
        List<Proxy> list = new ArrayList<>();
        // ** ADD PROXIES ** //
        loadProxiesFromFile(list);
        // **             ** //
        return list;
    }

    private static Proxy httpProxy(String ip, int port) {
        return new Proxy(Proxy.Type.HTTP,new InetSocketAddress(ip,port));
    }

    private static void loadProxiesFromFile(List<Proxy> list) {
        String fileName = "src/main/resources/Proxies.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Splitting at the first colon to separate the IP
                String[] ipAndRest = line.split(":", 2);
                String ip = ipAndRest[0];
                String rest = ipAndRest[1];

                // If the IP is "1" and the rest is "1", add a null proxy
                if ("1".equals(ip) && "1".equals(rest)) {
                    list.add(null);
                    continue;
                }

                // Checking for the presence of "=" for the SECCOOKIE
                if (rest.contains("=")) {
                    String[] portAndCookie = rest.split("=", 2); // Splitting by "=" but limiting to 2 parts
                    int port = Integer.parseInt(portAndCookie[0]);
                    String secCookie = ".ROBLOSECURITY=_|WARNING:-DO-NOT-SHARE-THIS.--Sharing-this-will-allow-someone-to-log-in-as-you-and-to-steal-your-ROBUX-and-items.|_" + portAndCookie[1] + ";";
                    Proxy proxy = httpProxy(ip, port);
                    list.add(proxy);
                    authenticatedProxyToSecCookie.put(proxy, secCookie); // Adding the proxy and its cookie to the hashmap
                } else {
                    int port = Integer.parseInt(rest);
                    list.add(httpProxy(ip, port));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static boolean isAuthenticated(Proxy proxy) {return authenticatedProxyToSecCookie.containsKey(proxy);}
    public static String getCookie(Proxy proxy) {return authenticatedProxyToSecCookie.get(proxy);}

    public static Integer getProxyCode(Proxy proxy) {
        if (proxyCodes.containsKey(proxy)) {return proxyCodes.get(proxy);}
        proxyCodes.put(proxy,new Random().nextInt(0,100000000));
        return getProxyCode(proxy);
    }

    public static void setProxyCode(Proxy proxy, Integer code) {
        proxyCodes.put(proxy,code);
    }

    public static Proxy getProxyFromCode(Integer code) {
        for (Proxy proxy : proxyCodes.keySet()) {
            if (Objects.equals(proxyCodes.get(proxy), code)) {
                return proxy;
            }
        }
        return null;
    }
}
