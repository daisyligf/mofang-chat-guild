package com.mofang.chat.guild.global;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.framework.util.StringUtil;

/**
 * 
 * @author zhaodx
 *
 */
public class ResultValue
{
	private Integer code;
	private String message;
	private Object data;
	
	public Integer getCode() 
	{
		return code;
	}
	
	public void setCode(Integer code)
	{
		this.code = code;
	}

	public String getMessage()
	{
		return message;
	}
	
	public void setMessage(String message) 
	{
		this.message = message;
	}
	
	public Object getData()
	{
		return data;
	}
	
	public void setData(Object data) 
	{
		this.data = data;
	}
	
	public String toJsonString()
	{
		JSONObject json = new JSONObject();
		try
		{
			if(null != code)
				json.put("code", code);
			if(!StringUtil.isNullOrEmpty(message))
				json.put("message", message);
			if(null != data)
			{
				if(data instanceof JSONObject)
					json.put("data", (JSONObject)data);
				else if(data instanceof JSONArray)
					json.put("data", (JSONArray)data);
			}
			return json.toString();
		}
		catch(Exception e)
		{
			return "{\"code\" : 500}";
		}
	}
}