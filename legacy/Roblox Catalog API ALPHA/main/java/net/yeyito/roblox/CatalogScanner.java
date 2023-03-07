package net.yeyito.roblox;

import net.yeyito.Main;
import net.yeyito.util.StringFilter;
import net.yeyito.VirtualBrowser;
import net.yeyito.connections.RouteManager;
import net.yeyito.util.JSON;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CatalogScanner {
    static VirtualBrowser virtualBrowser = new VirtualBrowser();
    static String XCSRF_token = "";
    static int requests_in_route = 0;
    public static void itemBulkToPrice(List<Long> IDs) {
        System.out.print("\nScanning");
        RouteManager.deleteRoute("catalog.roblox.com");
        XCSRF_token = getXCSRF_Token();

        if (IDs.size() <= 120) {
            LimitedPriceTracker.limitedToInfoMerge(itemBulkToPriceRequest(IDs));
        } else {
            int requestNumber = (IDs.size() + 119) / 120;
            List<List<Long>> listOfIDsLists = new ArrayList<>();

            for (int i = 0; i < requestNumber; i++) {
                int fromIndex = i * 120;
                int toIndex = Math.min(fromIndex + 120, IDs.size());
                List<Long> listOfIDs = IDs.subList(fromIndex, toIndex);
                listOfIDsLists.add(listOfIDs);
            }

            for (List<Long> IDs120: listOfIDsLists) {
                LimitedPriceTracker.limitedToInfoMerge(itemBulkToPriceRequest(IDs120));
            }
        }
        requests_in_route = 0; // Reset
    }

    public static HashMap<Long,List<Object>> itemBulkToPriceRequest(List<Long> IDs) {
        if (requests_in_route >= 10) {
            requests_in_route = 0;
            RouteManager.deleteRoute("catalog.roblox.com");
            RouteManager.addRoute("catalog.roblox.com", "192.168.0.1", 8);
            XCSRF_token = getXCSRF_Token(); // regenerate cookies
            Main.threadSleep(Main.getDefaultRetryTime()); // Wait so Token Validation doesn't Fail
        }

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

            HashMap<String, Object> itemsResponse = virtualBrowser.curlToOpenWebsite("curl 'https://catalog.roblox.com/v1/catalog/items/details' \\\n" +
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
                    "  --compressed", "  -H 'x-csrf-token: " + XCSRF_token + "' \\\n");
            requests_in_route++;
            System.out.print(".");
            return JSON.itemBatchStringToHashMap(itemsResponse.get("response").toString());
        } catch (IOException e) {
            Main.discordBot.sendMessageOnRegisteredChannel("all-item-price-changes",e.toString() + " error when route hasn't gotten 10 successful requests, retrying in " + Main.getDefaultRetryTime() + " millis! And reloading cookies.",0);
            Main.threadSleep(Main.getDefaultRetryTime());
            XCSRF_token = getXCSRF_Token();

            return itemBulkToPriceRequest(IDs); // Try Again with new cookies
        }
    }

    public static String getXCSRF_Token() {
        try {
            HashMap<String, Object> tokenRequest = virtualBrowser.curlToOpenWebsite("curl 'https://www.roblox.com/catalog?Category=1&salesTypeFilter=2' \\\n" +
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
            System.out.println("getXCSRF_Token Fail!");
            System.out.println(virtualBrowser.cookies);
            virtualBrowser.printCURL = true;

            Main.discordBot.sendMessageOnRegisteredChannel("all-item-price-changes",e.toString() + " retrying after" + Main.getDefaultRetryTime() + " millis!",0);
            Main.threadSleep(Main.getDefaultRetryTime());
            return getXCSRF_Token();
        }
    }
    public static List<Object> itemToInfo(long ID) {
        try {
            HashMap<String,Object> args = virtualBrowser.openWebsite("https://economy.roblox.com/v1/assets/" + ID + "/resale-data","GET",null,null,null,false,false);
            return JSON.itemToInfo(args.get("response").toString());
        }catch (IOException e) {
            Main.discordBot.sendMessageOnRegisteredChannel("all-item-price-changes",e.toString() + " could not retrieve item info of id " + ID + " retrying in " + Main.getDefaultRetryTime() + " millis!",0);
            Main.threadSleep(Main.getDefaultRetryTime());
            return itemToInfo(ID);
        }
    }

    @Deprecated static final String[] userAgents = new String[]{
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

}