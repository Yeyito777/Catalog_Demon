package net.yeyito.connections;

import net.yeyito.Main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Deprecated public class RouteManager {
    public static void addRoute(String host, String gateway, Integer netInterface) {
        try {
            Main.runCommand(new String[]{"route", "ADD", getHostIPfromURL(host), "MASK", "255.255.255.255", gateway, "METRIC", "1", "IF", netInterface.toString()}); // Breaks if Interface Index changes, or if gateway changes!
        } catch (IOException eCMD) {
            Main.discordBot.sendMessageOnRegisteredChannels(eCMD.toString() + " retrying in " + Main.getDefaultRetryTime() + " millis!",0);
            Main.threadSleep(Main.getDefaultRetryTime());
            addRoute(host,gateway,netInterface);
        }
    }

    public static void deleteRoute(String host) {
        try {
            Main.runCommand(new String[]{"route","DELETE",getHostIPfromURL(host)});
        } catch (IOException e) {
            Main.discordBot.sendMessageOnRegisteredChannels(e.toString() + " command to delete LIB route couldn't be processed, retrying in " + Main.getDefaultRetryTime() + " millis!",0);
            Main.threadSleep(Main.getDefaultRetryTime());
            deleteRoute(host);
        }
    }

    public static String getHostIPfromURL(String URL) {
        try {
            InetAddress inetAddress = InetAddress.getByName(URL);
            return inetAddress.getHostAddress();

        } catch (UnknownHostException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }
}