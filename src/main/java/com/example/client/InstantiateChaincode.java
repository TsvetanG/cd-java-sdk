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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.UpgradeProposalRequest;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.example.client.impl.ChannelUtil;
import com.example.client.impl.UserFileSystem;

public class InstantiateChaincode {

  public static void main(String[] args) throws CryptoException, InvalidArgumentException, IllegalAccessException,
      InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
      TransactionException, IOException, ProposalException, ChaincodeEndorsementPolicyParseException {

    String chaincodeName = "javacc";
    String channelName = "drug";
    int version = 12;
    String org = "druginc";
    boolean isUpgrade = false;
    InstantiateChaincode instantiate = new InstantiateChaincode();
    User user = new UserFileSystem("Admin", "druginc.drug.com");
    String[] params = new String[] { "Alice", "500", "Bob", "500" };
    instantiate.instantiate(chaincodeName, channelName, org, version, user, params, isUpgrade);

  }

  protected void instantiate(String chaincodeName, String channelName, String org, int version, User user,
      String[] params, boolean isUpgrade) throws InvalidArgumentException, TransactionException, IOException, CryptoException,
      IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException,
      InvocationTargetException, ChaincodeEndorsementPolicyParseException, ProposalException {
    HFClient client = HFClient.createNewInstance();
    client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
    client.setUserContext(user);
    ChannelUtil util = new ChannelUtil();
    Channel channel = util.reconstructChannel(org, channelName, client);

    ChaincodeID chaincodeID;
    Collection<ProposalResponse> responses;
    Collection<ProposalResponse> successful = new LinkedList<>();
    Collection<ProposalResponse> failed = new LinkedList<>();

    chaincodeID = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(String.valueOf(version)).build();
    if (isUpgrade) {
      UpgradeProposalRequest upgrade = client.newUpgradeProposalRequest();
      upgrade.setChaincodeID(chaincodeID);
      upgrade.setProposalWaitTime(60000);
      upgrade.setFcn("init");
      upgrade.setChaincodeName(chaincodeName);
      upgrade.setChaincodeVersion(String.valueOf(version));
      Map<String, byte[]> tm = new HashMap<>();
      tm.put("HyperLedgerFabric", "UpgradeProposalRequest:JavaSDK".getBytes(UTF_8));
      tm.put("method", "UpgradeProposalRequest".getBytes(UTF_8));
      upgrade.setTransientMap(tm);
      upgrade.setArgs(params);

      ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
      chaincodeEndorsementPolicy.fromYamlFile(new File("./store/endorsement/chaincodeendorsementpolicy.yaml"));
      upgrade.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);
      responses = channel.sendUpgradeProposal(upgrade, channel.getPeers());
    } else {

      InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
      instantiateProposalRequest.setProposalWaitTime(60000);
      instantiateProposalRequest.setChaincodeID(chaincodeID);
      instantiateProposalRequest.setFcn("init");
      instantiateProposalRequest.setChaincodeVersion(String.valueOf(version));
      instantiateProposalRequest.setChaincodeName(chaincodeName);

      instantiateProposalRequest.setArgs(params);
      Map<String, byte[]> tm = new HashMap<>();
      tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
      tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
      instantiateProposalRequest.setTransientMap(tm);

      ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
      chaincodeEndorsementPolicy.fromYamlFile(new File("./store/endorsement/chaincodeendorsementpolicy.yaml"));
      instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

      responses = channel.sendInstantiationProposal(instantiateProposalRequest, channel.getPeers());
    }

    for (ProposalResponse response : responses) {
      if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
        successful.add(response);
        System.out.println("SUCCESS-------------------------------------->");
      } else {
        failed.add(response);
        System.out.println("ERROR-------------------------------------->");
      }
    }

    if (failed.size() > 0) {
      ProposalResponse first = failed.iterator().next();
    } else {
      // send the transaction to ordering
      channel.sendTransaction(successful);
    }
    System.out.println("DONE=>>>>>>>>>>>>>>>>>>>>>>>>>");
  }

}