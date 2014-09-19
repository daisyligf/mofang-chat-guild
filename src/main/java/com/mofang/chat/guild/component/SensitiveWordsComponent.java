package com.mofang.chat.guild.component;

import java.net.URLEncoder;

import org.json.JSONObject;

import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.framework.net.http.HttpClientSender;

/**
 * 
 * @author daisyli
 * 
 */
public class SensitiveWordsComponent
{
    public static String filter(String words)
    {
	try 
	{
	    long start = System.currentTimeMillis();
	    String url = GlobalConfig.SENSITIVE_WORDS_SERVICE_URL + URLEncoder.encode(words, "UTF-8");
	    String result = HttpClientSender.get(GlobalObject.HTTP_CLIENT_API, url);
	    JSONObject json = new JSONObject(result);
	    String after = json.optString("out", "");
	    long end = System.currentTimeMillis();
	    GlobalObject.INFO_LOG.info("SENSITIVE_WORDS_SERVICE_URL costs time " + (end - start) + " ms");
	    GlobalObject.INFO_LOG.info("at SensitiveWordsComponent.filter before:" + words + ",after:" + after);
	    return after;
	}
	catch (Exception e) 
	{
	    GlobalObject.ERROR_LOG.error("at SensitiveWordsComponent.filter throw an error.", e);
	    return words;
	}
    }
}
