package net.yeyito.connections.tor;

import net.yeyito.VirtualBrowser;
import net.yeyito.util.TextFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TorManager {
    public static List<TorInstance> TORinstances = new ArrayList<>();
    public static final String torPassword = "yeyito";

    public static void generateInstances(int instances, int baseSocksPort, int baseControlPort) {
        // Create TOR instances
        for (int i = 0; i < instances; i++) {
            TORinstances.add(new TorInstance(baseSocksPort + (i * 2), baseControlPort + (i * 2)));
        }
    }

    public static void generateDataDirectory(TorInstance tor) {
        String dataDirectory = "C:/Users/aline/OneDrive/Desktop/Tor Browser/Directories/Data" + tor.controlPort;
        Path sourceDir = Paths.get("C:/Users/aline/OneDrive/Desktop/Tor Browser/Browser/TorBrowser/Data/");

        // Check if the new directory exists; if not, create it by copying the source directory
        if (!Files.exists(Path.of(dataDirectory))) {
            try {
                Files.walk(sourceDir).forEach(source -> {
                    Path destination = Path.of(dataDirectory).resolve(sourceDir.relativize(source));
                    try {
                        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void exitInstances() {
        for (TorInstance tor: TORinstances) {
            tor.exitTOR();
        }
    }

    public static void log(TorInstance tor, String data) {
        TextFile logFile = new TextFile("src/main/resources/TORlog/TOR" + tor.controlPort + ".txt");
        logFile.writeString(data);
    }

    public static void delLog(TorInstance tor) {
        TextFile logFile = new TextFile("src/main/resources/TORlog/TOR" + tor.controlPort + ".txt");
        logFile.deleteAllText();
    }

    public static TorInstance getAttachedTorFromBrowser(VirtualBrowser virtualBrowser) {
        for (TorInstance tor: TORinstances) {
            if (tor.socksPort == ((InetSocketAddress) virtualBrowser.proxy.address()).getPort()) {
                return tor;
            }
        }
        return null;
    }
}
