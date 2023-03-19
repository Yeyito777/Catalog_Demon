package net.yeyito.connections;

import com.beust.jcommander.internal.Nullable;
import net.yeyito.Main;
import net.yeyito.util.StringFilter;
import net.yeyito.util.TextFile;

import java.io.*;
import java.net.*;
import java.util.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class TOR {
    //* Love the Onion *//
    public static List<TOR> TORinstances = new ArrayList<>();
    public static final String TOR_PASSWORD = "yeyito";
    public static HashMap<String,Long> exitNodeToLastUsedNanoTime = new HashMap<>();
    String currentIP = "";
    public Process TORprocess;
    public int MAIN_PORT;
    public int CONTROL_PORT;
    private final String path;
    public String exitNodes = null;
    public static void generateInstances(int instances) {
        int baseSocksPort = 9050;
        int baseControlPort = 9051;

        // Create TOR instances with different exit country codes
        for (int i = 0; i < instances; i++) {
            new TOR(baseSocksPort + (i * 2), baseControlPort + (i * 2));
        }
    }

    public TOR(int socksPort, int controlPort) {
        this.path = "C:/Users/aline/OneDrive/Desktop/Tor Browser/Browser/TorBrowser/Tor/tor.exe";
        TORinstances.add(this);
        this.CONTROL_PORT = controlPort;
        this.MAIN_PORT = socksPort;
        openTOR(socksPort, controlPort);
    }

    public void openTOR(int socksPort, int controlPort) {
        String dataDirectory = "C:/Users/aline/OneDrive/Desktop/Tor Browser/Directories/Data" + CONTROL_PORT;

        // Define the source directory and the new directory paths
        Path sourceDir = Paths.get("C:/Users/aline/OneDrive/Desktop/Tor Browser/Browser/TorBrowser/Data/");
        Path newDir = Paths.get(dataDirectory);

        // Check if the new directory exists; if not, create it by copying the source directory
        if (!Files.exists(newDir)) {
            try {
                Files.walk(sourceDir).forEach(source -> {
                    Path destination = newDir.resolve(sourceDir.relativize(source));
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

        try {
            ProcessBuilder processBuilder;
            String geoipPath = "C:/Users/aline/OneDrive/Desktop/Tor Browser/Directories/Data" + this.CONTROL_PORT + "/Tor/geoip";
            String geoip6Path = "C:/Users/aline/OneDrive/Desktop/Tor Browser/Directories/Data" + this.CONTROL_PORT + "/Tor/geoip6";
            String clientOnionAuthPath = "C:/Users/aline/OneDrive/Desktop/Tor Browser/Directories/Data" + this.CONTROL_PORT + "/Tor/onion-auth";
            if (this.exitNodes != null) {processBuilder = new ProcessBuilder(this.path, "--ControlPort", String.valueOf(controlPort), "--SocksPort", String.valueOf(socksPort), "--DataDirectory", dataDirectory, "--ExitNodes", exitNodes, "--GeoIPFile", geoipPath, "--GeoIPv6File", geoip6Path, "--ClientOnionAuthDir", clientOnionAuthPath);}
            else {processBuilder = new ProcessBuilder(this.path, "--ControlPort", String.valueOf(controlPort), "--SocksPort", String.valueOf(socksPort), "--DataDirectory", dataDirectory, "--ClientOnionAuthDir", clientOnionAuthPath);}

            Process torProcess = processBuilder.start();
            TORprocess = torProcess;
            InputStream inputStream = torProcess.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                this.log("\n"+line);
                if (line.contains("100%")) {
                    break;
                } else if (line.contains("Opening Socks listener")) {
                    MAIN_PORT = Integer.parseInt(StringFilter.parseStringUsingRegex(line, "[^:]*$", 0));
                } else if (line.contains("Opening Control listener")) {
                    CONTROL_PORT = Integer.parseInt(StringFilter.parseStringUsingRegex(line, "[^:]*$", 0));
                }
            }
            this.log("TOR INFO: \n" + "MAIN PORT: " + MAIN_PORT + "\n" + "CONTROL PORT: " + CONTROL_PORT + "\n");
        } catch (IOException e) {
            e.printStackTrace();
            Main.threadSleep(Main.getDefaultRetryTime());
            openTOR(controlPort, socksPort);
        }
        this.currentIP = this.getPublicIP();
        this.log("Using ip: " + this.currentIP + "\n");
    }

    public void changeCircuit() throws IOException {
        // Exclude previously used IP
        exitNodeToLastUsedNanoTime.put(this.currentIP,System.nanoTime());
        for (String exitNode: exitNodeToLastUsedNanoTime.keySet()) {
            double secondsAgo = (System.nanoTime() - exitNodeToLastUsedNanoTime.get(exitNode))/1e+9;
            if (secondsAgo > 60) {
                this.log("\nremoved " + exitNode + " as it was last used " + secondsAgo);
                exitNodeToLastUsedNanoTime.remove(exitNode);
            }
        }
        // open a connection to the Tor control port
        Socket socket = new Socket("127.0.0.1", this.CONTROL_PORT);
        socket.setSoTimeout(1000); // Set a 1-second timeout
        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();

        // authenticate with the Tor control port
        outputStream.write(("AUTHENTICATE \"" + TOR_PASSWORD + "\"\r\n").getBytes());
        byte[] response = new byte[1024];
        int bytesRead = inputStream.read(response);
        String responseString = new String(response, 0, bytesRead);
        if (!responseString.trim().endsWith("250 OK")) {
            throw new IOException("Authentication failed: " + responseString);
        }

        // send the "SIGNAL NEWNYM" command to the Tor control port
        try {
            outputStream.write("SIGNAL NEWNYM\r\n".getBytes());
            bytesRead = inputStream.read(response);
            responseString = new String(response, 0, bytesRead);
            if (!responseString.trim().endsWith("250 OK")) {
                throw new IOException("Failed to signal new circuit: " + responseString);
            }
        } catch (SocketTimeoutException ignored) {
            System.out.println("WTFUESFHUOIESHRFIUESHUOFEHJSOUFHEU*S*(&*(&*(&(*&(*&");
            this.exitTOR();
            openTOR(this.MAIN_PORT, this.CONTROL_PORT);
            this.changeCircuit();
        }

        // close the connection to the Tor control port
        socket.close();

        // Logging
        this.currentIP = this.getPublicIP();
        this.log("\nUsing ip: " + this.currentIP + "\n");

        if (exitNodeToLastUsedNanoTime.containsKey(this.currentIP)) {
            this.log("\nNevermind! Ip was used before in the past 60 seconds, changing it\n");
            changeCircuit();
        }
    }

    public String getPublicIP() {
        String publicIP = "";

        try {
            URL checkIP = new URL("http://checkip.amazonaws.com/");
            HttpURLConnection connection = (HttpURLConnection) checkIP.openConnection(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", MAIN_PORT)));

            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            publicIP = reader.readLine();
            reader.close();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return publicIP;
    }

    boolean initLog = false;
    TextFile logFile = null;
    public void log(String data) {
        if (!initLog) {this.logFile = new TextFile("src/main/resources/TORlog/TOR" + this.CONTROL_PORT + ".txt");this.logFile.deleteAllText(); initLog = true;}
        this.logFile.writeString(data);
    }

    public void exitTOR() {
        TORprocess.destroy(); // Exit TOR
    }
}