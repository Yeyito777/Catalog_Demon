package net.yeyito.connections;

import net.yeyito.Main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class ProxyUtil {
    private static final List<Proxy> availableProxies = initAvailableProxies();
    private static final HashMap<Proxy,Integer> proxyCodes = new HashMap<>();

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
                String[] parts = line.split(":");
                String ip = parts[0];
                int port = Integer.parseInt(parts[1]);
                list.add(httpProxy(ip, port));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Proxy grabAvailableProxy() {
        if (!availableProxies.isEmpty()) {
            Proxy proxy = availableProxies.get(0);
            availableProxies.remove(0);
            return proxy;
        }
        else {return null;}
    }

    public static Integer getProxyCode(Proxy proxy) {
        if (proxyCodes.containsKey(proxy)) {return proxyCodes.get(proxy);}
        proxyCodes.put(proxy,new Random().nextInt(0,100000000));
        return getProxyCode(proxy);
    }

    public static void freeProxy(Proxy proxy, int seconds) {
        CompletableFuture.runAsync(() -> {
            Main.threadSleep(seconds*1000);
            availableProxies.add(proxy);
        });
    }
}
