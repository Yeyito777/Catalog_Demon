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
    public static DiscordBot discordBot = new DiscordBot("MTA3MTExNzYzMjcyNTU5NDE4Mg.Gq6H6P.xQrsfOvWAPf0HxizJSed-MPRco_9yjAsqckpv8", Activity.playing("no games"));
    public static boolean PRINT_CMD_ERRORS = false;
    public static ChromeDriver driver;
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

    public static void runCommand(String[] command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.contains("OK!")) {System.out.println(line);}
        }
        if (process.getErrorStream() != null && process.getErrorStream().available() > 0 && PRINT_CMD_ERRORS) {
            new TextFile("src/main/resources/StackTrace.txt").writeString("\nError with terminal: "+ process.getErrorStream().toString() + "\n");
        }
    }

    public static void runCommand(File directory,String[] command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(directory);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.contains("OK!")) {System.out.println(line);}
        }
        if (process.getErrorStream() != null && process.getErrorStream().available() > 0 && PRINT_CMD_ERRORS) {
            new TextFile("src/main/resources/StackTrace.txt").writeString("\nError with terminal: "+ process.getErrorStream().toString() + "\n");
        }
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