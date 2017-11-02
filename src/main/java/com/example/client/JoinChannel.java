package com.example.client;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.example.client.impl.ChannelUtil;
import com.example.client.impl.UserFileSystem;

public class JoinChannel {

  public static void main(String[] args) throws CryptoException, InvalidArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, TransactionException, IOException, ProposalException {
 
    String channelName = "transfer";
    String org = "druginc"; 
    String peer = "<peer name>:grpcs://<host>:<port>";
    String eventHub = "<peer name>:grpcs://<host>:<port>";
    JoinChannel join = new JoinChannel();
    User user = new UserFileSystem("Admin", "druginc.drug.com"); 
    join.join(channelName, peer, eventHub, org, user);

  }

  protected void join(String channelName, String peerPath ,String eventHub, String org, User user) throws CryptoException, InvalidArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, TransactionException, IOException, ProposalException {
    ChannelUtil util = new ChannelUtil();
    HFClient client = HFClient.createNewInstance();
    client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
    client.setUserContext(user);
    
    Channel channel = util.reconstructChannel(org, channelName, client);
    Peer peer = util.createPeer(client, peerPath);
    channel = channel.joinPeer(peer);
    
    util.updateChannelProps( channelName , org, peerPath, eventHub);
    System.out.println("DONE>>>>>>>");
    
  }


}