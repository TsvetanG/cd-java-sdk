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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.example.client.dto.QueryResult;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
public class QueryChaincode {

  public static void main(String[] args) throws CryptoException, InvalidArgumentException, TransactionException,
      IOException, ProposalException, InterruptedException, ExecutionException, TimeoutException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
 
    String channelName = "drugchan";
    String chainCode = "bbb";
    String peerName = "peer0.druginc.drug.com";
    String[] params = new String[] { "Bob" }; 
    
    User user = new UserFileSystem("Admin", "druginc.drug.com");
    new QueryChaincode().query(params, "druginc", peerName, channelName, chainCode, user);

  }

  @RequestMapping(value = "/chaincode/query", method = RequestMethod.GET)
  public QueryResult executeQuery(@RequestParam(value = "accountHolder") String accountHolder)
          throws CryptoException, InvalidArgumentException, TransactionException, IOException, ProposalException,
          InterruptedException, ExecutionException, TimeoutException, IllegalAccessException, InstantiationException,
          ClassNotFoundException, NoSuchMethodException, InvocationTargetException
  {
      String channelName = "drugchan";
      String chainCode = "bbb";
      String peerName = "peer0.druginc.drug.com";
      String[] params = new String[] { accountHolder };
      System.out.println(accountHolder);
      User user = new UserFileSystem("Admin", "druginc.drug.com");
      QueryResult queryResult = new QueryResult();
      
      String result = query(params, "druginc", peerName, channelName, chainCode, user);
      queryResult.setResponse(result);
      System.out.println("executed query: result is " + queryResult.getResponse());
      return queryResult;
  } 

  public String query(String[] params, String org, String peerName, String channelName, String chainCode, User user)
  throws CryptoException, InvalidArgumentException, TransactionException, IOException, InterruptedException,
  ExecutionException, TimeoutException, ProposalException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {

    ChannelUtil util = new ChannelUtil();
    HFClient client = HFClient.createNewInstance();
    client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
    client.setUserContext(user);
    Channel channel = util.reconstructChannel(org, channelName, peerName, client);

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
        return payload;
        
      }
    }
    return "";
  }

    @ModelAttribute
    public void setVaryResponseHeader(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
    }
}
