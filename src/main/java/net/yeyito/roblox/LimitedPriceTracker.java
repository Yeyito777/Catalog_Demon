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

    public static List<Long> getAllLimitedsInTXT() {
        shuffleLinesRetryable();
        Scanner scanner = scanLinesRetryable();
        List<Long> itemIDs = new ArrayList<Long>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            itemIDs.add(Long.parseLong(line));
        }
        scanner.close();
        return itemIDs;
    }
    public static Scanner scanLinesRetryable() {
        try {
            return new Scanner(new File("src/main/resources/Limiteds.txt"));
        } catch (FileNotFoundException e) {
            System.err.println("could not scan the lines of src/main/resources/Limiteds.txt! retrying in " + Main.getDefaultRetryTime() + " millis!");
            Main.threadSleep(Main.getDefaultRetryTime());
            return scanLinesRetryable();
        }
    }
    public static void shuffleLinesRetryable() {
        try {
            new TextFile("src/main/resources/Limiteds.txt").shuffleLines();
        } catch (IOException e) {
            System.err.println("could not shuffle the lines of src/main/resources/Limiteds.txt! retrying in " + Main.getDefaultRetryTime() + " millis!");
            Main.threadSleep(Main.getDefaultRetryTime());
            shuffleLinesRetryable();
        }
    }

    private static final VirtualBrowser virtualBrowser = new VirtualBrowser();
    public static void updateLimitedsTXT() {
        try {
            HashMap<String,Object> args = virtualBrowser.openWebsite("https://rblx.trade/api/v1/catalog/all","GET",null,null,null,false,false);
            TextFile limitedsTXT = new TextFile("src/main/resources/Limiteds.txt");
            List<String> IDs = JSON.getItemIdsFromRolimonAPI(args.get("response").toString());

            for (String id: IDs) {
                if (id != null && !limitedsTXT.findString(id)) {
                    limitedsTXT.writeString(id + "\n");
                    System.out.println("Added new limited id: " + id);
                }
            }

        }catch (IOException ignored) {}
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

                    if (newLimitedToInfo.get(key).get(1) == null || LimitedToInfo.get(key).get(1) == null) {sign = "??";}
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

                    Main.discordBot.sendMessageOnRegisteredChannels(
                        key + " | " + formattedValue + "%" + " | " + price_difference_string + ping_role + "\n" +
                                "```diff\n" +
                                ">> "+ LimitedToInfo.get(key).get(1) +"\n" +
                                sign + "> "+ newLimitedToInfo.get(key).get(1) +"\n" +
                                "```" + "```java\n" +
                                "Name: " + "\"" + newLimitedToInfo.get(key).get(0) + "\"\n" +
                                "RAP: " + newLimitedToInfo.get(key).get(2) +"```",0
                    );

                    LimitedToInfo.remove(key);
                    LimitedToInfo.put(key,newLimitedToInfo.get(key));
                }
            }
        }
    }
}