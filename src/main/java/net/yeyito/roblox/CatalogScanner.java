package net.yeyito.roblox;

import net.yeyito.Main;
import net.yeyito.connections.TOR;
import net.yeyito.util.*;
import net.yeyito.VirtualBrowser;

import java.io.*;
import java.rmi.UnexpectedException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class CatalogScanner {
    static VirtualBrowser virtualBrowser = new VirtualBrowser();
    static boolean initComplete = false;
    public static class CatalogSummary {
        static int index = 0;
        static String[] ANSI_COLORS = new String[]{"\u001B[30m","\u001B[31m","\u001B[32m","\u001B[33m","\u001B[34m","\u001B[35m","\u001B[36m","\u001B[37m"};
        static HashMap<Connection,String> conToColor = new HashMap<>();
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
        public static void count(int number, Connection connection) {
            indexCount++; if (indexCount % 130 == 0 && indexCount != 0) {System.out.print("\n");}
            if (number == 0) {System.out.print("*"); return;}
            itemsScanned = itemsScanned+number;
            if (connection == null) {
                System.out.print("." + "\u001B[0m");
            } else {
                if (conToColor.containsKey(connection)) {System.out.print(conToColor.get(connection) + "." + "\u001B[0m");}
                else {
                    conToColor.put(connection,ANSI_COLORS[index]);
                    index++;
                    if (index >= ANSI_COLORS.length) {index = 0;}
                    System.out.print(conToColor.get(connection) + "." + "\u001B[0m");
                }
            }
        }
    }
    public static void scanLimiteds() {
        if (!initComplete) {CatalogSummary.init(); virtualBrowser.muteErrors(); initComplete = true;}

        AtomicReference<List<Long>> IDs = new AtomicReference<>(LimitedPriceTracker.getIDs());
        AtomicReference<List<List<Long>>> listOfIDsLists = new AtomicReference<>(LimitedPriceTracker.chunkIDs(IDs.get()));

        for (TOR tor : TOR.TORinstances) {
            CompletableFuture<Void> torAsync = CompletableFuture.runAsync(() -> {
                while (true) {
                    if (new Random().nextInt(0, 500) == 100) {IDs.set(LimitedPriceTracker.getIDs()); listOfIDsLists.set(LimitedPriceTracker.chunkIDs(IDs.get()));} // update these ~ every 500 requests
                    VirtualBrowser v2 = new VirtualBrowser();
                    v2.muteErrors();
                    Connection connection = new Connection(Connection.TYPE.PROXY, new String[]{"true"});
                    connection.connect(v2, tor);
                    AtomicReference<String> token = new AtomicReference<>(getXCSRF_Token(v2,tor));
                    Main.threadSleep(3750);
                    List<CompletableFuture<Void>> requestList = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        int finalI = i;
                        CompletableFuture<Void> requestAsync = CompletableFuture.runAsync(() -> {
                            Collections.shuffle(listOfIDsLists.get());
                            HashMap<Long, List<Object>> result = itemBulkToPriceRequest(listOfIDsLists.get().get(finalI), token.get(), connection, v2);
                            if (result != null) {
                                LimitedPriceTracker.limitedToInfoMerge(result);
                                tor.log(".");
                            } else {tor.log("*");}
                        });
                        requestList.add(requestAsync);
                        Main.threadSleep(500);
                    }

                    List<CompletableFuture<Void>> testAsyncList = new ArrayList<>();
                    for (CompletableFuture<Void> asyncRequest : requestList) {
                        CompletableFuture<Void> testAsync = CompletableFuture.runAsync(() -> {
                            try {
                                asyncRequest.get(10, TimeUnit.SECONDS);
                            } catch (InterruptedException | ExecutionException e) {
                                tor.log("\n Request async interrupted. \n");
                            } catch (TimeoutException e) {
                                asyncRequest.cancel(true);
                                tor.log("\n A Request took more than 10 seconds to complete. Skipping. \n");
                            }
                        });
                        testAsyncList.add(testAsync);
                    }
                    for (CompletableFuture<Void> cf: testAsyncList) {cf.join();}
                    connection.disconnect();
                }
            });
        }
    }

    public static HashMap<Long,List<Object>> itemBulkToPriceRequest(List<Long> IDs,String token,Connection connection,VirtualBrowser browser) {
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
                    "  -H 'sec-fetch-site: same-site' \\\n" +
                    "  -H 'user-agent: "+ userAgents[new Random().nextInt(0,userAgents.length)] +"' \\\n" +
                    "  --data-raw '" + payload + "' \\\n" +
                    "  --compressed", "  -H 'x-csrf-token: " + token + "' \\\n");
            CatalogSummary.count(120,connection);
            return JSON.itemBatchStringToHashMap(itemsResponse.get("response").toString());
        } catch (IOException e) {
            connection.torInstance.log("\n" + e.getMessage() + "\n");
            return null;
        }
    }
    public static String getXCSRF_Token(VirtualBrowser browser, TOR tor) {
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
            tor.log("\nProblem getting XCSRF token, voiding following requests!\n");
            return null;
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

    public static final String[] userAgents = new String[]{
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