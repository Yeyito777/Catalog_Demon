package net.yeyito;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TextFile {
    File textFile;
    List<Thread> threads = new ArrayList<Thread>();
    List<Boolean> compleatedThreads = new ArrayList<Boolean>();
    long IDs_Checked = 0;

    public TextFile(String name) {
        this.textFile = new File(name);
        try {
            if (!textFile.exists()) {
                textFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeString(String data) {
        try {
            Path path = Paths.get(textFile.getName());
            Files.write(path, data.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean findString(String input) {
        try {
            Scanner scanner = new Scanner(this.textFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains(input)) {
                    scanner.close();
                    return true;
                }
            }
            scanner.close();
            return false;
        } catch (FileNotFoundException e) {e.printStackTrace(); return false;}
    }
    @Deprecated public void logItemInfo(long ID) throws IOException {
        String websiteInfo = null;
        try {websiteInfo = WebsiteScraper.scrapeRobloxCatalogID(ID);} catch (IOException e) {e.printStackTrace();}
        String robux = StringFilter.extractRobuxFromHTML(websiteInfo);
        String name = StringFilter.extractItemNameFromHTML(websiteInfo);
        String status = StringFilter.extractStatusFromHTML(websiteInfo);
        if (robux == null || name == null) {System.out.println("Not An Item!"); return;}

        String orderedInfo = ID + " " + robux + " " + status + " [" + name + "]" + "\n";
        System.out.println(orderedInfo);
        this.writeString(orderedInfo);
    }

    @Deprecated public void logItemRangeInfoThread(long IDstart, long IDend) {
        Thread thread = new Thread(() -> {
            for (long ID = IDstart; ID < IDend; ID++) {
                String websiteInfo = null;
                try {websiteInfo = WebsiteScraper.scrapeRobloxCatalogID(ID);} catch (IOException e) {e.printStackTrace();}
                String robux = StringFilter.extractRobuxFromHTML(websiteInfo);
                String name = StringFilter.extractItemNameFromHTML(websiteInfo);
                String status = StringFilter.extractStatusFromHTML(websiteInfo);
                IDs_Checked = IDs_Checked+1;
                if (robux != null && name != null) {
                    String orderedInfo = ID + " " + robux + " " + status + " [" + name + "]" + "\n";
                    System.out.println(orderedInfo);
                    this.writeString(orderedInfo);
                }
                System.out.println(IDs_Checked);
            }
            System.out.println("Thread " + compleatedThreads.size() + " finished execution.");
            compleatedThreads.add(true);
            checkThreads();
        });
        thread.start();
        threads.add(thread);
    }
    @Deprecated public void logThreadedItemRangeInfo(long IDstart, long IDend, int Threads) {
        long numberofIDs = IDend - IDstart;
        for (int i = 0; i < Threads; i++) {
            if (i+1 >= Threads) {
                logItemRangeInfoThread(IDstart + (numberofIDs/Threads)*i, IDend);
                //System.out.println("Thread " + i + " was assigned Ids: " + IDstart + (numberofIDs/Threads)*i + " - " + IDend);
                return;
            }
            //System.out.println("Thread " + i + " was assigned Ids: " + IDstart + (numberofIDs/Threads)*i + " - " + IDstart + (numberofIDs/Threads)*(i+1));
                logItemRangeInfoThread(IDstart + (numberofIDs/Threads)*i, IDstart + (numberofIDs/Threads)*(i+1));
        }
    }
    public void checkThreads() {
        if (threads.size() == compleatedThreads.size()) {
            for (Thread thread: threads) {
                thread.interrupt();
            }
            System.out.println(IDs_Checked);
        }
    }
}
