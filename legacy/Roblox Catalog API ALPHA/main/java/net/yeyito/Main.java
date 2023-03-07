package net.yeyito;

import net.dv8tion.jda.api.entities.Activity;
import net.yeyito.connections.DiscordBot;
import net.yeyito.connections.LimitedTXTUpdater;
import net.yeyito.connections.RouteManager;
import net.yeyito.connections.VPNinterface;
import net.yeyito.roblox.LimitedPriceTracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Main {
    @Deprecated public static String key_of_the_day = "_|WARNING:-DO-NOT-SHARE-THIS.--Sharing-this-will-allow-someone-to-log-in-as-you-and-to-steal-your-ROBUX-and-items.|_73ADBA4F2B96C51F11DA5552CBBDCFB1633965181AA7EF5146EBB4465361005C3CD2BE42B5AD1DACAAB3C23F57C8865EF0CF0A497410D04C2D5D64A012ECEDD3FF417E2A671FEE9BDB2DA513EF2B250799B9A981D1EFD227FD16B09FFBC57F4814D615311BE9BDAED3FDDFA30DDA50437CE57001337023C4618840B9E4CDB51849BDC0328AE51E7918DAED83C9619972CEEBD55B2C54601C7B6E23B6784EC6D3BE14B731CB3DFF9EC990327869F77C8D93AB0D249F600CED6E3CC7B172D89449C9F6936B24D388A0037381419CC7CDBAB81B1F4FED212F26347DA1AFC9862825B65B5006F0792C343C0E15D638118E9CB2448A72EA513D31FC7D507C85053D9E486BAD31C2A36D37EA3DBC746B8EBF1B9370993520900139138536121D5FF3DB522D1089BAF39B7477569587BAA5BCCAB9223BB81A77D14DAA85CC0BDE5DDC59F4FD88821D8412751278D5FE9BEFAC00F9E7168D4FD2951204A9AD202D32BAC3C1C4B36E40624C34A65D02DDF75DE464B68F45D3";
    public static DiscordBot discordBot = new DiscordBot("MTA3MTExNzYzMjcyNTU5NDE4Mg.Gq6H6P.xQrsfOvWAPf0HxizJSed-MPRco_9yjAsqckpv8", Activity.playing("no games"));
    public static boolean PRINT_CMD_ERRORS = false;

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
            threadSleep(50000); // Sleep in order to not get too many requests!
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
            System.out.println(process.getErrorStream().toString());
            Main.discordBot.sendMessageOnRegisteredChannel("all-item-price-changes","Error with terminal: " + process.getErrorStream().toString(),0);
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
            System.out.println(process.getErrorStream().toString());
            Main.discordBot.sendMessageOnRegisteredChannel("all-item-price-changes","Error with terminal: " + process.getErrorStream().toString(),0);
        }
    }

    public static void listenForExitCommand() {
        Scanner input = new Scanner(System.in);
        Thread inputThread = new Thread(() -> {
            while (input.hasNext()) {
                String command = input.nextLine();
                if (command.equals("exit")) {
                    System.out.println("Exiting!");

                    String[] CMD = {"route","DELETE", RouteManager.getHostIPfromURL("catalog.roblox.com")};
                    try {
                        Runtime.getRuntime().exec(CMD);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    System.exit(0);
                    break;
                }
            }
        });
        inputThread.start();
    }
}

