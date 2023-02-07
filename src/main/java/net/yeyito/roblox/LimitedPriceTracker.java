package net.yeyito.roblox;

import net.yeyito.TextFile;
import net.yeyito.WebsiteScraper;

import java.io.File;
import java.io.IOException;
import java.util.*;

// Class tracks the price of all limiteds
public class LimitedPriceTracker {
    public static HashMap<Long,Long> LimitedToPrice = new HashMap<>();

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

        for (Long key : newLimitedToPrice.keySet()) {
            if (!LimitedToPrice.containsKey(key)) {LimitedToPrice.put(key,newLimitedToPrice.get(key));}
            else {
                if (!Objects.equals(LimitedToPrice.get(key), newLimitedToPrice.get(key))) {
                    System.out.println("Item: " + key + " new price: " + newLimitedToPrice.get(key));
                    LimitedToPrice.remove(key);
                    LimitedToPrice.put(key,newLimitedToPrice.get(key));
                }
            }
        }
        System.out.println("Size of limiteds tracked = " + LimitedToPrice.keySet().size());
    }
}
