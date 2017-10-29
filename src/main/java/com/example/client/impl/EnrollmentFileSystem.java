package com.example.client.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;

import java.security.Security;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;

import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.apache.commons.io.IOUtils;
import org.hyperledger.fabric.sdk.Enrollment;

public class EnrollmentFileSystem implements Enrollment {

  private String cert;
  private PrivateKey pk;

  public EnrollmentFileSystem(String certFile, String pkFile)
      throws UnsupportedEncodingException, FileNotFoundException, IOException {
    cert = new String(IOUtils.toByteArray(new FileInputStream(new File(certFile))), "UTF-8");
    pk = getPKFromBytes(IOUtils.toByteArray(new FileInputStream(new File(pkFile))));

  }

  protected PrivateKey getPKFromBytes(byte[] bytes) throws IOException {
    final Reader reader = new StringReader(new String(bytes));

    PrivateKeyInfo pair;
    try (PEMParser pemParser = new PEMParser(reader)) {
      pair = (PrivateKeyInfo) pemParser.readObject();
    }

    PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
        .getPrivateKey(pair);

    return privateKey;
  }

  @Override
  public String getCert() {
    return cert;
  }

  @Override
  public PrivateKey getKey() {
    return pk;
  }

  static {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
  }

}
