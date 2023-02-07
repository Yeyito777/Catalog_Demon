package net.yeyito.roblox;

import net.dv8tion.jda.internal.entities.channel.concrete.TextChannelImpl;
import net.dv8tion.jda.internal.managers.channel.concrete.TextChannelManagerImpl;
import net.yeyito.Main;
import net.yeyito.TextFile;
import net.yeyito.WebsiteScraper;
import net.yeyito.connections.DiscordBot;
import net.yeyito.util.JSON;
import org.checkerframework.framework.qual.CFComment;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

// Class tracks the price of all limiteds
public class LimitedPriceTracker {
    public static HashMap<Long,List<Object>> LimitedToInfo = new HashMap<>();
    //List<Object> = String name, Long Price, Long RAP, Long Original_Price, Long Quantity_Sold, List<Long Price,Long Os.Time> Data Points

    public static void updatePrices() throws IOException {
        new TextFile("src/main/resources/Limiteds.txt").shuffleLines();
        Scanner scanner = new Scanner(new File("src/main/resources/Limiteds.txt"));
        List<Long> itemIDs = new ArrayList<Long>();

        int currentLine = 0;
        int maxLine = 2400;
        while (scanner.hasNextLine() && currentLine < maxLine) {
            String line = scanner.nextLine();
            itemIDs.add(Long.parseLong(line));
            currentLine++;
        }
        scanner.close();
        HashMap<Long,List<Object>> newLimitedToInfo = WebsiteScraper.itemBulkToPrice(itemIDs);

        String[] command = {"route","DELETE",WebsiteScraper.getHostIPfromURL("catalog.roblox.com")};
        Runtime.getRuntime().exec(command);

        System.out.println("Done Scanning!");

        for (Long key : newLimitedToInfo.keySet()) {
            if (!LimitedToInfo.containsKey(key)) {LimitedToInfo.put(key,newLimitedToInfo.get(key));}
            else {
                if (!Objects.equals((Long) LimitedToInfo.get(key).get(1), (Long) newLimitedToInfo.get(key).get(1))) {

                    String sign = "";
                    double price_difference_percentage = 0;
                    String price_difference_string;

                    if (newLimitedToInfo.get(key).get(1) == null || LimitedToInfo.get(key).get(1) == null) {sign = "??";}
                    else if ((Long) newLimitedToInfo.get(key).get(1) >= (Long) LimitedToInfo.get(key).get(1)) {sign = "+"; price_difference_percentage = (double) Math.abs((Long) LimitedToInfo.get(key).get(1) - (Long) newLimitedToInfo.get(key).get(1)) / (Long) LimitedToInfo.get(key).get(1);}
                    else if ((Long) newLimitedToInfo.get(key).get(1) < (Long) LimitedToInfo.get(key).get(1)) {sign = "-"; price_difference_percentage = (double) Math.abs((Long) LimitedToInfo.get(key).get(1) - (Long) newLimitedToInfo.get(key).get(1)) / (Long) LimitedToInfo.get(key).get(1);}
                    price_difference_percentage = price_difference_percentage*100;

                    if (price_difference_percentage < 10) {price_difference_string = "Low";}
                    else if (price_difference_percentage < 20) {price_difference_string = "Medium";}
                    else if (price_difference_percentage < 40) {price_difference_string = "High";}
                    else if (price_difference_percentage < 80) {price_difference_string = "Extreme";}
                    else {price_difference_string = "Ludicrous";}

                    DecimalFormat decimalFormat = new DecimalFormat("#.#");
                    String formattedValue = decimalFormat.format(price_difference_percentage);

                    for (Object o: WebsiteScraper.itemToInfo(key)) {
                        newLimitedToInfo.get(key).add(o);
                    }

                    Main.discordBot.sendMessageOnRegisteredChannel("all-item-price-changes",
                            key + " | " + formattedValue + "%" + " | " + price_difference_string + "\n" +
                                    "```diff\n" +
                                    ">> "+ LimitedToInfo.get(key).get(1) +"\n" +
                                    sign + "> "+ newLimitedToInfo.get(key).get(1) +"\n" +
                                    "```" + "```java\n" +
                                    "Name: " + "\"" + newLimitedToInfo.get(key).get(0) + "\"\n" +
                                    "RAP: " + newLimitedToInfo.get(key).get(2) +"```",15);

                    LimitedToInfo.remove(key);
                    LimitedToInfo.put(key,newLimitedToInfo.get(key));
                }
            }
        }
    }
}
