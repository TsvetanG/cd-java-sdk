/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  DO NOT USE IN PROJECTS , NOT for use in production
 */

package com.example.client.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;

public class ChannelUtil {

 
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
  protected Channel reconstructChannel(String channelName, HFClient client, List<Peer> peers, List<Orderer> orderers,
      List<EventHub> hubs) throws InvalidArgumentException, TransactionException {
 
    Channel channel = client.newChannel(channelName);

    for (Orderer orderer : orderers) { // add remaining orderers if any.
      channel.addOrderer(orderer);
    }

    for (EventHub hub : hubs) {
      channel.addEventHub(hub);
    }
    for (Peer peer : peers) {
      channel.addPeer(peer);
    }
    channel.setTransactionWaitTime(50000);
    channel.initialize();

    return channel;

  }
  
  public Channel reconstructChannel(String org, String channelName, String peerName, HFClient client) throws IOException, InvalidArgumentException, TransactionException {
    Properties props = new Properties();
    FileInputStream fis = new FileInputStream( new File("./store/channels/" + channelName + "/" + channelName + ".prop"));

    props.load(fis);
    fis.close();
    
    List<Peer> peers = new ArrayList<Peer>();
    List<Orderer> orderers = new ArrayList<Orderer>();
    List<EventHub> hubs = new ArrayList<EventHub>();


    add(org, peerName, client, props, peers, orderers, hubs );

    return reconstructChannel(channelName, client, peers, orderers, hubs);
  }

  public Channel reconstructChannel(String org, String channelName, HFClient client)
      throws IOException, InvalidArgumentException, TransactionException {
    Properties props = new Properties();
    FileInputStream fis = new FileInputStream(
        new File("./store/channels/" + channelName + "/" + channelName + ".prop"));

    props.load(fis);
    fis.close();

    List<Peer> peers = new ArrayList<Peer>();
    List<Orderer> orderers = new ArrayList<Orderer>();
    List<EventHub> hubs = new ArrayList<EventHub>();

    add(org, null, client, props, peers, orderers, hubs);

    return reconstructChannel(channelName, client, peers, orderers, hubs);
  }

  protected void add(String org, String peerName, HFClient client, Properties props, List<Peer> peers, List<Orderer> orderers,
      List<EventHub> hubs) throws InvalidArgumentException {
    
    String value;
    String key;
    String[] keySplit;
    Set<Entry<Object, Object>> set = props.entrySet();

    for (Entry<Object, Object> entry : set) { 
      key = entry.getKey().toString();
      keySplit = key.split("\\.");
      if (!org.equals(keySplit[1])) {
        continue;
      }
      value = entry.getValue().toString();
      switch (keySplit[0]) {
      case "peer":
        if (peerName == null || peerName.equals(split(value)[0]) ) {
          peers.add(createPeer(client, value));
        }  
        break;
      case "orderer":
        orderers.add(createOrderer(client, value));
        break;
      case "hub":
        if (peerName == null || peerName.equals(split(value)[0]) ) {
          hubs.add(createHub(client, value));
        }
        break;

      default:
        break;
      }
    }
  }

  protected Properties getOrdererProps(String name) {
    return getProps("orderer", name);
  }

  protected Properties getPeerProps(String name) {
    return getProps("peer", name);
  }

  protected Properties getProps(String type, String name) {
    String orgName = getOrgName(name);
    File cert = null;
    if ("peer".equals(type)) {
      cert = new File("./store/crypto-config/peerOrganizations/" + orgName + "/peers/" + name + "/tls/server.crt");
    } else {
      cert = new File(
          "./store/crypto-config/ordererOrganizations/" + orgName + "/orderers/" + name + "/tls/server.crt");
    }

    if (!cert.exists()) {
      throw new RuntimeException("Missing certificate file ");
    }

    Properties props = new Properties();
    props.setProperty("pemFile", cert.getAbsolutePath());
    // ret.setProperty("trustServerCertificate", "true"); //testing environment only
    // NOT FOR PRODUCTION!
    props.setProperty("hostnameOverride", name);
    props.setProperty("sslProvider", "openSSL");
    props.setProperty("negotiationType", "TLS");
    if("orderer".equals(type)) {
      props.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[] {5L, TimeUnit.MINUTES});
      props.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[] {8L, TimeUnit.SECONDS});
      props.put("grpc.NettyChannelBuilderOption.keepAliveWithoutCalls", new Object[] {true});
    }

    return props;
  }

  private String getOrgName(String name) {
    int index = name.indexOf(".");
    return name.substring(index + 1);
  }

  public EventHub createHub(HFClient client, String value) throws InvalidArgumentException {
    String[] split = split(value);
    return client.newEventHub(split[0], split[1], getPeerProps(split[0]));
  }

  public Peer createPeer(HFClient client, String value) throws InvalidArgumentException {
    String[] split = split(value);
    return client.newPeer(split[0], split[1], getPeerProps(split[0]));
  }

  protected Orderer createOrderer(HFClient client, String value) throws InvalidArgumentException {
    String[] split = split(value);
    return client.newOrderer(split[0], split[1], getOrdererProps(split[0]));
  }

  protected String[] split(String str) {
    int index = str.indexOf(":");
    return new String[] { str.substring(0, index), str.substring(index + 1) };
  }

  public Channel createNewChannel(String ordererPath, String pathToConfigTX, String channelName, String org, HFClient client)
      throws IOException, InvalidArgumentException, TransactionException {

    
    Orderer orderer = createOrderer(client, ordererPath);

    if (orderer == null) {
      throw new RuntimeException("Orderer not found in channel create property file");
    }
    
    storeNewChannel(channelName, ordererPath , org);

    Channel channel = createNewChannel(pathToConfigTX, channelName, client, orderer, client.getUserContext());
    return channel;
  }

  protected void storeNewChannel(String channelName, String ordererPath, String org) throws IOException {
    File file = new File("./store/channels/" + channelName);
    if (!file.exists()) {
      file.mkdirs();
    }
    
    File propFile = new File(file, channelName + ".prop");
    Properties props = new Properties();
    if(propFile.exists()) {
      FileInputStream fis = new FileInputStream(propFile);
      props.load(fis);
      fis.close();
    }
    
    props.setProperty("orderer." + org + ".0", ordererPath);
    
    FileOutputStream out = new FileOutputStream(propFile);
    props.store(out, null);
    out.close();
    
  }

  protected Channel createNewChannel(String pathToConfigTX, String channelName, HFClient client, Orderer orderer,
      User user) throws TransactionException, InvalidArgumentException, IOException {

    ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(pathToConfigTX));
    Channel newChannel = client.newChannel(channelName, orderer, channelConfiguration,
        client.getChannelConfigurationSignature(channelConfiguration, user));
    return newChannel;
  }

  public void updateChannelProps(String channelName, String org, String peerPath, String eventHub) throws IOException {
    Properties props = new Properties();
    File propFile = new File("./store/channels/" + channelName + "/" + channelName +  ".prop");
    FileInputStream fis = new FileInputStream(propFile);

    props.load(fis);
    fis.close();

    int i = 0;
    String key = null;
    String value = null;
    while (true) {
      key = "peer." + org + "." + i;
      value = props.getProperty(key);
      if (value == null) {
        key = org + "." + i;
        break;
      }
      ++i;
    }

    props.setProperty("peer." + key, peerPath);
    props.setProperty("hub." + key, eventHub);
    FileOutputStream out = new FileOutputStream(propFile);
    props.store(out, null);
    out.close();

  }

}
