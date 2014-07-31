package com.mofang.chat.guild.global;

/**
 * 
 * @author zhaodx
 *
 */
public class ReturnCodeHelper
{
	public static ResultValue serverError()
	{
		ResultValue result = new ResultValue();
		return serverError(result);
	}
	
	public static ResultValue serverError(ResultValue result)
	{
		try
		{
			result.setCode(ReturnCode.SERVER_ERROR);
			return result;
		}
		catch(Exception e)
		{
			return result;
		}
	}
}