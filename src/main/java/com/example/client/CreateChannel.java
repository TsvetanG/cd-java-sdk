package com.example.client;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.example.client.impl.ChannelUtil;
import com.example.client.impl.UserFileSystem;

public class CreateChannel {

  public static void main(String[] args) throws CryptoException, InvalidArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, TransactionException, IOException, ProposalException {
 
    String channelName = "drug";
    String org = "druginc";
    String pathToConfigTX = "./store/channels/" + channelName + ".tx";
    CreateChannel create = new CreateChannel();
    User user = new UserFileSystem("Admin", "druginc.drug.com"); 
    create.create(channelName, pathToConfigTX, org, user);

  }

  protected void create(String channelName, String pathToConfigTX, String org, User user) throws InvalidArgumentException, TransactionException, IOException, CryptoException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
    ChannelUtil util = new ChannelUtil();
    HFClient client = HFClient.createNewInstance();
    client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
    client.setUserContext(user);
    Channel channel = util.createNewChannel(pathToConfigTX, channelName, org, client);
    
    
   
  }
  
  


}