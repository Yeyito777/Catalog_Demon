package net.yeyito.roblox;

import com.beust.jcommander.internal.Nullable;
import net.yeyito.Main;
import net.yeyito.connections.ProxyUtil;
import net.yeyito.util.StringFilter;
import net.yeyito.VirtualBrowser;
import net.yeyito.util.JSON;
import net.yeyito.util.TextFile;

import java.io.*;
import java.net.Proxy;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class CatalogScanner {
    static VirtualBrowser virtualBrowser = new VirtualBrowser();
    static boolean initComplete = false;

    public static class CatalogSummary {
        static int index = 0;
        static int indexDot = 0;
        static String[] ANSI_COLORS = new String[]{"\u001B[30m","\u001B[31m","\u001B[32m","\u001B[33m","\u001B[34m","\u001B[35m","\u001B[36m","\u001B[37m"};
        static HashMap<Integer,String> conToColor = new HashMap<>();
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

        public static void count(int number,Integer id) {
            itemsScanned = itemsScanned+number;

            if (indexDot % 140 == 0 && indexDot != 0) {System.out.print("\n");}
            indexDot++;

            if (id == null) {
                System.out.print("." + "\u001B[0m");
            } else {
                if (conToColor.containsKey(id)) {System.out.print(conToColor.get(id) + "." + "\u001B[0m");}
                else {
                    conToColor.put(id,ANSI_COLORS[index]);
                    index++;
                    if (index >= ANSI_COLORS.length) {index = 0;}
                    System.out.print(conToColor.get(id) + "." + "\u001B[0m");
                }
            }
        }
        public static void error() {
            if (indexDot % 140 == 0 && indexDot != 0) {System.out.print("\n");}
            indexDot++;
            System.out.print("*");
        }
    }

    static String token = null;
    public static void itemBulkToPrice(List<Long> IDs) {
        if (!initComplete) {
            CatalogSummary.init();
            virtualBrowser.muteErrors();
            initComplete = true;
            new TextFile("src/main/resources/StackTrace.txt").deleteAllText();
            new TextFile("src/main/resources/Logs/ProxyLogs.txt").deleteAllText();
        }

        int requestNumber = (IDs.size() + 119) / 120;
        List<List<Long>> listOfIDsLists = new ArrayList<>();

        for (int i = 0; i < requestNumber; i++) {
            int fromIndex = i * 120;
            int toIndex = Math.min(fromIndex + 120, IDs.size());
            List<Long> listOfIDs = IDs.subList(fromIndex, toIndex);
            listOfIDsLists.add(listOfIDs);
        }

        VirtualBrowser v2 = new VirtualBrowser(); v2.muteErrors();
        if (token == null) {getXCSRF_Token(v2);}

        for (int i = 0; i < 10; i++) {
            try {
                LimitedPriceTracker.limitedToInfoMerge(Objects.requireNonNull(itemBulkToPriceRequest(listOfIDsLists.get(i), token, v2)));
                Main.threadSleep(500);
            } catch (Exception e) {
                Main.threadSleep(5000);
                token = getXCSRF_Token(v2);
            }
        }
    }

    public static HashMap<Long,List<Object>> itemBulkToPriceRequest(List<Long> IDs, String token, VirtualBrowser browser) {
        try {
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
                    "  -H 'cookie: " + Main.secCookie + "' \\\n" +
                    "  -H 'sec-fetch-site: same-site' \\\n" +
                    "  --data-raw '" + payload + "' \\\n" +
                    "  --compressed", "  -H 'x-csrf-token: " + token + "' \\\n");
            CatalogSummary.count(120,ProxyUtil.getProxyCode(browser.proxy));
            new TextFile("src/main/resources/Logs/ProxyLogs.txt").writeString(".");

            return JSON.itemBatchStringToHashMap(itemsResponse.get("response").toString());
        } catch (IOException e) {
            CatalogSummary.error(); // *
            new TextFile("src/main/resources/Logs/ProxyLogs.txt").writeString("*");
            new TextFile("src/main/resources/StackTrace.txt").writeString(browser.proxy + " " + e.getMessage() + "\n");
            return null;
        }
    }

    public static String getXCSRF_Token(VirtualBrowser browser) {
        new TextFile("src/main/resources/Logs/ProxyLogs.txt").writeString("\nUsing Proxy: " + browser.proxy);
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
                    "  -H 'cookie: " + Main.secCookie + "' \\\n" +
                    "  -H 'sec-fetch-user: ?1' \\\n" +
                    "  -H 'upgrade-insecure-requests: 1' \\\n" +
                    "  --compressed");
            return StringFilter.parseStringUsingRegex((String) tokenRequest.get("response"), "csrf-token\" data-token=\"(.*?)\""); // Remember it also saves cookies!
        } catch (IOException e) {
            new TextFile("src/main/resources/StackTrace.txt").writeString(browser.proxy + " " + e.getMessage() + "\n");
            return null;
        }
    }
    public static List<Object> itemToInfo(long ID) {
        if (ID == 0) {return null;}
        try {
            HashMap<String,Object> args = virtualBrowser.openWebsite("https://economy.roblox.com/v1/assets/" + ID + "/resale-data","GET",null,null,null,false,false);
            return JSON.itemToInfo(args.get("response").toString());
        }catch (IOException e) {
            new TextFile("src/main/resources/StackTrace.txt").writeString("\nCould not retrieve item info of id " + ID + "\n");
            Main.threadSleep(Main.getDefaultRetryTime()/5);
            return itemToInfo(ID);
        }
    }
}