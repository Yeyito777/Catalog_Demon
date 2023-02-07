package net.yeyito.connections;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class ProxyUtil {
    // Sponsored by: https://www.proxynova.com/proxy-server-list/  !!
    private static int proxyIndex = 0;
    private static final String[][] proxies = {
            new String[]{"104.43.230.151","3128"}};

    private static String[] getNextProxy() {
        if (proxyIndex == proxies.length) {
            proxyIndex = 0;
        }
        return proxies[proxyIndex++];
    }

    public static Proxy getFunctionalProxy() {
        Proxy proxy = null;
        while (proxy == null) {
            String[] proxyDetails = getNextProxy();
            String proxyIP = proxyDetails[0];
            String proxyPort = proxyDetails[1];
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIP, Integer.parseInt(proxyPort)));
            System.out.println("Trying proxy: " + proxyIP + ":" + proxyPort);
            try {
                URL url = new URL("https://www.proxynova.com/proxy-server-list/");
                HttpURLConnection con = (HttpURLConnection) url.openConnection(proxy);
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                if (con.getResponseCode() != 200) {
                    System.out.println("Proxy not functional, trying with next one");
                    proxy = null;
                }
            } catch (Exception e) {
                System.out.println("Proxy not functional, trying with next one");
                proxy = null;
            }
        }
        System.out.println("Using proxy: " + proxy.address().toString());
        return proxy;
    }
}
