package net.yeyito.roblox;

import net.yeyito.Main;
import net.yeyito.VirtualBrowser;
import net.yeyito.util.JSON;
import net.yeyito.util.TextFile;
import net.yeyito.connections.DiscordBot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

// Class tracks the price of all limiteds
public class LimitedPriceTracker {
    public static HashMap<Long,List<Object>> LimitedToInfo = new HashMap<>();
    //List<Object> = String name, Long Price, Long RAP, Long Original_Price, Long Quantity_Sold, List<Long Price,Long Os.Time> Data Points

    public static List<Long> getIDs() {
        Scanner scanner = scanLinesRetryable();
        List<Long> itemIDs = new ArrayList<Long>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            itemIDs.add(Long.parseLong(line));
        }
        scanner.close();
        updateIDs();
        return itemIDs;
    }

    public static List<List<Long>> chunkIDs(List<Long> IDs) {
        int requestNumber = (IDs.size() + 119) / 120;
        List<List<Long>> listOfIDsLists = new ArrayList<>();

        for (int i = 0; i < requestNumber; i++) {
            int fromIndex = i * 120;
            int toIndex = Math.min(fromIndex + 120, IDs.size());
            List<Long> listOfIDs = IDs.subList(fromIndex, toIndex);
            listOfIDsLists.add(listOfIDs);
        }
        for (int i = 0; i < listOfIDsLists.size(); i++) {
            List<Long> listOfIDs120 = listOfIDsLists.get(i);
            if (listOfIDs120.size() < 120) {
                int itemsToAdd = 120 - listOfIDs120.size();
                List<Long> newListOfIDs120 = new ArrayList<>(listOfIDs120);
                for (int j = 0; j < itemsToAdd; j++) {
                    newListOfIDs120.add(listOfIDsLists.get(1).get(j));
                }
                listOfIDsLists.set(i, newListOfIDs120);
            }
        }
        return listOfIDsLists;
    }

    private static final VirtualBrowser virtualBrowser = new VirtualBrowser();
    public static void updateIDs() {
        CompletableFuture.runAsync(() -> {
            try {
                HashMap<String, Object> args = virtualBrowser.openWebsite("https://rblx.trade/api/v1/catalog/all", "GET", null, null, null, false, false);
                TextFile limitedsTXT = new TextFile("src/main/resources/Limiteds.txt");
                List<String> IDs = JSON.getItemIdsFromRolimonAPI(args.get("response").toString());

                for (String id : IDs) {
                    if (id != null && !limitedsTXT.findString(id)) {
                        limitedsTXT.writeString(id + "\n");
                        System.out.println("Added new limited id: " + id);
                    }
                }

            } catch (IOException e) {
                Main.discordBot.sendMessageOnRegisteredChannel("all-item-sales", "Could not update limiteds.txt error: " + e.toString() + " will retry on next loop.", 0);
            }
        });
    }
    public static Scanner scanLinesRetryable() {
        try {
            return new Scanner(new File("src/main/resources/Limiteds.txt"));
        } catch (FileNotFoundException e) {
            Main.discordBot.sendMessageOnRegisteredChannel("all-item-price-changes"," could not scan the lines of src/main/resources/Limiteds.txt! retrying in " + Main.getDefaultRetryTime() + " millis!",0);
            Main.threadSleep(Main.getDefaultRetryTime());
            return scanLinesRetryable();
        }
    }
    public static void shuffleLinesRetryable() {
        try {
            new TextFile("src/main/resources/Limiteds.txt").shuffleLines();
        } catch (IOException e) {
            Main.discordBot.sendMessageOnRegisteredChannel("all-item-price-changes"," could not shuffle the lines of src/main/resources/Limiteds.txt! retrying in " + Main.getDefaultRetryTime() + " millis!",0);
            Main.threadSleep(Main.getDefaultRetryTime());
            shuffleLinesRetryable();
        }
    }
    public static void limitedToInfoMerge(HashMap<Long,List<Object>> newLimitedToInfo) {
        for (Long key : newLimitedToInfo.keySet()) {
            if (!LimitedToInfo.containsKey(key)) {LimitedToInfo.put(key,newLimitedToInfo.get(key));}
            else {
                if (!Objects.equals((Long) LimitedToInfo.get(key).get(1), (Long) newLimitedToInfo.get(key).get(1))) {

                    String sign = "";
                    double price_difference_percentage = 0;
                    String price_difference_string;
                    String ping_role = "";

                    if (newLimitedToInfo.get(key).get(1) == null || LimitedToInfo.get(key).get(1) == null) {new TextFile("src/main/resources/StackTrace.txt").writeString("Item price is null, ignoring!"); return;}
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

                    for (Object o: CatalogScanner.itemToInfo(key)) {
                        newLimitedToInfo.get(key).add(o);
                    }

                    Main.discordBot.sendMessageOnRegisteredChannel("all-item-price-changes",
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