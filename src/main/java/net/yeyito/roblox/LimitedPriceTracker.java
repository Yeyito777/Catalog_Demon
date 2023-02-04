package net.yeyito.roblox;

import net.yeyito.WebsiteScraper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

// Class tracks the price of all limiteds
public class LimitedPriceTracker {
    public static HashMap<Long,Long> LimitedToPrice = new HashMap<>();

    public static void updatePrices() throws IOException {
        Scanner scanner = new Scanner(new File("src/main/resources/Limiteds.txt"));
        List<Long> itemIDs = new ArrayList<Long>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            itemIDs.add(Long.parseLong(line));
        }
        scanner.close();
        LimitedToPrice = WebsiteScraper.itemBulkToPrice(itemIDs);
        System.out.println(LimitedToPrice.keySet().size());
        System.out.println(LimitedToPrice);
    }
}
