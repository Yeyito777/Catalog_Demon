package net.yeyito.connections;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class ProxyUtil {
    // Sponsored by: https://www.proxynova.com/proxy-server-list/  !!
    private static int proxyIndex = 0;
    private static final String[][] proxies = {
            new String[]{"104.129.41.2","1994"}};
            //new String[]{"78.46.244.55","3128"}};

    private static String[] getNextProxy() {
        if (proxyIndex == proxies.length) {
            proxyIndex = 0;
        }
        return proxies[proxyIndex++];
    }

    public static Proxy getProxy() {
        String proxyIP = ProxyUtil.getNextProxy()[0];
        String proxyPort = ProxyUtil.getNextProxy()[1];
        System.out.println("using proxy: " + proxyIP + ":" + proxyPort);
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIP, Integer.parseInt(proxyPort)));
    }
}
