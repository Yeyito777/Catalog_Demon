package net.yeyito.connections;

import net.yeyito.Main;
import net.yeyito.VirtualBrowser;
import net.yeyito.util.Connection;
import net.yeyito.util.StringFilter;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class TORinterface {
    //* Love the Onion *//
    public static Process TOR;
    public static int MAIN_PORT;
    public static int CONTROL_PORT;
    public static final String TOR_PASSWORD = "yeyito";
    public static void openTOR() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("C:/Users/aline/OneDrive/Desktop/Tor Browser/Browser/TorBrowser/Tor/tor.exe");
            Process torProcess = processBuilder.start();
            TOR = torProcess;
            InputStream inputStream = torProcess.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                if (line.contains("100%")) {
                    break;
                } else if (line.contains("Opening Socks listener")) {
                    MAIN_PORT = Integer.parseInt(StringFilter.parseStringUsingRegex(line,"[^:]*$",0));
                } else if (line.contains("Opening Control listener")) {
                    CONTROL_PORT = Integer.parseInt(StringFilter.parseStringUsingRegex(line,"[^:]*$",0));
                }
            } System.out.println("TOR INFO: \n" + "MAIN PORT: " + MAIN_PORT + "\n" + "CONTROL PORT: " + CONTROL_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            Main.threadSleep(Main.getDefaultRetryTime());
            openTOR();
        }
    }

    public static void changeCircuit() throws IOException {
        // open a connection to the Tor control port
        Socket socket = new Socket("127.0.0.1", 9151);
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

    public static void exitTOR() {
        TOR.destroy(); // Exit TOR
    }
}