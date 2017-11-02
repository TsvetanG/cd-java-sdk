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


package com.example.client;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeEvent;
import org.hyperledger.fabric.sdk.ChaincodeEventListener;
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

public class EventChaincode implements ChaincodeEventListener {

 
  public static void main(String[] args)
      throws CryptoException, InvalidArgumentException, TransactionException, IOException, ProposalException,
      InterruptedException, ExecutionException, TimeoutException, IllegalAccessException, InstantiationException,
      ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
    String channelName = "transfer";
     
    String chainCode = "javacc";
    String org = "druginc";
    String ops = "transfer";
    User user = new UserFileSystem("Admin", "druginc.drug.com");
    new EventChaincode().start(chainCode, channelName, org, user);
    System.out.println("DONE ->>>>>>>>>>>>>>>");
  }

  private void start(String chainCode, String channelName, String org, User user) throws CryptoException, InvalidArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, TransactionException, IOException, InterruptedException {
    ChannelUtil util = new ChannelUtil();
    HFClient client = HFClient.createNewInstance();
    client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
    client.setUserContext(user);
    
    Channel channel = util.reconstructChannel(org, channelName, client);
    String eventName = channel.registerChaincodeEventListener(Pattern.compile(".*"), Pattern.compile(Pattern.quote("event")), this);
    Thread.currentThread().sleep(100000000);
  }

  @Override
  public void received(String handle, BlockEvent blockEvent, ChaincodeEvent chaincodeEvent) {
    System.out.println("Event received. Happens when transaction is ordered !");
    System.out.println("payload" + new String(chaincodeEvent.getPayload()));
    System.out.println("event name:" + new String(chaincodeEvent.getEventName()));
    
    
  }

  
}
