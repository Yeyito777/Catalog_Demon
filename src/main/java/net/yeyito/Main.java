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
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class Main {
    public static DiscordBot discordBot = new DiscordBot("MTA3MTExNzYzMjcyNTU5NDE4Mg.GVbLmT.gZBzQ6dteuapWbNa39cCOCklIirsOho6KUkBFA", Activity.playing("no games"));
    public static String secCookie = ".ROBLOSECURITY=_|WARNING:-DO-NOT-SHARE-THIS.--Sharing-this-will-allow-someone-to-log-in-as-you-and-to-steal-your-ROBUX-and-items.|_2783C9F3891567014890B0F80ED5AA72E3A259AF2DE50D4DC8A1322DBEA96F550751D67482A7E846BA1995D933478E677C1F1F14BC46A099B7875F827F21BA91C21B5050AFC7B352A5B9769BF5E99633B27D147916F3F1F877E6DAA9AC645112199AAB76451A8AF74CC11265F6EA7A5D6F3ADCB1B9CBEF761ACE1687325926047CED0EDC52DC4726ECE2CE4C1D80F55284F7934D0A1260C3208CFB2DD9371A1D91EBB68E31D122D9F30315D6992A97C58A55FF9813FD2B88DA0AB5B58773C1D644FDF8178E3CBE0B1373BD9B9EF13B8D72CFF4887EE813210B306543A62D0E3D89C945E754917454947CC35422EBC672ADFC27EB1362242F13357523790CDE7DBD5900E70BBCC8058AA97715A68F02A5B7FA3A851F94DCF7CAD2CAA636C3A53A1E33031AA734F1E7810F8A63D807D80D5489A797665DC3926BA3FF52AC06DF7C3386AA4F81E4336E7CA1575B938AB6E10F3875C909EAFD55C88D7B3191F74CF59C328AA98BBFA86B0E6857CB1FEEAEE07EDE03D9;";

    public static void main(String[] args) {
        listenForExitCommand();

        System.out.println("Waiting for registered channel");
        while (discordBot.registeredTextChannels.isEmpty()) {
            threadSleep(10);
        }
        System.out.println("Channel registered.");

        while (true) {
            LimitedTXTUpdater.updateLimitedsTXT();
            LimitedPriceTracker.updatePrices();
        }
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