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
    public static String secCookie = ".ROBLOSECURITY=_|WARNING:-DO-NOT-SHARE-THIS.--Sharing-this-will-allow-someone-to-log-in-as-you-and-to-steal-your-ROBUX-and-items.|_EADFFF8CF51E664BE018330E7E474944DA6F62B3B2D6DCFED481964BB6438B530A837E700CE2276DC935B0EF838A9A74D24230AF42338247226F42E196C275FC824CEECDB91C39076FD1DB719F627D2A1DF80276E2CCA647B57F8068327865E5ADAF804F3023F3A17D938023020BB5516DFE0E0B4C505040195E7D052C30565B3FE05CF7B0A26B5CD05317C0E0EF89AF9BA4D76B32FE304BE65B9603F3444FB64BC80D7D4D782DF0313A66B6BAE8D127AF0CC4C214E2BA926E9DA7B840F6A1A9BF58FA6447E31C6BD00DCC372DC90FC0E167E366172E336A9D89894F7CD405EF97B28AE7087A6BA84BCCDDEC3B3F265DB02A16917045E99BBBFBBE9B2FAE52FED7025B8C18B3E46CA3EF4A70ABC5C3A67BFF65F7D639510E88D8A8F88949F2C4C783918AF49FADC5224A7A68805936CC0BD5EC74F5045CF419D95A0E40B3A69B41B9C98150060BD52D42BA48C3CBEE7C4D0457D5E84DFDD38EDC6AC6BD6D84BCF8D318DE66C34ACD3D8D8F8370B43E04F08BE879;";

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