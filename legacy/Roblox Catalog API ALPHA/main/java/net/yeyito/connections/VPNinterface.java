package net.yeyito.connections;

import com.beust.jcommander.internal.Nullable;
import net.yeyito.Main;

import java.io.File;
import java.io.IOException;

public class VPNinterface {
    public static void connect(@Nullable String serverName, @Nullable String country) {
        try {
            if (serverName != null) {Main.runCommand(new File("C:/Program Files/NordVPN/"), new String[]{"\"C:/Program Files/NordVPN/nordvpn.exe\"", "-c","-n",serverName});}
            else if (country != null) {Main.runCommand(new File("C:/Program Files/NordVPN/"), new String[]{"\"C:/Program Files/NordVPN/nordvpn.exe\"", "-c","-g",country});}
            else {Main.runCommand(new File("C:/Program Files/NordVPN/"), new String[]{"\"C:/Program Files/NordVPN/nordvpn.exe\"", "-c"});}
        } catch (IOException e) {
            System.out.println(e.getMessage());
            Main.discordBot.sendMessageOnRegisteredChannel("all-item-price-changes","Error whilst connecting to nordvpn: " + e.toString() + " retrying in " + Main.getDefaultRetryTime() + " millis!",0);
            Main.threadSleep(Main.getDefaultRetryTime());
            connect(serverName,country);
        }
    }
    public static void disconnect() {
        try {
            Main.runCommand(new File("C:/Program Files/NordVPN/"), new String[]{"\"C:/Program Files/NordVPN/nordvpn.exe\"", "-d"});
        } catch (IOException e) {
            System.out.println(e.getMessage());
            Main.discordBot.sendMessageOnRegisteredChannel("all-item-price-changes","Error whilst disconnecting from nordvpn: " + e.toString() + " retrying in " + Main.getDefaultRetryTime() + " millis!",0);
            Main.threadSleep(Main.getDefaultRetryTime());
            disconnect();
        }
    }
}
