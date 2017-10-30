package com.example.client;

import java.util.Collection;
import java.util.Set;

import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.User;

public interface Organization {

  public Set<String> getOrdererNames();

  public User getPeerAdmin();

  public String getOrdererLocation(String orderName);

  public Set<String> getPeerNames();

  public String getPeerLocation(String peerName);

  public String getEventHubLocation(String eventHubName);

  public Set<String> getEventHubNames();

  public Collection<Orderer> getOrderers();

}
