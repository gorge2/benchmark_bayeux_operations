package com.sfdc.perfeng;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.ClientTransport;
import org.cometd.client.transport.LongPollingTransport;
import org.eclipse.jetty.client.HttpClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author psrinivasan
 *         Date: 9/21/12
 *         Time: 8:18 PM
 */
public class BayeuxClientTest extends junit.framework.TestCase {
    HttpClient httpClient;
    Properties properties;
    String url;
    String channel;

    public void setUp() throws Exception {
        properties = loadConfigProperties("src/main/resources/config.properties");
        url = (String) properties.get("url");
        channel = (String) properties.get("channel");
         httpClient = new HttpClient();
        try {
            httpClient.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void tearDown() throws Exception {

    }

    public void testHandshake() throws Exception {
        Map<String, Object> options = new HashMap<String, Object>();
        ClientTransport transport = LongPollingTransport.create(options, httpClient);
        BayeuxClient client = new BayeuxClient(url, transport);
        client.handshake();
        client.waitFor(2000, BayeuxClient.State.CONNECTED);
        assertEquals(true, client.isHandshook());
    }

    public void testSubscribe() throws Exception {
        Map<String, Object> options = new HashMap<String, Object>();
        ClientTransport transport = LongPollingTransport.create(options, httpClient);
        BayeuxClient client = new BayeuxClient(url, transport);
        client.handshake();
        client.waitFor(2000, BayeuxClient.State.CONNECTED);
        assertEquals(true, client.isHandshook());
        ClientSessionChannel channel = client.getChannel("/chat/demo");
        channel.subscribe(new ClientSessionChannel.MessageListener()
        {
            public void onMessage(ClientSessionChannel channel, Message message)
            {
                System.out.println("Message : " + message.toString());
            }
        });
    }

    public void testPublish() {
        Map<String, Object> options = new HashMap<String, Object>();
        ClientTransport transport = LongPollingTransport.create(options, httpClient);
        BayeuxClient client = new BayeuxClient(url, transport);
        client.handshake();
        client.waitFor(2000, BayeuxClient.State.CONNECTED);
        assertEquals(true, client.isHandshook());
        ClientSessionChannel channel = client.getChannel("/chat/demo");
        final String[] recd_message = new String[1];
        channel.subscribe(new ClientSessionChannel.MessageListener()
        {
            public void onMessage(ClientSessionChannel channel, Message message)
            {
                System.out.println("Message : " + message.toString());
                recd_message[0] =  message.toString();
            }
        });
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("foo", "bar");
        channel.publish(data);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String [] tokens = recd_message[0].split(",");
        assertEquals(" data={foo=bar}", tokens[1]);
        assertEquals(" channel=/chat/demo}", tokens[2]);
    }

    public Properties loadConfigProperties(String fileName) throws IOException {
        Properties p = new Properties();
        p.load(new FileInputStream(fileName));
        return p;
    }
}
