package com.example.client;

import java.io.IOException;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.example.client.impl.ChannelUtil;

public class InvokeChaincode {
  
  
  public static void main(String[] args) throws CryptoException, InvalidArgumentException, TransactionException, IOException {
    if (args == null || args.length == 0 ) {
      args = new String[] { "invoke" , "a", "b" , "2" };
    }
    String channelName = "transfer";
    new InvokeChaincode().invoke(args, channelName);
  }

  public void invoke(String[] params, String channelName) throws CryptoException, InvalidArgumentException, TransactionException, IOException {
    
    ChannelUtil util = new ChannelUtil();
    HFClient client = HFClient.createNewInstance();
    client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
    Channel channel = util.reconstructChannel(channelName, client);
    
    
    
  }

}
