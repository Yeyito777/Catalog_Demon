package net.yeyito.roblox;

import net.yeyito.Main;
import net.yeyito.VirtualBrowser;
import net.yeyito.connections.tor.TorInstance;
import net.yeyito.connections.tor.TorManager;
import net.yeyito.connections.tor.TorNetworkManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RequestManager {

    static HashMap<TorInstance,CompletableFuture<Void>> torToAsyncMap = new HashMap<>();
    static HashMap<CompletableFuture<Void>,Long> torAsyncToTimeStartedMap = new HashMap<>();
    static List<TorInstance> completedInstances;
    static List<Long> limitedItems;
    public static List<TorInstance> cantCancel = new ArrayList<>();

    static int torInstanceSecondsLimit = 45;
    static int requestSecondsLimit = 9;
    static int waitMillisAfterToken = 3750;

    public static void handleRequests() {
        CatalogScanner.CatalogSummary.init();
        completedInstances = new ArrayList<>(TorManager.TORinstances);
        limitedItems = LimitedPriceTracker.getAllLimitedsInTXT();
        while (true) {
            for (TorInstance tor : TorManager.TORinstances) {
                if (completedInstances.contains(tor)) {
                    System.out.println(tor + " is marked as completed, proceeding to assign it");
                    completedInstances.remove(tor);
                    assignLimitedsToTor(tor);
                } else {
                    if ((System.nanoTime() - torAsyncToTimeStartedMap.get(torToAsyncMap.get(tor))) / 1e+9 > torInstanceSecondsLimit && !cantCancel.contains(tor)) {
                        System.err.println(tor + " has exceeded its allowed execution time, cancelling it and assigning it");
                        try {torToAsyncMap.get(tor).cancel(true);} catch (CancellationException ignored) {}
                        assignLimitedsToTor(tor);
                    }
                }
            }
            LimitedPriceTracker.updateLimitedsTXT();
            limitedItems = LimitedPriceTracker.getAllLimitedsInTXT();
            Main.threadSleep(100);
        }
    }

    public static void assignLimitedsToTor(TorInstance tor) {
        CompletableFuture<Void> asyncTor = CompletableFuture.runAsync(() -> {
            System.out.println("Starting asyncTor");
            VirtualBrowser virtualBrowser = new VirtualBrowser(); virtualBrowser.muteErrors();
            virtualBrowser.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", tor.socksPort)));
            getToken(tor,virtualBrowser,0,false);
        });
        System.out.println("Removing old mappings and adding new ones!");
        torAsyncToTimeStartedMap.remove(torToAsyncMap.get(tor));
        torAsyncToTimeStartedMap.put(asyncTor,System.nanoTime());

        torToAsyncMap.remove(tor);
        torToAsyncMap.put(tor, asyncTor);
    }

    public static void getToken(TorInstance tor, VirtualBrowser virtualBrowser, int requestsInPreviousAttempt, boolean errorBefore) {
        AtomicReference<String> problem = new AtomicReference<>();

        String token = null;
        while (token == null) {
            System.out.println("Inside getToken for: " + tor);
            if (!errorBefore) {
                tor.changeCircuit();
            }
            System.out.println("Finished changing circuit!");
            token = CatalogScanner.getXCSRF_Token(virtualBrowser);
            System.out.println("Finished asigning token! " + token);
            Main.threadSleep(100);
        }
        Main.threadSleep(waitMillisAfterToken);
        System.out.println("Finished wait millis");
        // ** Assign requests ** //
        HashMap<CompletableFuture<Void>,Long> requestToTimeStartedMap = new HashMap<>();
        AtomicInteger successfulRequests = new AtomicInteger();
        successfulRequests.set(requestsInPreviousAttempt);
        for (int i = 0; i < 10; i++) {
            String finalToken = token;
            CompletableFuture<Void> request = CompletableFuture.runAsync(() -> {
                // ** REQUEST ** //
                List<Long> IDs120 = new ArrayList<>();
                for (int j = 0; j < 120; j++) {
                    boolean foundID = false;
                    while (!foundID) {
                        Long id = limitedItems.get(new Random().nextInt(0, limitedItems.size()));
                        if (!IDs120.contains(id)) {
                            IDs120.add(id);
                            foundID = true;
                        }
                    }
                }
                try {
                    LimitedPriceTracker.limitedToInfoMerge(CatalogScanner.itemBulkToPriceRequest(IDs120, finalToken,virtualBrowser.proxy,virtualBrowser));
                    successfulRequests.getAndIncrement();
                } catch (IOException e) {
                    System.out.println("Uh oh, problem with request " + e.getMessage());
                    if (e.getMessage().contains("429")) {problem.set("429");}
                    else if (e.getMessage().contains("end") || e.getMessage().contains("file") || e.getMessage().contains("File") || e.getMessage().contains("End")) {problem.set("endOfFile");}
                    else if (e.getMessage().contains("token") || e.getMessage().contains("Token")) {problem.set("tokenValid");}
                    else {problem.set("unknown");}
                }
            });
            requestToTimeStartedMap.put(request,System.nanoTime());
        }
        while (successfulRequests.get() < 10 + requestsInPreviousAttempt && problem.get() == null) {
            for (CompletableFuture<Void> request: requestToTimeStartedMap.keySet()) {
                if ((System.nanoTime() - requestToTimeStartedMap.get(request))/1e+9 > requestSecondsLimit) {
                    try {
                        request.cancel(true);
                        problem.set("timeout");
                        System.out.println("Problem timeout :/");
                    } catch (CompletionException ignored) {}
                }
            }
            Main.threadSleep(100);
        }
        colorRefresh.add(virtualBrowser.proxy);
        if (problem.get() == null) {
            completedInstances.add(tor);
        } else {
            System.out.println("Encountered problem: " + problem.get());
            if (problem.get().equals("timeout")) {TorNetworkManager.blockedExitNodes.put(tor.currentIP,Long.MAX_VALUE); getToken(tor,virtualBrowser,successfulRequests.get(),false);}
            else if (problem.get().equals("endOfFile")) {
                if (!errorBefore) {getToken(tor,virtualBrowser,successfulRequests.get(),true);}
                else {
                    getToken(tor,virtualBrowser,0,false);
                }
            }
            else if (problem.get().equals("tokenValid")) {
                if (!errorBefore) {getToken(tor,virtualBrowser,successfulRequests.get(),true);}
                else {
                    getToken(tor,virtualBrowser,0,false);
                }
            }
            else if (problem.get().equals("429")) {TorNetworkManager.blockedExitNodes.put(tor.currentIP,Long.MAX_VALUE); getToken(tor,virtualBrowser,successfulRequests.get(),false);}
            else if (problem.get().equals("unknown")) {getToken(tor,virtualBrowser,successfulRequests.get(),false);}
        }
    }

    public static List<Proxy> colorRefresh = new ArrayList<>();
}
