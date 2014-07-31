package com.mofang.chat.guild.redis;

/**
 * 
 * @author zhaodx
 *
 */
public interface ResultCacheRedis
{
	public boolean saveCache(String key, String value, int expire) throws Exception;
	
	public boolean deleteCache(String key) throws Exception;
	
	public String getCache(String key) throws Exception;
}