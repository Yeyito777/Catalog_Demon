package net.yeyito.connections.tor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;

public class TorNetworkManager {
    public static HashMap<String,Long> blockedExitNodes = new HashMap<>();

    public static void finishedUsingIP(String ip) {
        blockedExitNodes.put(ip,System.nanoTime());
    }

    public static void updateIPs() {
        for (String exitNode: blockedExitNodes.keySet()) {
            double secondsAgo = (System.nanoTime() - blockedExitNodes.get(exitNode))/1e+9;
            if (secondsAgo > 60) {
                blockedExitNodes.remove(exitNode);
            }
        }
    }

    public static boolean isIPblocked(String ip) {
        return blockedExitNodes.containsKey(ip);
    }

    public static String getPublicIP(TorInstance tor) {
        String publicIP = "";

        try {
            URL checkIP = new URL("http://checkip.amazonaws.com/");
            HttpURLConnection connection = (HttpURLConnection) checkIP.openConnection(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", tor.socksPort)));

            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            publicIP = reader.readLine();
            reader.close();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return publicIP;
    }
}
