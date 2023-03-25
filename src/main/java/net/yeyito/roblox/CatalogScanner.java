package net.yeyito.roblox;

import net.yeyito.Main;
import net.yeyito.connections.tor.TorInstance;
import net.yeyito.connections.tor.TorManager;
import net.yeyito.util.*;
import net.yeyito.VirtualBrowser;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CatalogScanner {
    static VirtualBrowser virtualBrowser = new VirtualBrowser();

    public static HashMap<Long,List<Object>> itemBulkToPriceRequest(List<Long> IDs, String token, Proxy proxy, VirtualBrowser browser) throws IOException {
        StringBuilder payload = new StringBuilder();
        payload.append("{\"items\":[");
        for (int i = 0; i < IDs.size(); i++) {
            payload.append("{\"id\":").append(IDs.get(i)).append(",\"itemType\":\"Asset\",\"key\":\"Asset_").append(IDs.get(i)).append("\",\"thumbnailType\":\"Asset\"}");
            if (i != IDs.size() - 1) {
                payload.append(",");
            }
        }
        payload.append("]}");

        HashMap<String, Object> itemsResponse = browser.curlToOpenWebsite("curl 'https://catalog.roblox.com/v1/catalog/items/details' \\\n" +
                "  -H 'authority: catalog.roblox.com' \\\n" +
                "  -H 'accept: application/json, text/plain, */*' \\\n" +
                "  -H 'accept-language: en-US,en;q=0.9' \\\n" +
                "  -H 'content-type: application/json;charset=UTF-8' \\\n" +
                "  -H 'origin: https://www.roblox.com' \\\n" +
                "  -H 'referer: https://www.roblox.com/' \\\n" +
                "  -H 'sec-ch-ua: \"Chromium\";v=\"110\", \"Not A(Brand\";v=\"24\", \"Google Chrome\";v=\"110\"' \\\n" +
                "  -H 'sec-ch-ua-mobile: ?0' \\\n" +
                "  -H 'sec-ch-ua-platform: \"Windows\"' \\\n" +
                "  -H 'sec-fetch-dest: empty' \\\n" +
                "  -H 'sec-fetch-mode: cors' \\\n" +
                "  -H 'sec-fetch-site: same-site' \\\n" +
                "  -H 'user-agent: "+ userAgents[new Random().nextInt(0,userAgents.length)] +"' \\\n" +
                "  --data-raw '" + payload + "' \\\n" +
                "  --compressed", "  -H 'x-csrf-token: " + token + "' \\\n");
        CatalogSummary.count(120,proxy);
        return JSON.itemBatchStringToHashMap(itemsResponse.get("response").toString());
    }

    public static String getXCSRF_Token(VirtualBrowser browser) {
        try {
            HashMap<String, Object> tokenRequest = browser.curlToOpenWebsite("curl 'https://www.roblox.com/catalog?Category=1&salesTypeFilter=2' \\\n" +
                    "  -H 'authority: www.roblox.com' \\\n" +
                    "  -H 'accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7' \\\n" +
                    "  -H 'accept-language: en-US,en;q=0.9' \\\n" +
                    "  -H 'sec-ch-ua: \"Chromium\";v=\"110\", \"Not A(Brand\";v=\"24\", \"Google Chrome\";v=\"110\"' \\\n" +
                    "  -H 'sec-ch-ua-mobile: ?0' \\\n" +
                    "  -H 'sec-ch-ua-platform: \"Windows\"' \\\n" +
                    "  -H 'sec-fetch-dest: document' \\\n" +
                    "  -H 'sec-fetch-mode: navigate' \\\n" +
                    "  -H 'sec-fetch-site: none' \\\n" +
                    "  -H 'sec-fetch-user: ?1' \\\n" +
                    "  -H 'upgrade-insecure-requests: 1' \\\n" +
                    "  -H 'user-agent: "+ userAgents[new Random().nextInt(0,userAgents.length)] +"' \\\n" +
                    "  --compressed");
            return StringFilter.parseStringUsingRegex((String) tokenRequest.get("response"), "csrf-token\" data-token=\"(.*?)\""); // Remember it also saves cookies!
        } catch (IOException e) {
            TorInstance attachedTor = TorManager.getAttachedTorFromBrowser(browser);
            if (attachedTor != null) {
                TorManager.log(attachedTor,"problem getting XCSRF token, retrying!");
                attachedTor.changeCircuit();
                Main.threadSleep(500);
                return getXCSRF_Token(browser);
            }
            System.out.println("hey! Im get csrf toekn and Im returning null beause: " + e.getMessage());
            return null;
        }
    }
    public static List<Object> itemToInfo(long ID) {
        try {
            HashMap<String,Object> args = virtualBrowser.openWebsite("https://economy.roblox.com/v1/assets/" + ID + "/resale-data","GET",null,null,null,false,false);
            return JSON.itemToInfo(args.get("response").toString());
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Retrying in " + Main.getDefaultRetryTime() + " millis!");
            Main.threadSleep(Main.getDefaultRetryTime());
            return itemToInfo(ID);
        }
    }

    static final String[] userAgents = new String[]{
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:79.0) Gecko/20100101 Firefox/79.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:79.0) Gecko/20100101 Firefox/79.0",
            "Mozilla/5.0 (X11; Linux i686; rv:79.0) Gecko/20100101 Firefox/79.0",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 14_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (iPad; CPU OS 14_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (Windows NT 10.0; Trident/7.0; rv:11.0) like Gecko",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36"
    };

    public static class CatalogSummary {
        static int index = 0;
        static String[] ANSI_COLORS = new String[]{"\u001B[30m","\u001B[31m","\u001B[32m","\u001B[33m","\u001B[34m","\u001B[35m","\u001B[36m","\u001B[37m"};
        static HashMap<Proxy,String> proxToColor = new HashMap<>();
        static long startTime;
        static long endTime;
        static Date startDate;
        static long itemsScanned = 0;

        public static void init() {
            startTime = System.nanoTime();
            startDate = Date.from(Instant.now());
            System.out.print("\n");
        }
        public static void summarize() {
            endTime = System.nanoTime();
            int seconds = (int) ((endTime - startTime)/1e+9);
            int itemsScannedPerSecond = (int) (itemsScanned / seconds);
            System.out.print("\n\u001B[34m" + "CATALOG SCANNER SUMMARY:\n" + " Start Date: " + startDate + "\n End Date: " + Date.from(Instant.now()) + "\n Items Scanned: " + itemsScanned +
                    "\n Items Scanned Per Second: " + itemsScannedPerSecond + "\u001B[0m");
        }

        static int indexCount = -1;
        public static void count(int number, Proxy proxy) {
            indexCount++; if (indexCount % 130 == 0 && indexCount != 0) {System.out.print("\n");}
            if (number == 0) {System.out.print("*"); return;}

            if (RequestManager.colorRefresh.contains(proxy) && proxToColor.containsKey(proxy)) {
                String color = proxToColor.get(proxy);
                String newColor = null;
                proxToColor.remove(proxy);
                for (int i = 0; i < Arrays.stream(ANSI_COLORS).toList().size(); i++) {
                    if (Objects.equals(Arrays.stream(ANSI_COLORS).toList().get(i), color) && !(i+1 == Arrays.stream(ANSI_COLORS).toList().size())) {
                        newColor = ANSI_COLORS[i+1];
                    } else if (i+1 == Arrays.stream(ANSI_COLORS).toList().size()) {
                        newColor = ANSI_COLORS[0];
                    }
                }
                proxToColor.put(proxy,newColor);
                RequestManager.colorRefresh.remove(proxy);
            }

            itemsScanned = itemsScanned+number;
            if (proxy == null) {
                System.out.print("." + "\u001B[0m");
            } else {
                if (proxToColor.containsKey(proxy)) {System.out.print(proxToColor.get(proxy) + "." + "\u001B[0m");}
                else {
                    proxToColor.put(proxy,ANSI_COLORS[index]);
                    index++;
                    if (index >= ANSI_COLORS.length) {index = 0;}
                    System.out.print(proxToColor.get(proxy) + "." + "\u001B[0m");
                }
            }
        }
    }
}