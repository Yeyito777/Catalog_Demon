package net.yeyito.connections.tor;

import net.yeyito.roblox.RequestManager;

import java.io.*;
import java.net.*;

import java.io.IOException;

public class TorInstance {
    //* Love the Onion *//
    public Process TOR;
    public int socksPort;
    public int controlPort;
    public String currentIP;

    public TorInstance(int socksPort, int controlPort) {
        openTor(socksPort, controlPort);
    }

    public void openTor(int socksPort, int controlPort) {
        this.socksPort = socksPort;
        this.controlPort = controlPort;

        TorManager.generateDataDirectory(this);
        try {
            String torExe = "C:/Users/aline/OneDrive/Desktop/Tor Browser/Browser/TorBrowser/Tor/tor.exe";
            String clientOnionAuthPath = "C:/Users/aline/OneDrive/Desktop/Tor Browser/Directories/Data" + this.controlPort + "/Tor/onion-auth";
            String dataDirectory = "C:/Users/aline/OneDrive/Desktop/Tor Browser/Directories/Data" + this.controlPort;
            ProcessBuilder processBuilder = new ProcessBuilder(torExe, "--ControlPort", String.valueOf(this.controlPort), "--SocksPort", String.valueOf(this.socksPort), "--DataDirectory", dataDirectory + this.controlPort, "--ClientOnionAuthDir", clientOnionAuthPath);
            Process torProcess = processBuilder.start();
            this.TOR = torProcess;

            InputStream inputStream = torProcess.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                TorManager.log(this,line + "\n");
                if (line.contains("100%")) {break;}
            }
            this.currentIP = TorNetworkManager.getPublicIP(this);
            TorManager.delLog(this);
            TorManager.log(this,"Using IP: " + this.currentIP + "\n");
        } catch (IOException e) {
            throw new RuntimeException("Failure to open TOR");
        }
    }

    public void changeCircuit() {
        try {
            TorNetworkManager.finishedUsingIP(this.currentIP);
            TorNetworkManager.updateIPs();

            // open a connection to the Tor control port
            Socket socket = new Socket("127.0.0.1", this.controlPort);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            socket.setSoTimeout(1000);

            // authenticate with the Tor control port
            outputStream.write(("AUTHENTICATE \"" + TorManager.torPassword + "\"\r\n").getBytes());
            byte[] response = new byte[1024];
            int bytesRead = 0;
            try {
                bytesRead = inputStream.read(response);
            } catch (SocketTimeoutException e) {
                this.reloadTOR();
            }
            String responseString = new String(response, 0, bytesRead);
            if (!responseString.trim().endsWith("250 OK")) {
                throw new IOException("Authentication failed: " + responseString);
            }

            // send the "SIGNAL NEWNYM" command to the Tor control port
            outputStream.write("SIGNAL NEWNYM\r\n".getBytes());
            try {
                bytesRead = inputStream.read(response);
            } catch (SocketTimeoutException e) {
                this.reloadTOR();
            }
            responseString = new String(response, 0, bytesRead);
            if (!responseString.trim().endsWith("250 OK")) {
                throw new IOException("Failed to signal new circuit: " + responseString);
            }

            // close the connection to the Tor control port
            socket.close();

            // Logging
            this.currentIP = TorNetworkManager.getPublicIP(this);
            TorManager.log(this, "\nUsing IP: " + this.currentIP);
            if (TorNetworkManager.isIPblocked(this.currentIP)) {
                TorManager.log(this, "\nExit node has been recently used, switching!");
                this.changeCircuit();
            }
            RequestManager.cantCancel.remove(this);
        } catch (IOException e) {
            throw new RuntimeException("Unresolved problem when changing circuit: " + e.getMessage());
        }
    }

    private void reloadTOR() {
        System.out.println("Reloading tor! (holy shit)");
        RequestManager.cantCancel.add(this);
        TorManager.log(this,"\nReloading Tor!");
        this.exitTOR();
        this.openTor(this.socksPort,this.controlPort);
        this.changeCircuit();
    }

    public void exitTOR() {
        TOR.destroy(); // Exit TOR
    }
}