/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bsb.vote.service;

import bsb.vote.ui.MainUI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 *
 * @author Administrator
 */
public class DoVote {

    /**
     * @param VOTE_NUM
     * @param ITEM_ID
     * @param V_ID
     */
    public static void doVote(int VOTE_NUM, String ITEM_ID, String V_ID){
        // TODO code application logic here
        MainUI.startFlag=false;
        DefaultHttpClient httpclient = new DefaultHttpClient();
        
        //设置代理开始。如果代理服务器需要验证的话，可以修改用户名和密码  
        //192.168.1.107为代理地址 808为代理端口 UsernamePasswordCredentials后的两个参数为代理的用户名密码  
//        httpclient.getCredentialsProvider().setCredentials(new AuthScope("127.0.0.1", 8888), new UsernamePasswordCredentials("", ""));
//        HttpHost proxy = new HttpHost("127.0.0.1", 8888);
//        httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        //设置代理结束
        MainUI.voteNum=0;
        MainUI.regNum=0;
        MainUI.validNum=0;
        int j = 0;
//        try {
//            getId(httpclient);
//            getList(httpclient);
//        } catch (IOException | JSONException ex) {
//            Logger.getLogger(DoVote.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        注册
        for(int i=0;i<VOTE_NUM;i++){
            try {
                MainUI.voteNum++;
                httpclient.getCookieStore().clear();
                Thread.sleep(1000);
                try {
                    if (regUser(httpclient)) {
                        MainUI.regNum++;
                    }else{
                        continue;
                    }
                } catch (IOException | JSONException ex) {
                    Logger.getLogger(DoVote.class.getName()).log(Level.SEVERE, null, ex);
                }
                Thread.sleep(500);
                try {
                    if(vote(httpclient,ITEM_ID, V_ID)){
                        MainUI.validNum++;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(DoVote.class.getName()).log(Level.SEVERE, null, ex);
                }
                j++;
                if(j==15){
                    Thread.sleep(60000*2);
                    j=0;
                }
            }
            catch (InterruptedException ex) {
                Logger.getLogger(DoVote.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        MainUI.startFlag=true;
        //关闭连接
        httpclient.getConnectionManager ().shutdown();
    }
  
    //平日投票获取itemid和vid
    private static void getId(DefaultHttpClient httpclient) throws UnsupportedEncodingException, IOException, JSONException {
        HttpGet httpget = new HttpGet("http://qr.cntv.cn/bsb/vote/Aindex?vote_type=0");
        System.out.println("请求: " + httpget.getRequestLine());
        //设置头
        httpget.setHeader("Host", "qr.cntv.cn");
        httpget.setHeader("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");
        httpget.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        httpget.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        //httpget.setHeader("Accept-Encoding", "gzip, deflate");
        httpget.setHeader("DNT", "1");
        httpget.setHeader("Referer", "http://qr.cntv.cn/bsb");
        httpget.setHeader("Connection", "keep-alive");
        //httpget.setHeader("Cookie", "m_cntv_app=bsb;m_cntv_gapp=bsb;m_cntv_uid=7047108;m_cntv_nickname=%E5%A4%AE%E8%A7%86%E7%BD%91%E5%8F%8B;m_cntv_avatar=http%3A%2F%2Fm.passport.cntv.cn%2Fimages%2Favatar.jpg;m_cntv_mobile=312fJo3LydoFGja0HbDQVxa8okTmRQFeg90mk9K%2BGB4MxSQnZobzTA;m_cntv_realname=d91arKGm5cJt%2BjsVAqMgDcNhgLiEG3sVY693zP4;m_cntv_auth=070djYVzCvXdNB%2B2D06%2BQz2xUW3RgaNBewF0Os2m05ph2AQ37AXbRaKSZuoD8VEUA7EkpjhMAw;iphoneFix=FixHidden");

        HttpResponse response = httpclient.execute(httpget);

        Header[] heads = response.getAllHeaders();
        // 打印所有响应头   
//        for (Header h : heads) {
//            System.out.println(h.getName() + ":" + h.getValue());
//        }

        // 执行   
        HttpEntity entity = response.getEntity();
       // 显示结果   
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
        StringBuilder builder = new StringBuilder();
        System.out.println("获取的信息-----------------------------------------");
        for (String line = reader.readLine(); line != null; line = reader 
                .readLine()) { 
            builder.append(line); 
        } 
        try {
            JSONObject jsonObject = new JSONObject(builder.toString());
            System.out.println("error：" + jsonObject.getInt("error"));
            System.out.println(jsonObject.getString("msg"));
            if(jsonObject.getInt("error")==0){
                System.out.println("in in in");
                JSONArray ja = jsonObject.getJSONObject("params").getJSONArray("items");
                System.out.println("\n将Json数据解析为Map：");
                
                System.out.println("vid: " + jsonObject.getJSONObject("params").getString("vid"));
                
                System.out.println("itemid: " + ja.getJSONObject(0).getString("itemid")
                + " title: " + ja.getJSONObject(0).getString("title") + " singers: "
                + ja.getJSONObject(0).getString("singers"));
                
                System.out.println("itemid: " + ja.getJSONObject(1).getString("itemid")
                + " title: " + ja.getJSONObject(1).getString("title") + " singers: "
                + ja.getJSONObject(1).getString("singers"));
            }
        } catch (JSONException e) {
            System.out.println("\nrrrrrrrrrrrrrrr"+e.toString());
        }
    }
    
    //注册用户
    private static boolean regUser(DefaultHttpClient httpclient) throws UnsupportedEncodingException, IOException, JSONException {
//        httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.ACCEPT_ALL);  
        HttpPost httppost = new HttpPost("http://m.passport.cntv.cn/site/reg");
        System.out.println("请求: " + httppost.getRequestLine());
        // 构造最简单的字符串数据
        //生成手机号
        String n1 = (String.valueOf(System.currentTimeMillis()%(9999999-1111111)+1111111));
        String n2 = (String.valueOf(System.currentTimeMillis()%(999-111)+111));
        String phoneNum = "1"+n1+n2;
        System.out.print(phoneNum+"\n");
        StringEntity reqEntity = new StringEntity("Form%5Busername%5D="+phoneNum+"&Form%5Brealname%5D=&Form%5Bpassword%5D=123456&app=bsb");
        // 设置请求的数据   
        httppost.setEntity(reqEntity);
        //设置头
        httppost.setHeader("Host", "m.passport.cntv.cn");
        httppost.setHeader("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");
        httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        httppost.setHeader("Accept-Encoding", "gzip, deflate");
        httppost.setHeader("X-Requested-With", "XMLHttpRequest");
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httppost.setHeader("Referer", "http://m.passport.cntv.cn/html/reg.html?rurl=http%3A%2F%2Fqr.cntv.cn%2Fbsb");
        httppost.setHeader("Connection", "keep-alive");
        httppost.setHeader("Pragma", "no-cache");
        httppost.setHeader("Cache-Control", "no-cache");
        //执行
        HttpResponse response = httpclient.execute(httppost);

//        Header[] heads = response.getAllHeaders();
        // 打印所有响应头   
//        for (Header h : heads) {
//            System.out.println(h.getName() + ":" + h.getValue());
//        }

        //获取响应信息
        response.setEntity(new GzipDecompressingEntity(response.getEntity()));
        HttpEntity entity = response.getEntity();
        // 显示结果
//            GZIPInputStream gInputStream = new GZIPInputStream(entity.getContent());
//            byte[] by = new byte[1024];
//            StringBuilder builder = new StringBuilder();
//            int len = 0;
//            while ((len = gInputStream.read(by)) != -1) {
//                builder.append(new String(by, 0, len, "UTF-8"));
//            }
        InputStreamReader rr = new InputStreamReader(entity.getContent(), "UTF-8");
        BufferedReader reader = new BufferedReader(rr);
        StringBuilder builder = new StringBuilder();
        System.out.println("返回信息-----------------------------------------");
        for (String line = reader.readLine(); line != null; line = reader 
                .readLine()) { 
            builder.append(line); 
        }
        rr.close();
        try {
            JSONObject jsonObject = new JSONObject(builder.toString());
            System.out.println("+++++++++++++++++++++\n" + jsonObject.getInt("error"));
            System.out.println("--------" + jsonObject.getString("msg"));
            if(jsonObject.getInt("error")!=0){
                try {
                    Thread.sleep(60000*3);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DoVote.class.getName()).log(Level.SEVERE, null, ex);
                }
                return false;
            }else{
                return true;
            }
        } catch (JSONException e) {
            return false;
        }
    }
    //投票
    private static boolean vote(DefaultHttpClient httpclient, String ITEM_ID, String V_ID) throws UnsupportedEncodingException, IOException {
//        HttpGet httpget = new HttpGet("http://qr.cntv.cn/bsb/vote/Post?itemid=3249&vid=366");
        HttpGet httpget = new HttpGet("http://qr.cntv.cn/bsb/vote/Post?itemid="+ITEM_ID+"&vid="+V_ID);
        System.out.println("请求: " + httpget.getRequestLine());
        //设置头
        httpget.setHeader("Host", "qr.cntv.cn");
        httpget.setHeader("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");
        httpget.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        httpget.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        httpget.setHeader("Accept-Encoding", "gzip, deflate");
        httpget.setHeader("DNT", "1");
        httpget.setHeader("Referer", "http://qr.cntv.cn/bsb");
        httpget.setHeader("Connection", "keep-alive");
        //httpget.setHeader("Cookie", "m_cntv_app=bsb;m_cntv_gapp=bsb;m_cntv_uid=7047108;m_cntv_nickname=%E5%A4%AE%E8%A7%86%E7%BD%91%E5%8F%8B;m_cntv_avatar=http%3A%2F%2Fm.passport.cntv.cn%2Fimages%2Favatar.jpg;m_cntv_mobile=312fJo3LydoFGja0HbDQVxa8okTmRQFeg90mk9K%2BGB4MxSQnZobzTA;m_cntv_realname=d91arKGm5cJt%2BjsVAqMgDcNhgLiEG3sVY693zP4;m_cntv_auth=070djYVzCvXdNB%2B2D06%2BQz2xUW3RgaNBewF0Os2m05ph2AQ37AXbRaKSZuoD8VEUA7EkpjhMAw;iphoneFix=FixHidden");

        // 执行   
        HttpResponse response = httpclient.execute(httpget);

//        Header[] heads = response.getAllHeaders();
        // 打印所有响应头   
//        for (Header h : heads) {
//            System.out.println(h.getName() + ":" + h.getValue());
//        }

        response.setEntity(new GzipDecompressingEntity(response.getEntity()));
        HttpEntity entity = response.getEntity();
        
         // 显示结果
//            GZIPInputStream gInputStream = new GZIPInputStream(entity.getContent());
//            response.setEntity(new GzipDecompressingEntity(response.getEntity())); 
//            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
//            byte[] by = new byte[1024];
//            StringBuilder builder = new StringBuilder();
//            int len = 0;
//            while ((len = gInputStream.read(by)) != -1) {
//                builder.append(new String(by, 0, len, "UTF-8"));
//            }
        InputStreamReader rr = new InputStreamReader(entity.getContent(), "UTF-8");
        BufferedReader reader = new BufferedReader(rr);
        StringBuilder builder = new StringBuilder();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
//        StringBuilder builder = new StringBuilder();
        System.out.println("投票信息-----------------------------------------");
        for (String line = reader.readLine(); line != null; line = reader 
                .readLine()) { 
            builder.append(line); 
        } 
        rr.close();
        try {
            JSONObject jsonObject = new JSONObject(builder.toString());
            System.out.println("+++++++++++++++++++++\n" + jsonObject.getInt("error"));
            System.out.println(jsonObject.getString("msg"));
            if(jsonObject.getInt("error")==0){
                if( "成功参与投票，本轮投票将获得1积分".equals(jsonObject.getString("msg"))){
                    return  true;
                }else{
                    try {
                        Thread.sleep(60000*3);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DoVote.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return  false;
                }
            }
        } catch (JSONException e) {
            return false;
        }
        return false;
    }
    
    
     //获取列表
    private static boolean getList(DefaultHttpClient httpclient) throws UnsupportedEncodingException, IOException, JSONException {
        HttpPost httppost = new HttpPost("http://192.168.1.110/WAN/ttl_list.cgi");
        System.out.println("请求: " + httppost.getRequestLine());
        // 构造最简单的字符串数据
        StringEntity reqEntity = new StringEntity("cCMD_EXEC.x=100&cPARENTID=/01/01/&cSORTTYPE=0&SID=0112345602&cSTARTINDEX=0&cCMD_EXEC.y=100&cCOUNT=10");
        // 设置请求的数据   
        httppost.setEntity(reqEntity);
        //设置头
        httppost.setHeader("Host", "http://192.168.1.110");
        httppost.setHeader("User-Agent", "EI-LAN-REMOTE-CALL");
        //httppost.setHeader("Accept-Encoding", "gzip, deflate");
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httppost.setHeader("Expect", "100-continue");
        //执行
        HttpResponse response = httpclient.execute(httppost);

        Header[] heads = response.getAllHeaders();
        // 打印所有响应头   
//        for (Header h : heads) {
//            System.out.println(h.getName() + ":" + h.getValue());
//        }

        //获取响应信息
        HttpEntity entity = response.getEntity();
        // 显示结果   
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
        StringBuilder builder = new StringBuilder();
        System.out.println("返回信息-----------------------------------------");
        for (String line = reader.readLine(); line != null; line = reader 
                .readLine()) { 
            builder.append(line); 
        } 
        try {
            JSONObject jsonObject = new JSONObject(builder.toString());
            System.out.println("+++++++++++++++++++++\n" + jsonObject.getInt("error"));
            System.out.println("--------" + jsonObject.getString("msg"));
            if(jsonObject.getInt("error")!=0){
                try {
                    Thread.sleep(60000*3);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DoVote.class.getName()).log(Level.SEVERE, null, ex);
                }
                return false;
            }else{
                return true;
            }
        } catch (JSONException e) {
            return false;
        }
    }
    
}
