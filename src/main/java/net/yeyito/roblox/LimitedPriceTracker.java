package net.yeyito.roblox;

import net.dv8tion.jda.internal.entities.channel.concrete.TextChannelImpl;
import net.dv8tion.jda.internal.managers.channel.concrete.TextChannelManagerImpl;
import net.yeyito.Main;
import net.yeyito.TextFile;
import net.yeyito.WebsiteScraper;
import net.yeyito.connections.DiscordBot;
import org.checkerframework.framework.qual.CFComment;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

// Class tracks the price of all limiteds
public class LimitedPriceTracker {
    public static HashMap<Long,Long> LimitedToPrice = new HashMap<>();
    public static HashMap<Long,List<Object>> LimitedToInfo = new HashMap<>();
    //List<Object> = String name, Long Price, Long RAP, Long Quantity_Sold, Long Original_Price, List<Long Price,Long Os.Time> Data Points

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
        HashMap<Long,Long> newLimitedToPrice = WebsiteScraper.itemBulkToPrice(itemIDs);

        String[] command = {"route","DELETE",WebsiteScraper.getHostIPfromURL("catalog.roblox.com")};
        Runtime.getRuntime().exec(command);

        System.out.println("Done Scanning!");
        for (Long key : newLimitedToPrice.keySet()) {
            if (!LimitedToPrice.containsKey(key)) {LimitedToPrice.put(key,newLimitedToPrice.get(key));}
            else {
                if (!Objects.equals(LimitedToPrice.get(key), newLimitedToPrice.get(key))) {

                    String sign = "";
                    double price_difference_percentage = 0;
                    String price_difference_string;

                    if (newLimitedToPrice.get(key) == null || LimitedToPrice.get(key) == null) {sign = "??";}
                    else if (newLimitedToPrice.get(key) >= LimitedToPrice.get(key)) {sign = "+"; price_difference_percentage = (double) Math.abs(LimitedToPrice.get(key) - newLimitedToPrice.get(key)) / LimitedToPrice.get(key);}
                    else if (newLimitedToPrice.get(key) < LimitedToPrice.get(key)) {sign = "-"; price_difference_percentage = (double) Math.abs(LimitedToPrice.get(key) - newLimitedToPrice.get(key)) / LimitedToPrice.get(key);}
                    price_difference_percentage = price_difference_percentage*100;

                    if (price_difference_percentage < 10) {price_difference_string = "Low";}
                    else if (price_difference_percentage < 20) {price_difference_string = "Medium";}
                    else if (price_difference_percentage < 40) {price_difference_string = "High";}
                    else if (price_difference_percentage < 80) {price_difference_string = "Extreme";}
                    else {price_difference_string = "Ludicrous";}

                    DecimalFormat decimalFormat = new DecimalFormat("#.#");
                    String formattedValue = decimalFormat.format(price_difference_percentage);

                    Main.discordBot.sendMessageOnRegisteredChannel("all-item-price-changes",
                            key + " | " + formattedValue + "%" + " | " + price_difference_string + "\n" +
                                    "```diff\n" +
                                    sign + "> "+ newLimitedToPrice.get(key) +"\n" +
                                    ">> "+ LimitedToPrice.get(key) +"\n" +
                                    "```",15);

                    LimitedToPrice.remove(key);
                    LimitedToPrice.put(key,newLimitedToPrice.get(key));
                }
            }
        }
    }
}
