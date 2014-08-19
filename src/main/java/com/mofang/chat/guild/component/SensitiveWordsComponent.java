package com.mofang.chat.guild.component;

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
	String url = GlobalConfig.SENSITIVE_WORDS_SERVICE_URL + words;
	try 
	{
	    String result = HttpClientSender.get(GlobalObject.HTTP_CLIENT_API,
		    url);
	    JSONObject json = new JSONObject(result);
	    String after = json.optString("out", "");
	    return after;
	} 
	catch (Exception e) 
	{
	    return "";
	}
    }
}
