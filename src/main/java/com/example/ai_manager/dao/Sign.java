package com.example.ai_manager.dao;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Copyright (year) Beijing Volcano Engine Technology Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public  class Sign {

    private static final BitSet URLENCODER = new BitSet(256);

    private static final String CONST_ENCODE = "0123456789ABCDEF";
    public static final Charset UTF_8 = StandardCharsets.UTF_8;


    static {
        int i;
        for (i = 97; i <= 122; ++i) {
            URLENCODER.set(i);
        }

        for (i = 65; i <= 90; ++i) {
            URLENCODER.set(i);
        }

        for (i = 48; i <= 57; ++i) {
            URLENCODER.set(i);
        }
        URLENCODER.set('-');
        URLENCODER.set('_');
        URLENCODER.set('.');
        URLENCODER.set('~');
    }

    public static String getAuthorization(String method, String path, Map<String, String> headers,
                                          Map<String, String> queryList, String action, String version, String ak, String sk, String region,
                                          String service)
            throws Exception {

        String xDate = headers.get("X-Date");
        assert xDate != null;
        String shortXDate = xDate.substring(0, 8);
        // 表明有哪些 header 字段参与签名。
        StringBuilder signHeader = new StringBuilder();
        for (String key : headers.keySet()) {
            if ("Authorization".equals(key)) {
                continue;
            }
            signHeader.append(key.toLowerCase()).append(";");
        }
        signHeader.deleteCharAt(signHeader.length() - 1);
        System.out.println("===>signHeader:" + signHeader);
        String queryStr = getQuery(queryList, action, version);

        StringBuilder canonicalStringBuilder = new StringBuilder();
        canonicalStringBuilder.append(method).append("\n")
                .append(path).append("\n")
                .append(queryStr).append("\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if ("Authorization".equals(entry.getKey())) {
                continue;
            }
            canonicalStringBuilder.append(entry.getKey().toLowerCase()).append(":").append(entry.getValue())
                    .append("\n");
        }
        canonicalStringBuilder.append("\n")
                .append(signHeader).append("\n");
        String xContentSha256 = headers.get("X-Content-Sha256");
        if (xContentSha256 != null) {
            canonicalStringBuilder.append(xContentSha256);
        }
        System.out.println("===>canonicalStringBuilder:" + canonicalStringBuilder);

        String hashcanonicalString = hashSHA256(canonicalStringBuilder.toString().getBytes());
        String credentialScope = shortXDate + "/" + region + "/" + service + "/request";
        String signString = "HMAC-SHA256" + "\n" + xDate + "\n" + credentialScope + "\n" + hashcanonicalString;

        byte[] signKey = genSigningSecretKeyV4(sk, shortXDate, region, service);
//        String signature = HexFormat.of().formatHex(hmacSHA256(signKey, signString));
        String signature = Hex.encodeHexString(hmacSHA256(signKey, signString)).toLowerCase();
        return "HMAC-SHA256" +
                " Credential=" + ak + "/" + credentialScope +
                ", SignedHeaders=" + signHeader +
                ", Signature=" + signature;
    }

    public static String getXDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(date);
    }

    public static String getQuery(Map<String, String> query, String action, String version) {
        SortedMap<String, String> realQueryList = new TreeMap<>(query);
        realQueryList.put("Action", action);
        realQueryList.put("Version", version);
        StringBuilder querySB = new StringBuilder();
        for (String key : realQueryList.keySet()) {
            querySB.append(signStringEncoder(key)).append("=").append(signStringEncoder(realQueryList.get(key)))
                    .append("&");
        }
        querySB.deleteCharAt(querySB.length() - 1);
        return querySB.toString();
    }

    private static String signStringEncoder(String source) {
        if (source == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(source.length());
        ByteBuffer bb = UTF_8.encode(source);
        while (bb.hasRemaining()) {
            int b = bb.get() & 255;
            if (URLENCODER.get(b)) {
                buf.append((char) b);
            } else if (b == 32) {
                buf.append("%20");
            } else {
                buf.append("%");
                char hex1 = CONST_ENCODE.charAt(b >> 4);
                char hex2 = CONST_ENCODE.charAt(b & 15);
                buf.append(hex1);
                buf.append(hex2);
            }
        }

        return buf.toString();
    }

    public static String hashSHA256(byte[] content) throws Exception {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

//            return HexFormat.of().formatHex(md.digest(content));
            return Hex.encodeHexString(md.digest(content));
        } catch (Exception e) {
            throw new Exception(
                    "Unable to compute hash while signing request: "
                            + e.getMessage(), e);
        }
    }

    public static byte[] hmacSHA256(byte[] key, String content) throws Exception {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(content.getBytes());
        } catch (Exception e) {
            throw new Exception(
                    "Unable to calculate a request signature: "
                            + e.getMessage(), e);
        }
    }

    private static byte[] genSigningSecretKeyV4(String secretKey, String date, String region, String service)
            throws Exception {
        byte[] kDate = hmacSHA256((secretKey).getBytes(), date);
        byte[] kRegion = hmacSHA256(kDate, region);
        byte[] kService = hmacSHA256(kRegion, service);
        return hmacSHA256(kService, "request");
    }
}
