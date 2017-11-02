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

package com.example.client.impl;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

public class UserFileSystem implements User  {

  private Enrollment enrollment;
  private String name;
  private String mspId;
  
  public UserFileSystem(String name, String org ) throws UnsupportedEncodingException, FileNotFoundException, IOException {
     //Load the pk files and certificate to set the enrollment
    this.enrollment = new EnrollmentFileSystem(getCertFile(name, org ) , getPkFile(name, org));
    this.mspId = org.substring(0, org.indexOf("."));
    this.name = name;
    
  }

  protected String getPkFile(String uName, String org) throws IOException { 
    File folder = new File("." + getPathToMSP(uName, org) , "keystore");
    File[] files = folder.listFiles();
    return files[0].getCanonicalPath();
  }

  private String getPathToMSP(String uName, String org) {
    return "/store/crypto-config/peerOrganizations/" + org + "/users/" + uName + "@" + org + "/msp";

  }

  protected String getCertFile(String uName, String org) throws IOException {
    File folder = new File(new File("." + getPathToMSP(uName, org)) , "signcerts");
    File[] files = folder.listFiles();
    return files[0].getCanonicalPath();
  }

  @Override
  public String getAccount() { 
    return null;
  }

  @Override
  public String getAffiliation() { 
    return null;
  }

  @Override
  public Enrollment getEnrollment() { 
    return enrollment;
  }

  @Override
  public String getMspId() { 
    return mspId;
  }

  @Override
  public String getName() { 
    return name;
  }

  @Override
  public Set<String> getRoles() { 
    return null;
  }

}
