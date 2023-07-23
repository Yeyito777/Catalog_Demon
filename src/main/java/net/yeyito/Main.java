package net.yeyito;

import net.dv8tion.jda.api.entities.Activity;
import net.yeyito.connections.*;
import net.yeyito.roblox.CatalogScanner;
import net.yeyito.roblox.LimitedPriceTracker;
import net.yeyito.util.TextFile;
import org.checkerframework.checker.units.qual.C;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.util.*;

public class Main {
    public static DiscordBot discordBot = new DiscordBot("MTA3MTExNzYzMjcyNTU5NDE4Mg.GVbLmT.gZBzQ6dteuapWbNa39cCOCklIirsOho6KUkBFA", Activity.playing("no games"));
    public static String secCookie = ".ROBLOSECURITY=_|WARNING:-DO-NOT-SHARE-THIS.--Sharing-this-will-allow-someone-to-log-in-as-you-and-to-steal-your-ROBUX-and-items.|_2168C24BD79C06F8AB023E64E7B35354E9B3E130BC08660C221EA49B97444B8C1F48F6F51A55FE5125BC2FAB904C9E23E1BD5363F17EB23050E4F1830140BA14A261949E4242CAC44C6B2A31C62868C3267F8BECC959553A8D8EDC6069282544213539C3BE698C4D7A853A0CFE4CEB74BA46660070199D9374835520F3EC919AFE6240B25C9CA896C5EFDDAC2EA71B661FC648D9F407163885E1B384E61581B600A8B6411A51F04137A376261C0FFE15A14562971F3478AED607539E887171D53F0D71BBA91D408AC9F5E4ACE9967FB1FA57B4CAFC5973916ABD41BCB423E3174DECF41B931D47668FBB633C6470EF617DD8DDC4684E7CD0CC5279009A811DEBFCAB473AB9ADF9E498256346C5B6AE6C5DA372762FC465C76E1C2FE876D31B331976C4869C36AE954D690F4DC705B5D9FB4FB0DF146EBDEFD9A3B4255A612E3F0FEE0ED338C5681A1E55131788E96E4D4029C1953049DC990B67443BF86EE3A6FE2BD227172F96E44CD0FB63F7D62F6E44B8C072;";
    public static String[] secCookies = {
            ".ROBLOSECURITY=_|WARNING:-DO-NOT-SHARE-THIS.--Sharing-this-will-allow-someone-to-log-in-as-you-and-to-steal-your-ROBUX-and-items.|_2168C24BD79C06F8AB023E64E7B35354E9B3E130BC08660C221EA49B97444B8C1F48F6F51A55FE5125BC2FAB904C9E23E1BD5363F17EB23050E4F1830140BA14A261949E4242CAC44C6B2A31C62868C3267F8BECC959553A8D8EDC6069282544213539C3BE698C4D7A853A0CFE4CEB74BA46660070199D9374835520F3EC919AFE6240B25C9CA896C5EFDDAC2EA71B661FC648D9F407163885E1B384E61581B600A8B6411A51F04137A376261C0FFE15A14562971F3478AED607539E887171D53F0D71BBA91D408AC9F5E4ACE9967FB1FA57B4CAFC5973916ABD41BCB423E3174DECF41B931D47668FBB633C6470EF617DD8DDC4684E7CD0CC5279009A811DEBFCAB473AB9ADF9E498256346C5B6AE6C5DA372762FC465C76E1C2FE876D31B331976C4869C36AE954D690F4DC705B5D9FB4FB0DF146EBDEFD9A3B4255A612E3F0FEE0ED338C5681A1E55131788E96E4D4029C1953049DC990B67443BF86EE3A6FE2BD227172F96E44CD0FB63F7D62F6E44B8C072;"
    };
    public static void main(String[] args) {
        listenForExitCommand();

        System.out.println("Waiting for registered channel");
        while (discordBot.registeredTextChannels.isEmpty()) {
            threadSleep(10);
        }
        System.out.println("Channel registered.");

        LimitedTXTUpdater.updateLimitedsTXT();
        LimitedPriceTracker.updatePrices();
    }
    public static int getDefaultRetryTime() {
        return 5000;
    }
    public static void threadSleep(int timeMillis) {
        try {Thread.sleep(timeMillis);} catch (InterruptedException ie) {ie.printStackTrace(); System.out.println("Who the fuck is interrupting my threads?!");}
    }

    public static void listenForExitCommand() {
        Scanner input = new Scanner(System.in);
        Thread inputThread = new Thread(() -> {
            while (input.hasNext()) {
                String command = input.nextLine();
                if (command.equals("exit")) {
                    System.out.println("Exiting!");

                    CatalogScanner.CatalogSummary.summarize();
                    System.exit(0);
                    break;
                }
            }
        });
        inputThread.start();
    }
}