package net.yeyito.connections;

import net.yeyito.Main;
import net.yeyito.VirtualBrowser;
import net.yeyito.util.JSON;
import net.yeyito.util.TextFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class LimitedTXTUpdater {
    private static VirtualBrowser virtualBrowser = new VirtualBrowser();
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

        }catch (IOException e) {
            new TextFile("src/main/resources/StackTrace.txt").writeString("\nCould not update limiteds.txt error: " + e.toString() + " will retry on next loop.\n");
        }
    }
}