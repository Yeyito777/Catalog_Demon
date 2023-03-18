package net.yeyito.util;


import net.yeyito.VirtualBrowser;
import net.yeyito.connections.RouteManager;
import net.yeyito.connections.TOR;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

public class Connection {
    public static List<Connection> connections = new ArrayList<>();
    public static final Connection[] CONNECTION_PATH = {
            new Connection(TYPE.NONE,null),
            new Connection(TYPE.DIRECT,new String[]{"catalog.roblox.com","192.168.0.1","8"}),
            new Connection(TYPE.PROXY,null),
            new Connection(TYPE.PROXY,null)
    };

    static int conIndex = 0;
    public static Connection getNextConnection() {
        Connection returnable = CONNECTION_PATH[conIndex];
        conIndex++;
        if (conIndex >= CONNECTION_PATH.length) {
            conIndex = 0;
        }
        return returnable;
    }
    TYPE connectionType;
    String host;
    String gateway;
    int netInterface;
    boolean destroyOnDisconnect = false;
    public boolean active = false;

    public enum TYPE {
        NONE,
        DIRECT,
        PROXY
    }

    private VirtualBrowser assignedBrowser = null;
    public TOR torInstance = null;
    public Connection(Connection.TYPE type, String[] data) {
        this.connectionType = type;
        connections.add(this);
        if (type == TYPE.DIRECT) {
            this.host = data[0];
            this.gateway = data[1];
            this.netInterface = Integer.parseInt(data[2]);
        }
        if (type == TYPE.PROXY) {
            if (data != null && data[0].equals("true")) {
                this.destroyOnDisconnect = true;
            }
        }
    }
    public void connect() {
        // Disconnect from all connections
        for (Connection connection1 : connections) {if (connection1.active) {connection1.disconnect(); connection1.active = false;}}
        // Connect
        if (this.connectionType == TYPE.DIRECT) {RouteManager.addRoute(this.host,this.gateway,this.netInterface);}
        this.active = true;
    }
    public void connect(VirtualBrowser browser) {
        // Disconnect from all connections
        for (Connection connection1 : connections) {if (connection1.active) {connection1.disconnect(); connection1.active = false;}}
        // Connect
        if (this.connectionType == TYPE.DIRECT) {RouteManager.addRoute(this.host,this.gateway,this.netInterface);}
        else if (this.connectionType == TYPE.PROXY) {
            this.assignedBrowser = browser;
            this.torInstance = TOR.getAvailableInstance(10);
            assert this.torInstance != null; // Can assert since previous line waits for an available instance because of method.
            this.torInstance.markUnavailable();
            browser.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", torInstance.MAIN_PORT)));
        }
        this.active = true;
    }

    public void disconnect() {
        if (this.connectionType == TYPE.DIRECT) {
            RouteManager.deleteRoute(this.host);
        }
        else if (this.connectionType == TYPE.PROXY) {
            this.assignedBrowser.setProxy(null);
            try {
                this.torInstance.changeCircuit(); this.torInstance.markAvailable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (this.destroyOnDisconnect) {
            connections.remove(this);
        }
    }

    public TYPE getConnectionTYPE() {
        return this.connectionType;
    }

    @Override
    public String toString() {
        return super.toString() + "{Type: " + this.connectionType + ", Host: " + this.host + ", Gateway: " + this.gateway + ", Interface: " + this.netInterface + "}";
    }
}