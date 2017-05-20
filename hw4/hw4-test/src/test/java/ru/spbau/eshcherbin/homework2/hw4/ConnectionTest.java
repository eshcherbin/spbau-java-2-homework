package ru.spbau.eshcherbin.homework2.hw4;

import org.junit.Test;
import ru.spbau.eshcherbin.hw4.client.Client;
import ru.spbau.eshcherbin.hw4.client.FtpClient;
import ru.spbau.eshcherbin.hw4.server.FtpServer;
import ru.spbau.eshcherbin.hw4.server.Server;

import java.net.InetSocketAddress;

public class ConnectionTest {
    protected static final int PORT = 1117;

    @Test
    public void startServerTest() throws Exception {
        Server server = new FtpServer(new InetSocketAddress(PORT));
        server.start();
        Thread.sleep(100);
        server.stop();
    }

    @Test
    public void clientConnectTest() throws Exception {
        Server server = new FtpServer(new InetSocketAddress(PORT + 1));
        server.start();
        Thread.sleep(100);
        Client client = new FtpClient();
        client.connect(new InetSocketAddress("127.0.0.1", PORT + 1));
        Thread.sleep(300);
        client.disconnect();
        server.stop();
    }

    @Test
    public void severalClientsConnectTest() throws Exception {
        Server server = new FtpServer(new InetSocketAddress(PORT + 2));
        server.start();
        Thread.sleep(100);
        Client client1 = new FtpClient();
        client1.connect(new InetSocketAddress("127.0.0.1", PORT + 2));
        Client client2 = new FtpClient();
        client2.connect(new InetSocketAddress("127.0.0.1", PORT + 2));
        Thread.sleep(300);
        client2.disconnect();
        client1.disconnect();
        server.stop();
    }

    @Test
    public void clientConnectSeveralTimesTest() throws Exception {
        Server server = new FtpServer(new InetSocketAddress(PORT + 3));
        server.start();
        Thread.sleep(100);
        Client client = new FtpClient();
        client.connect(new InetSocketAddress("127.0.0.1", PORT + 3));
        client.disconnect();
        client.connect(new InetSocketAddress("127.0.0.1", PORT + 3));
        client.disconnect();
        server.stop();
    }
}
