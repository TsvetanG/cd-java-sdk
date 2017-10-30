package com.example.client.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;

import com.example.client.Organization;

public class ChannelUtil {

  /**
   * Creates new Channel in fabric network for specific organization
   * 
   * @param channelName
   * @param client
   * @param org
   * @return
   */
  public Channel createChannel(String channelName, HFClient client, Organization org) {
    return null;
  }

  /**
   * Re cosntructs sdk channel object
   * 
   * @param channelName
   * @param client
   * @param org
   * @return
   * @throws TransactionException
   * @throws InvalidArgumentException
   */
  public Channel reconstructChannel(String channelName, HFClient client, List<Peer> peers, List<Orderer> orderers,
      List<EventHub> hubs) throws InvalidArgumentException, TransactionException {

    Channel channel = client.newChannel(channelName);

    for (Orderer orderer : orderers) { // add remaining orderers if any.
      channel.addOrderer(orderer);
    }

    for (EventHub hub : hubs) {
      channel.addEventHub(hub);
    } 
    channel.initialize();

    return channel;

  }

  public Channel reconstructChannel(String channelName, HFClient client)
      throws IOException, InvalidArgumentException, TransactionException {
    Properties props = new Properties();
    FileInputStream fis = new FileInputStream(new File("./store/channels/" + channelName + ".prop"));
    props.load(fis);
    fis.close();

    List<Peer> peers = new ArrayList<Peer>();
    List<Orderer> orderers = new ArrayList<Orderer>();
    List<EventHub> hubs = new ArrayList<EventHub>();
    String key;
    String value;

    Set<Entry<Object, Object>> set = props.entrySet();

    for (Entry<Object, Object> entry : set) {
      key = entry.getKey().toString();
      value = entry.getValue().toString();
      key = key.substring(0, key.indexOf("."));
      switch (key) {
      case "peer":
        peers.add(createPeer(client, value));
        break;
      case "orderer":
        orderers.add(createOrderer(client, value));
        break;
      case "hub":
        hubs.add(createHub(client, value));
        break;

      default:
        break;
      }
    }

    return reconstructChannel(channelName, client, peers, orderers, hubs);
  }

  protected EventHub createHub(HFClient client, String value) throws InvalidArgumentException {
    String[] split = split(value);
    return client.newEventHub(split[0], split[1]);
  }

  protected Peer createPeer(HFClient client, String value) throws InvalidArgumentException {
    String[] split = split(value);
    return client.newPeer(split[0], split[1]);
  }

  protected Orderer createOrderer(HFClient client, String value) throws InvalidArgumentException {
    String[] split = split(value);
    return client.newOrderer(split[0], split[1]);
  }

  protected String[] split(String str) {
    int index = str.indexOf(":");
    return new String[] { str.substring(0, index), str.substring(index + 1) };
  }

}
