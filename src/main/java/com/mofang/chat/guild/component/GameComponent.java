package com.mofang.chat.guild.component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONObject;

import com.mofang.chat.guild.entity.Game;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.global.ReturnCode;
import com.mofang.chat.guild.redis.ResultCacheRedis;
import com.mofang.chat.guild.redis.impl.ResultCacheRedisImpl;
import com.mofang.framework.net.http.HttpClientSender;
import com.mofang.framework.util.StringUtil;

/**
 * 
 * @author zhaodx
 *
 */
public class GameComponent
{
    	private final static int THREADS = Runtime.getRuntime().availableProcessors() + 1;
    	private final ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
    	private final static GameComponent instance = new GameComponent();
    	
    	private GameComponent() {}
    	
    	public static GameComponent getInstance()
    	{
    	    return instance;
    	}
    	
	public Game getInfo(int gameId) throws Exception
	{
		///从本地redis获取游戏信息, 如果本地没有缓存，则调用服务端接口获取，并且调用后台更新服务
		String key = RedisKey.CACHE_GAME_INFO_KEY_PREFIX + gameId;
		ResultCacheRedis cacheRedis = ResultCacheRedisImpl.getInstance();
		String value = cacheRedis.getCache(key);
		Game game = null;
		GameFutureTask task = new GameFutureTask(gameId);
		
		if(!StringUtil.isNullOrEmpty(value))
		{
			JSONObject json = new JSONObject(value);
			game = Game.buildByJson(json);
		} else 
		{
		    	// 主要考虑到初始化的情况
		    	game = task.getInfoByAPI(gameId);
		}
		
		Future<Integer> future = executorService.submit(task);
		if (future.get() != 0)
		{
		    	GlobalObject.ERROR_LOG.error("GameComponent update GameInfo failed.");
		}
		return game;
		
	}
	
	public class GameFutureTask implements Callable<Integer>
	{
	    private int gameId;
	    
	    public GameFutureTask(int gameId) {
		super();
		this.gameId = gameId;
	    }

	    @Override
	    public Integer call()
	    {
		String key = RedisKey.CACHE_GAME_INFO_KEY_PREFIX + gameId;
		ResultCacheRedis cacheRedis = ResultCacheRedisImpl.getInstance();
		
		/// 异步调用服务端接口获取
		Game game = getInfoByAPI(gameId);
		try
		{
			///保存到redis中
			cacheRedis.saveCache(key, game.toJson().toString());
			return ReturnCode.SUCCESS;
			
		}
		catch(Exception e)
		{	
			GlobalObject.ERROR_LOG.error("GameFuture.call throws an error." + e);
			return -1;
		}
		
	    }
	    
	public Game getInfoByAPI(int gameId) 
	{
	    String url = GlobalConfig.GAME_INFO_URL + "?id=" + gameId;
	    try 
	    {
		String result = HttpClientSender.get(
			GlobalObject.HTTP_CLIENT_API, url);
		JSONObject json = new JSONObject(result);
		int code = json.optInt("code", -1);
		if (0 != code)
		    return null;

		JSONObject data = json.optJSONObject("data");
		if (null == data)
		    return null;

		Game game = new Game();
		game.setGameId(gameId);
		game.setGameName(data.optString("name", ""));
		game.setIcon(data.optString("app_icon", ""));
		return game;
	    } catch (Exception e) 
	    {
		GlobalObject.ERROR_LOG.error(
			"GameComponent.getInfoByAPI throws an error.", e);
		return null;
	    }
	}
    }
	
	
}