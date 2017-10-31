package com.example.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.TxReadWriteSetInfo;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.example.client.impl.ChannelUtil;
import com.example.client.impl.UserFileSystem;

public class InvokeChaincode {
  
  
  public static void main(String[] args) throws CryptoException, InvalidArgumentException, TransactionException, IOException, ProposalException, InterruptedException, ExecutionException, TimeoutException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
    if (args == null || args.length == 0 ) {
      args = new String[] { "invoke" , "a", "b" , "2" };
    }
    String channelName = "transfer";
    String chainCode = "transfercc";
    User user = new UserFileSystem("Admin", "druginc.drug.com"); 
    TransactionEvent event = new InvokeChaincode().invoke(args, channelName, chainCode, user);
    if (event != null) {
//      event.getTransactionID().
    }
  }

  public TransactionEvent invoke(String[] params, String channelName, String chainCode, User user) throws CryptoException, InvalidArgumentException, TransactionException, IOException, InterruptedException, ExecutionException, TimeoutException, ProposalException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
    
    ChannelUtil util = new ChannelUtil();
    HFClient client = HFClient.createNewInstance();
    client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
    client.setUserContext(user);
    Channel channel = util.reconstructChannel(channelName, client);
    
    ChaincodeID chaincodeID;
    
//    chaincodeID = ChaincodeID.newBuilder().setName(chainCode).setPath("main/java").build();
    chaincodeID = ChaincodeID.newBuilder().setName(chainCode).setPath("chaincode_example02").build();
    
    TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
    transactionProposalRequest.setChaincodeID(chaincodeID);
    transactionProposalRequest.setFcn("invoke");
    transactionProposalRequest.setArgs(new String[] { "a", "b", "10" });
    
    Map<String, byte[]> tm2 = new HashMap<>();
    tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
    tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
    tm2.put("result", ":)".getBytes(UTF_8)); /// This should be returned see chaincode.
    transactionProposalRequest.setTransientMap(tm2);
    
    Collection<ProposalResponse> successful = new LinkedList<>();
    Collection<ProposalResponse> failed = new LinkedList<>();
    
    Collection<ProposalResponse> propResponse = channel.sendTransactionProposal(transactionProposalRequest, channel.getPeers());
    for (ProposalResponse response : propResponse) {
      if (response.getStatus() == ProposalResponse.Status.SUCCESS) { 
        successful.add(response);
      } else {
        failed.add(response);
      }
    } 
 
    if (failed.size() > 0) {
      ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
      return null;
    } 

    ProposalResponse resp = propResponse.iterator().next();
    byte[] x = resp.getChaincodeActionResponsePayload(); // This is the data returned by the chaincode.
    String resultAsString = null;
    if (x != null) {
      resultAsString = new String(x, "UTF-8");
    } 

    TxReadWriteSetInfo readWriteSetInfo = resp.getChaincodeActionResponseReadWriteSetInfo(); 

    ChaincodeID cid = resp.getChaincodeID(); 

    ////////////////////////////
    // Send Transaction Transaction to orderer 
    return channel.sendTransaction(successful).get(10000, TimeUnit.SECONDS);
  }

}
