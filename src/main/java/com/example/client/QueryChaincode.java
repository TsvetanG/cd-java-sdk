package com.example.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.example.client.impl.ChannelUtil;
import com.example.client.impl.UserFileSystem;

public class QueryChaincode {

  public static void main(String[] args) throws CryptoException, InvalidArgumentException, TransactionException,
      IOException, ProposalException, InterruptedException, ExecutionException, TimeoutException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
 
    String channelName = "transfer";
    String chainCode = "javacc";
    String[] params = new String[] { "Bob" }; 
    User user = new UserFileSystem("Admin", "druginc.drug.com");
    new QueryChaincode().query(params, "druginc", channelName, chainCode, user);

  }

  public void query(String[] params, String org, String channelName, String chainCode, User user)
      throws CryptoException, InvalidArgumentException, TransactionException, IOException, InterruptedException,
      ExecutionException, TimeoutException, ProposalException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {

    ChannelUtil util = new ChannelUtil();
    HFClient client = HFClient.createNewInstance();
    client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
    client.setUserContext(user);
    Channel channel = util.reconstructChannel(org, channelName, client);

    ChaincodeID chaincodeID;

    chaincodeID = ChaincodeID.newBuilder().setName(chainCode).build();
    QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
    queryByChaincodeRequest.setArgs(params);
    queryByChaincodeRequest.setFcn("query"); 
    queryByChaincodeRequest.setChaincodeID(chaincodeID);

    Map<String, byte[]> tm2 = new HashMap<>();
    tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
    tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
    queryByChaincodeRequest.setTransientMap(tm2);

    Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest, channel.getPeers());
    for (ProposalResponse proposalResponse : queryProposals) {
      if (!proposalResponse.isVerified() || proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
       
      } else {
        String payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
        System.out.println("Result > " + payload);
      }
    }
  }

}
