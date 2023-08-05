package net.yeyito.roblox;

import net.yeyito.Main;
import net.yeyito.connections.ProxyUtil;
import net.yeyito.util.DeltaTime;
import net.yeyito.util.TextFile;
import net.yeyito.connections.DiscordBot;
import org.json.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Proxy;
import java.text.DecimalFormat;
import java.util.*;

// Class tracks the price of all limiteds
public class LimitedPriceTracker {
    public static HashMap<Long,List<Object>> LimitedToInfo = new HashMap<>();
    //List<Object> = String name, Long Price, Long RAP, Long Original_Price, Long Quantity_Sold, JSONArray Price_Data_Points

    public static void updatePrices() {
        Scanner scanner = scanLinesRetryable();
        List<Long> itemIDs = new ArrayList<Long>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            itemIDs.add(Long.parseLong(line));
        }
        scanner.close();
        System.out.print("\n");

        for (Proxy proxy : ProxyUtil.initAvailableProxies()) {
            if (proxy == null) {
                Runnable scan = new CatalogScanner(itemIDs, Main.secCookie);
                Thread thread = new Thread(scan);
                thread.start();
            } else {
                Runnable scan = new CatalogScanner(itemIDs, proxy);
                Thread thread = new Thread(scan);
                thread.start();
            }
        }
    }
    public static Scanner scanLinesRetryable() {
        try {
            return new Scanner(new File("src/main/resources/Limiteds.txt"));
        } catch (FileNotFoundException e) {
            new TextFile("src/main/resources/StackTrace.txt").writeString("\ncould not scan the lines of src/main/resources/Limiteds.txt! error: "+ e.getMessage() + "\n");
            Main.threadSleep(Main.getDefaultRetryTime());
            return scanLinesRetryable();
        }
    }

    synchronized public static void limitedToInfoMerge(HashMap<Long,List<Object>> newLimitedToInfo) {
        for (Long key : newLimitedToInfo.keySet()) {
            if (!LimitedToInfo.containsKey(key)) {LimitedToInfo.put(key,newLimitedToInfo.get(key));}
            else {
                if (!Objects.equals((Long) LimitedToInfo.get(key).get(1), (Long) newLimitedToInfo.get(key).get(1))) {
                    String sign = "";
                    double price_difference_percentage = 0;
                    String price_difference_string;
                    String ping_role = "";

                    if (newLimitedToInfo.get(key).get(1) == null || LimitedToInfo.get(key).get(1) == null) {new TextFile("src/main/resources/StackTrace.txt").writeString("\n an item's price is null, voiding it! \n"); return;} // If it updates to null just return
                    else if ((Long) newLimitedToInfo.get(key).get(1) >= (Long) LimitedToInfo.get(key).get(1)) {sign = "+"; price_difference_percentage = (double) Math.abs((Long) LimitedToInfo.get(key).get(1) - (Long) newLimitedToInfo.get(key).get(1)) / (Long) LimitedToInfo.get(key).get(1);}
                    else if ((Long) newLimitedToInfo.get(key).get(1) < (Long) LimitedToInfo.get(key).get(1)) {sign = "-"; price_difference_percentage = (double) Math.abs((Long) LimitedToInfo.get(key).get(1) - (Long) newLimitedToInfo.get(key).get(1)) / (Long) LimitedToInfo.get(key).get(1);}
                    price_difference_percentage = price_difference_percentage*100;

                    if (price_difference_percentage < 10) {price_difference_string = "Low";}
                    else if (price_difference_percentage < 25) {price_difference_string = "Medium";}
                    else if (price_difference_percentage < 40) {price_difference_string = "High"; ping_role = " | " + DiscordBot.High_Role + " | https://www.roblox.com/catalog/"+key;}
                    else if (price_difference_percentage < 80) {price_difference_string = "Extreme"; ping_role = " | " + DiscordBot.Extreme_Role + " | https://www.roblox.com/catalog/"+key;}
                    else {price_difference_string = "Ludicrous";  ping_role = " | " + DiscordBot.Ludicrous_Role + " | https://www.roblox.com/catalog/"+key;}

                    if (sign.equals("+")) {ping_role = "";} // Only ping when down
                    if (newLimitedToInfo.get(key).get(1) != null && (Long) newLimitedToInfo.get(key).get(1) >= 10000) {ping_role = "";} // Only ping when under 10k

                    DecimalFormat decimalFormat = new DecimalFormat("#.#");
                    String formattedValue = decimalFormat.format(price_difference_percentage);

                    // Instantaneous buy function
                    try {
                        if ((Long) newLimitedToInfo.get(key).get(1) < 21) {
                            ItemManager.buyItem(key,null, (Long) newLimitedToInfo.get(key).get(1));
                        }} catch (Exception e) {e.printStackTrace();}
                    // Item data
                    for (Object o: Objects.requireNonNull(CatalogScanner.itemToInfo(key))) {
                        newLimitedToInfo.get(key).add(o);
                    }
                    // Informed buy function
                    try {
                        if ((Long) newLimitedToInfo.get(key).get(1) < (Integer) newLimitedToInfo.get(key).get(2) / 2) {
                            ItemManager.buyItem(key,(JSONArray) newLimitedToInfo.get(key).get(5), (Long) newLimitedToInfo.get(key).get(1));
                        }} catch (Exception e) {e.printStackTrace();}

                    // Sending message
                    Main.discordBot.sendMessageOnRegisteredChannels(
                            key + " | " + formattedValue + "%" + " | " + price_difference_string + ping_role + "\n" +
                                    "```diff\n" +
                                    ">> "+ LimitedToInfo.get(key).get(1) +"\n" +
                                    sign + "> "+ newLimitedToInfo.get(key).get(1) +"\n" +
                                    "```" + "```java\n" +
                                    "Name: " + "\"" + newLimitedToInfo.get(key).get(0) + "\"\n" +
                                    "RAP: " + newLimitedToInfo.get(key).get(2) +"```",0);

                    LimitedToInfo.remove(key);
                    LimitedToInfo.put(key,newLimitedToInfo.get(key));
                }
            }
        }
    }
}