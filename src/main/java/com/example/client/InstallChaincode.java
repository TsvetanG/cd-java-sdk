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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionRequest.Type;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.example.client.impl.ChannelUtil;
import com.example.client.impl.UserFileSystem;

public class InstallChaincode {

  public static void main(String[] args) throws CryptoException, InvalidArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, TransactionException, IOException, ProposalException, ChaincodeEndorsementPolicyParseException {

    String path = "../cd-java-cc";
    String channelName = "drug";
    int version = 12; 
    String chaincodeName = "javacc";
    String peerName = "peer0.druginc.drug.com";
    String org = "druginc";
    InstallChaincode install = new InstallChaincode();
    User user = new UserFileSystem("Admin", "druginc.drug.com"); 
    install.install(path, org ,  peerName , channelName, chaincodeName, version, user);

  }

  protected void install(String path, String org, String peerName, String channelName, String chaincodeName, int version, User user) throws CryptoException, InvalidArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, TransactionException, IOException, ProposalException, ChaincodeEndorsementPolicyParseException {
    HFClient client = HFClient.createNewInstance();
    client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
    client.setUserContext(user);
    ChannelUtil util = new ChannelUtil();
    Peer peer;
    Channel channel = util.reconstructChannel(org, channelName, client);
    
    Collection<Peer> peers = channel.getPeers();
    if(peerName != null) {
      for (Iterator<Peer> iterator = peers.iterator(); iterator.hasNext();) {
        peer = iterator.next();
        if(!peerName.equals(peer.getName())) {
          peers.remove(peer);
        }
      } 
    }
    
    if (peers.isEmpty()) {
      return;
    }
     
    ChaincodeID chaincodeID;
    Collection<ProposalResponse> responses;
    Collection<ProposalResponse> successful = new LinkedList<>();
    Collection<ProposalResponse> failed = new LinkedList<>();
    
    chaincodeID = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(String.valueOf(version)).build();
    
    
    
    InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
    installProposalRequest.setChaincodeID(chaincodeID);

    installProposalRequest.setChaincodeSourceLocation(new File(path));

    installProposalRequest.setChaincodeVersion(String.valueOf(version));
    installProposalRequest.setChaincodeLanguage(Type.JAVA);
    installProposalRequest.setChaincodePath(null);
    responses = client.sendInstallProposal(installProposalRequest, peers);
    
    
    
    for (ProposalResponse response : responses) {
      if (response.getStatus() == ProposalResponse.Status.SUCCESS) { 
        successful.add(response);
      } else {
        failed.add(response);
      }
    }

//    SDKUtils.getProposalConsistencySets(responses);
    // // } 

    if (failed.size() > 0) {
      ProposalResponse first = failed.iterator().next();
      // fail("Not enough endorsers for install :" + successful.size() + ". " +
      // first.getMessage());
    }
    System.out.println("DONE =>>>>>>>>>>>>>>>>>>>>>>>>>>");
    
  }

 

}