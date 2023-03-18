package net.yeyito.connections;

import net.yeyito.Main;
import net.yeyito.util.StringFilter;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class TOR {
    //* Love the Onion *//
    public static List<TOR> TORinstances = new ArrayList<>();
    public static final String TOR_PASSWORD = "yeyito";
    public Process TORprocess;
    public int MAIN_PORT;
    public int CONTROL_PORT;
    private final String path;

    public boolean available = true;
    public static void waitForAvailableInstance(int sampleMillis) { // Halts the code until an instance is available
        if (getAvailableInstance() == null) {
            Main.threadSleep(sampleMillis);
            waitForAvailableInstance(sampleMillis);
        }
    }
    public static TOR getAvailableInstance() { //TOR.waitForAvailableInstance(10);
        for (TOR T: TORinstances) {
            if (T.available) {
                return T;
            }
        }
        return null;
    }
    public static TOR getAvailableInstance(int sampleMillis) {
        waitForAvailableInstance(sampleMillis);
        for (TOR T: TORinstances) {
            if (T.available) {
                return T;
            }
        }
        return null;
    }
    public void markUnavailable() {
        available = false;
    }

    public void markAvailable() {
        available = true;
    }
    public TOR(String torPath, int socksPort, int controlPort) {
        this.path = torPath;
        TORinstances.add(this);
        this.CONTROL_PORT = controlPort;
        this.MAIN_PORT = socksPort;
        openTOR(socksPort,controlPort);
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
            ProcessBuilder processBuilder = new ProcessBuilder(this.path, "--ControlPort", String.valueOf(controlPort), "--SocksPort", String.valueOf(socksPort), "--DataDirectory", dataDirectory);
            Process torProcess = processBuilder.start();
            TORprocess = torProcess;
            InputStream inputStream = torProcess.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                if (line.contains("100%")) {
                    break;
                } else if (line.contains("Opening Socks listener")) {
                    MAIN_PORT = Integer.parseInt(StringFilter.parseStringUsingRegex(line, "[^:]*$", 0));
                } else if (line.contains("Opening Control listener")) {
                    CONTROL_PORT = Integer.parseInt(StringFilter.parseStringUsingRegex(line, "[^:]*$", 0));
                }
            }
            System.out.println("TOR INFO: \n" + "MAIN PORT: " + MAIN_PORT + "\n" + "CONTROL PORT: " + CONTROL_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            Main.threadSleep(Main.getDefaultRetryTime());
            openTOR(controlPort, socksPort);
        }
    }

    public void changeCircuit() throws IOException {
        // open a connection to the Tor control port
        Socket socket = new Socket("127.0.0.1", this.CONTROL_PORT);
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
        outputStream.write("SIGNAL NEWNYM\r\n".getBytes());
        bytesRead = inputStream.read(response);
        responseString = new String(response, 0, bytesRead);
        if (!responseString.trim().endsWith("250 OK")) {
            throw new IOException("Failed to signal new circuit: " + responseString);
        }

        // close the connection to the Tor control port
        socket.close();
    }

    public void exitTOR() {
        TORprocess.destroy(); // Exit TOR
    }
}