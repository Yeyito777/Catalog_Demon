package net.yeyito;

import io.netty.resolver.InetSocketAddressResolver;
import net.dv8tion.jda.api.entities.Activity;
import net.yeyito.connections.DiscordBot;
import net.yeyito.roblox.LimitedPriceTracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Scanner;

public class Main {
    public static String key_of_the_day = "_|WARNING:-DO-NOT-SHARE-THIS.--Sharing-this-will-allow-someone-to-log-in-as-you-and-to-steal-your-ROBUX-and-items.|_15E90750A60924BDE3E74B4C51E41FA583E7D89BA0CA47267E7D15D7C9D5DEB5B92975ED3044C2E50D647C8DBFF6CD897B310422FCE065C7270E42C3014C755AD2218FB83C79AFADA91E4AC02892A79ABA4AD824E888540640068F8F3871C2199794CDA4369B3961B54CDDC5AA7B3F82207CD93227F4AAFF5208B15D749C2429442461139978FBE63FA78DCBA86FBF2309DBAFDD9CC41678703B515F47D4950097F11F48994688491C95E0BBDD1E03F41CFE865927410C2EFB47E42B7910DCB273708D9D40F9E181B5AA58D5F30C922B98A187950870787DFFA21829E4B4529A8E228599F56DC35D13A78C93DC4CDC2A629659A244A9876BD360E2DD77602BEE0146468FDC2E05215B7487B62D717D116CBC54CEACB695356453F113AFC735D3E8D0D6EDEB27F90218CA109ED4D2E9E154B585CAD65D57883643851797BD404BFB4820551F32C8E40D57F42B25D9EC0A90DD4D37FFF90D20985909B13990B0DEBDE0057DA75C09A818ED991EFA95A34874FAF4E426A1432901F39E18A310001081A266D2AC0CDF14418F877810EBEEA71CAAF189";
    public static DiscordBot discordBot = new DiscordBot("MTA3MTExNzYzMjcyNTU5NDE4Mg.Gq6H6P.xQrsfOvWAPf0HxizJSed-MPRco_9yjAsqckpv8", Activity.playing("no games"));

    public static void main(String[] args) throws IOException {
        listenForExitCommand();
        while (true) {
            LimitedPriceTracker.updatePrices();
            threadSleep(60000); // Sleep for a minute in order to not get 404'd
        }
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
    }

    public static void listenForExitCommand() throws IOException {
        Scanner input = new Scanner(System.in);
        Thread inputThread = new Thread(() -> {
            while (true) {
                String command = input.nextLine();
                if (command.equals("exit")) {
                    System.out.println("Exiting!");

                    String[] CMD = {"route","DELETE",WebsiteScraper.getHostIPfromURL("catalog.roblox.com")};
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

