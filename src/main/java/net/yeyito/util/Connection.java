package net.yeyito.util;

import net.yeyito.VirtualBrowser;
import net.yeyito.connections.RouteManager;
import net.yeyito.connections.TOR;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Connection {
    public final List<Connection> connections = new ArrayList<>();
    private final TYPE connectionType;
    private final String host;
    private final String gateway;
    private final int netInterface;

    private final AtomicBoolean active = new AtomicBoolean(false);

    private VirtualBrowser assignedBrowser;
    public TOR torInstance;

    public enum TYPE {
        NONE,
        DIRECT,
        PROXY
    }

    public Connection(TYPE type, String[] data) {
        this.connectionType = type;
        if (type == TYPE.DIRECT) {
            this.host = data[0];
            this.gateway = data[1];
            this.netInterface = Integer.parseInt(data[2]);
        } else {
            this.host = null;
            this.gateway = null;
            this.netInterface = -1;
        }
        connections.add(this);
    }

    public void connect() {
        if (!active.compareAndSet(false, true)) {
            return;
        }

        if (connectionType == TYPE.DIRECT) {
            RouteManager.addRoute(host, gateway, netInterface);
        } else if (connectionType == TYPE.PROXY) {
            throw new RuntimeException("Only handling TOR rn");
        }
    }

    public void connect(VirtualBrowser browser, TOR tor) {
        if (!active.compareAndSet(false, true)) {
            return;
        }

        if (connectionType == TYPE.DIRECT) {
            RouteManager.addRoute(host, gateway, netInterface);
        } else if (connectionType == TYPE.PROXY) {
            assignedBrowser = browser;
            this.torInstance = tor;
            browser.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", tor.MAIN_PORT)));
        }
    }

    public void disconnect() {
        if (!active.compareAndSet(true, false)) {
            return;
        }

        if (connectionType == TYPE.DIRECT) {
            RouteManager.deleteRoute(host);
        } else if (connectionType == TYPE.PROXY) {
            assignedBrowser.setProxy(null);
            try {
                torInstance.changeCircuit();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public TYPE getConnectionType() {
        return connectionType;
    }

    @Override
    public String toString() {
        return super.toString() + "{Type: " + connectionType + ", Host: " + host + ", Gateway: " + gateway + ", Interface: " + netInterface + "}";
    }
}
