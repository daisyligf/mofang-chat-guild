package com.mofang.chat.guild.component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONObject;

import com.mofang.chat.guild.entity.User;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.global.common.UserStatus;
import com.mofang.chat.guild.global.common.UserType;
import com.mofang.chat.guild.redis.ResultCacheRedis;
import com.mofang.chat.guild.redis.impl.ResultCacheRedisImpl;
import com.mofang.framework.net.http.HttpClientSender;
import com.mofang.framework.util.StringUtil;

/**
 * 
 * @author zhaodx
 * 
 */
public class UserComponent {
	private final static int THREADS = Runtime.getRuntime().availableProcessors() + 1;
	private final ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
	private final static UserComponent instance = new UserComponent();

	private UserComponent() 
	{
	}

	public static UserComponent getInstance() {
		return instance;
	}

	public User getInfo(long userId) throws Exception {
		// /先从本地获取用户信息, 如果本地没有缓存，则调用服务端接口获取
		String key = RedisKey.CACHE_USER_INFO_KEY_PREFIX + userId;
		ResultCacheRedis cacheRedis = ResultCacheRedisImpl.getInstance();
		String value = cacheRedis.getCache(key);
		User user = null;
		UserFutureTask task = new UserFutureTask(userId);
		if (!StringUtil.isNullOrEmpty(value)) {
			JSONObject json = new JSONObject(value);
			return User.buildByJson(json);
		} else {
			// 调用服务端接口获取
			user = task.getInfoByAPI(userId);
		}

		Future<Long> future = executorService.submit(task);
		if (future.get() != 0L)
		{
		    GlobalObject.ERROR_LOG.error("UserComponent update UserInfo failed.");
		}

		try {
			// /保存到redis中
			cacheRedis.saveCache(key, user.toJson().toString());
			return user;
		} catch (Exception e) {
			throw e;
		}
	}

	public class UserFutureTask implements Callable<Long> 
	{
		private long userId;

		public UserFutureTask(long userId) {
			super();
			this.userId = userId;
		}

		@Override
		public Long call() 
		{
			String key = RedisKey.CACHE_USER_INFO_KEY_PREFIX + userId;
			ResultCacheRedis cacheRedis = ResultCacheRedisImpl.getInstance();

			// 异步调用服务端接口获取
			User user = getInfoByAPI(userId);
			try {
				// /保存到redis中
				cacheRedis.saveCache(key, user.toJson().toString());
				return 0L;

			} catch (Exception e) {
				GlobalObject.ERROR_LOG
						.error("UserFutureTask.call throws an error." + e);
				return -1L;
			}

		}

		public User getInfoByAPI(long userId) {
			String url = GlobalConfig.USER_INFO_URL + "?to_uid=" + userId;
			try {
				String result = HttpClientSender.get(
						GlobalObject.HTTP_CLIENT_API, url);
				JSONObject json = new JSONObject(result);
				int code = json.optInt("code", -1);
				if (0 != code)
					return null;

				JSONObject data = json.optJSONObject("data");
				if (null == data)
					return null;

				User user = new User();
				user.setUserId(userId);
				user.setSessionId("");
				user.setNickName(data.optString("nickname", ""));
				user.setAvatar(data.optString("avatar", ""));
				user.setStatus(data.optInt("status", UserStatus.NORMAL));
				user.setType(data.optInt("type", UserType.NORMAL));
				user.setGender(data.optInt("sex", 1));
				return user;
			} catch (Exception e) {
				return null;
			}
		}
	}

}