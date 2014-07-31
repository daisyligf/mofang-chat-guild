package com.mofang.chat.guild.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.component.GameComponent;
import com.mofang.chat.guild.component.IndexComponent;
import com.mofang.chat.guild.entity.Game;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.global.common.GuildGroupType;
import com.mofang.chat.guild.model.Guild;
import com.mofang.chat.guild.model.GuildGame;
import com.mofang.chat.guild.model.GuildGroup;
import com.mofang.chat.guild.model.GuildGroupUser;
import com.mofang.chat.guild.mysql.GuildGameDao;
import com.mofang.chat.guild.mysql.GuildGroupDao;
import com.mofang.chat.guild.mysql.GuildGroupUserDao;
import com.mofang.chat.guild.mysql.impl.GuildGameDaoImpl;
import com.mofang.chat.guild.mysql.impl.GuildGroupDaoImpl;
import com.mofang.chat.guild.mysql.impl.GuildGroupUserDaoImpl;
import com.mofang.chat.guild.redis.GuildGameRedis;
import com.mofang.chat.guild.redis.GuildGroupRedis;
import com.mofang.chat.guild.redis.GuildGroupUserRedis;
import com.mofang.chat.guild.redis.GuildRedis;
import com.mofang.chat.guild.redis.ResultCacheRedis;
import com.mofang.chat.guild.redis.impl.GuildGameRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildGroupRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildGroupUserRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildRedisImpl;
import com.mofang.chat.guild.redis.impl.ResultCacheRedisImpl;
import com.mofang.chat.guild.service.GuildGameService;
import com.mofang.chat.guild.service.GuildService;
import com.mofang.framework.util.StringUtil;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildGameServiceImpl implements GuildGameService
{
	private final static GuildGameServiceImpl SERVICE = new GuildGameServiceImpl();
	private GuildRedis guildRedis = GuildRedisImpl.getInstance();
	private GuildGameRedis guildGameRedis = GuildGameRedisImpl.getInstance();
	private GuildGameDao guildGameDao = GuildGameDaoImpl.getInstance();
	private GuildGroupRedis guildGroupRedis = GuildGroupRedisImpl.getInstance();
	private GuildGroupDao guildGroupDao = GuildGroupDaoImpl.getInstance();
	private GuildGroupUserRedis guildGroupUserRedis = GuildGroupUserRedisImpl.getInstance();
	private GuildGroupUserDao guildGroupUserDao = GuildGroupUserDaoImpl.getInstance();
	private ResultCacheRedis resultCacheRedis = ResultCacheRedisImpl.getInstance();
	private GuildService guildService = GuildServiceImpl.getInstance();
	
	private GuildGameServiceImpl()
	{}
	
	public static GuildGameServiceImpl getInstance()
	{
		return SERVICE;
	}

	@Override
	public void edit(final long guildId, final String guildName, final long userId, final JSONArray addGames, final JSONArray delGames) throws Exception
	{
		try
		{
			///先执行删除关联游戏
			int delCount = delGames.length();
			if(delCount > 0)
			{
				int gameId = 0;
				for(int i=0; i<delCount; i++)
				{
					gameId = delGames.getInt(i);
					guildGameRedis.delete(guildId, gameId);
					guildGameDao.delete(guildId, gameId);
					
					///删除游戏对应的群组
					Set<String> groupIds = guildGroupRedis.getGuildGameGroupList(guildId, gameId);
					if(null != groupIds)
					{
						for(String groupId : groupIds)
						{
							///删除群组信息
							guildGroupRedis.delete(Long.parseLong(groupId));
							///删除游戏对应的群组成员
							guildGroupUserRedis.deleteByGroup(Long.parseLong(groupId));
						}
					}
					guildGroupRedis.deleteByGame(guildId, gameId);
				}
			}
			
			int addCount = addGames.length();
			if(addCount > 0)
			{
				int gameId = 0;
				long gameGroupId = 0L;
				GuildGame guildGame = null;
				GuildGroup guildGameGroup = null;
				for(int i=0; i<addCount; i++)
				{
					gameId = addGames.getInt(i);
					guildGame = new GuildGame();
					guildGame.setGuildId(guildId);
					guildGame.setGameId(gameId);
					guildGame.setCreateTime(new Date());
					guildGameRedis.add(guildGame);
					guildGameDao.add(guildGame);
					
					///创建公会游戏默认群组
					gameGroupId = guildGroupRedis.getMaxId();
					guildGameGroup = new GuildGroup();
					guildGameGroup.setGroupId(gameGroupId);
					guildGameGroup.setGuildId(guildId);
					guildGameGroup.setGameId(gameId);
					guildGameGroup.setType(GuildGroupType.GUILD_GAME);
					guildGameGroup.setCreatorId(userId);
					guildGameGroup.setGroupName("");
					guildGameGroup.setAvatar("");
					guildGameGroup.setCreateTime(new Date());
					///构建游戏群组名称
					Game gameInfo = GameComponent.getInfo(gameId);
					if(null != gameInfo)
					{
						String groupName = guildName + gameInfo.getGameName() + "聊天群";
						guildGameGroup.setGroupName(groupName);
					}
					guildGroupDao.add(guildGameGroup);
					guildGroupRedis.add(guildGameGroup);
					
					///将会长加入公会游戏默认群组
					GuildGroupUser gameGroupUser = new GuildGroupUser();
					gameGroupUser.setGroupId(gameGroupId);
					gameGroupUser.setUserId(userId);
					gameGroupUser.setReceiveNotify(1);
					gameGroupUser.setCreateTime(new Date());
					guildGroupUserDao.add(gameGroupUser);
					guildGroupUserRedis.add(gameGroupUser);
				}
			}
			
			///更新公会索引
			Guild guild = guildRedis.getInfo(guildId);
			if(null != guild)
			{
				List<Integer> gameIds = null;
				Set<String> ids = guildGameRedis.getGameListByGuild(guildId);
				if(null != ids && ids.size() > 0)
				{
					gameIds = new ArrayList<Integer>();
					for(String gameIdStr : ids)
						gameIds.add(Integer.parseInt(gameIdStr));
				}
				
				SolrInputDocument solrDoc = GuildServiceImpl.convertToSolrDoc(guild, gameIds);
				IndexComponent.add(solrDoc);
			}
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at GuildGameServiceImpl.edit throw an error.", e);
			throw e;
		}
	}

	@Override
	public JSONObject getGuildList(int gameId, int start, int end) throws Exception
	{
		///如果缓存里有，直接返回
		String cacheKey = RedisKey.CACHE_GAME_GUILD_LIST_KEY_PREFIX + gameId + "_" + start + "_" + end;
		String result = resultCacheRedis.getCache(cacheKey);
		if(!StringUtil.isNullOrEmpty(result))
		{
			JSONObject data = new JSONObject(result);
			return data;
		}
		
		///缓存没有，则需要重新构建列表信息，并将结果存入缓存中
		JSONObject data = new JSONObject();
		Set<String> guildIds = guildGameRedis.getGuildListByGame(gameId, start, end);
		if(null == guildIds)
			return data;
		
		long guildTotal = guildGameRedis.getGuildCountByGame(gameId);
		data.put("total", guildTotal);
		
		/*
		JSONArray guildJsonArray = new JSONArray();
		JSONObject guildJson = null;
		Guild guild = null;
		long guildId = 0L;
		for(String guildIdStr : guildIds)
		{
			guildId = Long.parseLong(guildIdStr);
			guild = guildRedis.getInfo(guildId);
			if(null == guild)
				continue;
			
			guildJson  = new JSONObject();
			guildJson.put("id", guild.getGuildId());
			guildJson.put("name", guild.getGuildName());
			guildJson.put("avatar", guild.getAvatar());
			guildJson.put("intro", guild.getIntro());
			guildJson.put("notice", guild.getNotice());
			guildJson.put("background", guild.getBackground());
			guildJson.put("prefix", guild.getGuildNamePrefix());
			guildJson.put("level", guild.getLevel());
			guildJson.put("upper_count", GlobalConfig.MAX_GUILD_MEMBER_COUNT);
			guildJson.put("member_count", guildUserRedis.getUserCount(guild.getGuildId()));
			int giftCount = GiftComponent.getGuildGiftCount(guildId);
			guildJson.put("gift_count", giftCount);
			///添加到公会列表中
			guildJsonArray.put(guildJson);
		}
		*/
		
		JSONArray guildJsonArray = new JSONArray();
		JSONObject guildJson = null;
		long guildId = 0L;
		for(String guildIdStr : guildIds)
		{
			guildId = Long.parseLong(guildIdStr);
			guildJson = guildService.getInfo(guildId);
			if(null == guildJson)
				continue;
			
			guildJson.remove("group");
			guildJson.remove("games");
			///添加到公会列表中
			guildJsonArray.put(guildJson);
		}
		data.put("guilds", guildJsonArray);
		///存入缓存中
		resultCacheRedis.saveCache(cacheKey, data.toString(), GlobalConfig.GAME_GUILD_LIST_EXPIRE);
		return data;
	}
}