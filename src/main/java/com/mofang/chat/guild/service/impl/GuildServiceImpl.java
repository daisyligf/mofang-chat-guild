package com.mofang.chat.guild.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.component.CheckInComponent;
import com.mofang.chat.guild.component.GameComponent;
import com.mofang.chat.guild.component.GiftComponent;
import com.mofang.chat.guild.component.IndexComponent;
import com.mofang.chat.guild.component.NotifyPushComponent;
import com.mofang.chat.guild.component.UserComponent;
import com.mofang.chat.guild.entity.Game;
import com.mofang.chat.guild.entity.User;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.RedisKey;
import com.mofang.chat.guild.global.common.GuildGroupType;
import com.mofang.chat.guild.global.common.GuildListType;
import com.mofang.chat.guild.global.common.GuildUpDownType;
import com.mofang.chat.guild.global.common.GuildUserRole;
import com.mofang.chat.guild.global.common.GuildUserStatus;
import com.mofang.chat.guild.model.Guild;
import com.mofang.chat.guild.model.GuildGame;
import com.mofang.chat.guild.model.GuildGroup;
import com.mofang.chat.guild.model.GuildGroupUser;
import com.mofang.chat.guild.model.GuildUser;
import com.mofang.chat.guild.mysql.GuildDao;
import com.mofang.chat.guild.mysql.GuildGameDao;
import com.mofang.chat.guild.mysql.GuildGroupDao;
import com.mofang.chat.guild.mysql.GuildGroupUserDao;
import com.mofang.chat.guild.mysql.GuildRecruitDao;
import com.mofang.chat.guild.mysql.GuildUserDao;
import com.mofang.chat.guild.mysql.impl.GuildDaoImpl;
import com.mofang.chat.guild.mysql.impl.GuildGameDaoImpl;
import com.mofang.chat.guild.mysql.impl.GuildGroupDaoImpl;
import com.mofang.chat.guild.mysql.impl.GuildGroupUserDaoImpl;
import com.mofang.chat.guild.mysql.impl.GuildRecruitDaoImpl;
import com.mofang.chat.guild.mysql.impl.GuildUserDaoImpl;
import com.mofang.chat.guild.redis.GuildGameRedis;
import com.mofang.chat.guild.redis.GuildGroupMessageRedis;
import com.mofang.chat.guild.redis.GuildGroupRedis;
import com.mofang.chat.guild.redis.GuildGroupUserRedis;
import com.mofang.chat.guild.redis.GuildRecruitRedis;
import com.mofang.chat.guild.redis.GuildRedis;
import com.mofang.chat.guild.redis.GuildUserRedis;
import com.mofang.chat.guild.redis.ResultCacheRedis;
import com.mofang.chat.guild.redis.impl.GuildGameRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildGroupMessageRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildGroupRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildGroupUserRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildRecruitRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildUserRedisImpl;
import com.mofang.chat.guild.redis.impl.ResultCacheRedisImpl;
import com.mofang.chat.guild.service.GuildService;
import com.mofang.framework.util.StringUtil;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildServiceImpl implements GuildService
{
	private final static GuildServiceImpl SERVICE = new GuildServiceImpl();
	private GuildRedis guildRedis = GuildRedisImpl.getInstance();
	private GuildDao guildDao = GuildDaoImpl.getInstance();
	private GuildGameDao guildGameDao = GuildGameDaoImpl.getInstance();
	private GuildGameRedis guildGameRedis = GuildGameRedisImpl.getInstance();
	private GuildUserDao guildUserDao = GuildUserDaoImpl.getInstance();
	private GuildUserRedis guildUserRedis = GuildUserRedisImpl.getInstance();
	private GuildGroupDao guildGroupDao = GuildGroupDaoImpl.getInstance();
	private GuildGroupRedis guildGroupRedis = GuildGroupRedisImpl.getInstance();
	private GuildGroupUserDao guildGroupUserDao = GuildGroupUserDaoImpl.getInstance();
	private GuildGroupUserRedis guildGroupUserRedis = GuildGroupUserRedisImpl.getInstance();
	private GuildRecruitRedis guildRecruitRedis = GuildRecruitRedisImpl.getInstance();
	private GuildRecruitDao guildRecruitDao = GuildRecruitDaoImpl.getInstance();
	private GuildGroupMessageRedis guildGroupMessageRedis = GuildGroupMessageRedisImpl.getInstance();
	private ResultCacheRedis resultCacheRedis = ResultCacheRedisImpl.getInstance();
	
	private GuildServiceImpl()
	{}
	
	public static GuildServiceImpl getInstance()
	{
		return SERVICE;
	}

	/**
	 * 异步创建公会
	 * 1. 保存公会信息
	 * 2. 将会长(创建人)添加到公会成员列表中
	 * 3. 创建公会默认群组
	 * 4. 将会长添加到公会默认群组成员列表中
	 * 5. 添加公会和游戏的对应关系
	 * 6. 创建公会游戏群组
	 * 7. 将会长添加到公会游戏群组成员列表中
	 */
	@Override
	public void create(Guild model, JSONArray games) throws Exception
	{
		try
		{
			///保存公会信息
			guildRedis.save(model);
			guildDao.add(model);
			
			///将会长添加到公会成员列表中
			GuildUser guildUser = new GuildUser();
			guildUser.setGuildId(model.getGuildId());
			guildUser.setUserId(model.getCreatorId());
			guildUser.setRole(GuildUserRole.CHAIRMAN);
			guildUser.setStatus(GuildUserStatus.NORMAL);
			Date curdate = new Date();
			guildUser.setApplyTime(curdate);
			guildUser.setAuditTime(curdate);
			guildUser.setLastLoginTime(curdate);
			guildUserDao.add(guildUser);
			guildUserRedis.add(guildUser);
			
			///创建公会默认群组
			long groupId = guildGroupRedis.getMaxId();
			GuildGroup guildGroup = new GuildGroup();
			guildGroup.setGroupId(groupId);
			guildGroup.setGuildId(model.getGuildId());
			guildGroup.setGameId(0);
			guildGroup.setType(GuildGroupType.GUILD);
			guildGroup.setCreatorId(model.getCreatorId());
			guildGroup.setGroupName(model.getGuildName() + "公会群");
			guildGroup.setAvatar("");
			guildGroup.setCreateTime(new Date());
			guildGroupDao.add(guildGroup);
			guildGroupRedis.add(guildGroup);
			
			///将会长加入公会默认群组成员列表
			GuildGroupUser guildGroupUser = new GuildGroupUser();
			guildGroupUser.setGroupId(groupId);
			guildGroupUser.setUserId(model.getCreatorId());
			guildGroupUser.setReceiveNotify(1);
			guildGroupUser.setCreateTime(new Date());
			guildGroupUserDao.add(guildGroupUser);
			guildGroupUserRedis.add(guildGroupUser);
			
			///添加公会关联游戏
			int gameId;
			GuildGame guildGame = null;
			long gameGroupId;
			GuildGroup guildGameGroup = null;
			List<Integer> gameIds = new ArrayList<Integer>();
			for(int i=0; i<games.length(); i++)
			{
				///保存公会和游戏的对应关系
				gameId = games.getInt(i);
				guildGame = new GuildGame();
				guildGame.setGuildId(model.getGuildId());
				guildGame.setGameId(gameId);
				guildGame.setCreateTime(new Date());
				guildGameDao.add(guildGame);
				guildGameRedis.add(guildGame);
				
				///创建公会游戏默认群组
				gameGroupId = guildGroupRedis.getMaxId();
				guildGameGroup = new GuildGroup();
				guildGameGroup.setGroupId(gameGroupId);
				guildGameGroup.setGuildId(model.getGuildId());
				guildGameGroup.setGameId(gameId);
				guildGameGroup.setType(GuildGroupType.GUILD_GAME);
				guildGameGroup.setCreatorId(model.getCreatorId());
				guildGameGroup.setGroupName("");
				guildGameGroup.setAvatar("");
				guildGameGroup.setCreateTime(new Date());
				///构建游戏群组名称
				Game gameInfo = GameComponent.getInfo(gameId);
				if(null != gameInfo)
				{
					String groupName = model.getGuildName() + gameInfo.getGameName() + "聊天群";
					guildGameGroup.setGroupName(groupName);
				}
				guildGroupDao.add(guildGameGroup);
				guildGroupRedis.add(guildGameGroup);
				
				///将会长加入公会游戏默认群组
				GuildGroupUser gameGroupUser = new GuildGroupUser();
				gameGroupUser.setGroupId(gameGroupId);
				gameGroupUser.setUserId(model.getCreatorId());
				gameGroupUser.setReceiveNotify(1);
				gameGroupUser.setCreateTime(new Date());
				guildGroupUserDao.add(gameGroupUser);
				guildGroupUserRedis.add(gameGroupUser);
				
				///将游戏ID添加到游戏ID列表中，以便于构建gamelist索引字段
				gameIds.add(gameId);
			}
			
			///创建公会索引
			SolrInputDocument solrDoc = convertToSolrDoc(model, gameIds);
			IndexComponent.add(solrDoc);
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at GuildServiceImpl.create throw an error.", e);
			throw e;
		}
	}

	@Override
	public void edit(Guild model) throws Exception
	{
		try
		{
			///编辑公会
			guildRedis.update(model);
			guildDao.update(model);
			
			///更新公会索引
			List<Integer> gameIds = null;
			Set<String> ids = guildGameRedis.getGameListByGuild(model.getGuildId());
			if(null != ids && ids.size() > 0)
			{
				gameIds = new ArrayList<Integer>();
				for(String gameIdStr : ids)
					gameIds.add(Integer.parseInt(gameIdStr));
			}
			SolrInputDocument solrDoc = convertToSolrDoc(model, gameIds);
			IndexComponent.add(solrDoc);
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at GuildServiceImpl.edit throw an error.", e);
			throw e;
		}
	}
	
	/**
	 * 异步删除公会(解散/删除)
	 * 1. 删除公会信息(redis删除 mysql更新状态)
	 * 2. 将公会从最新和最热的公会列表中删除
	 * 3. 将公会从会员的“我的公会”中删除
	 * 4. 将公会的成员列表删除
	 * 5. 将公会的默认群组删除
	 * 6. 将公会的默认群组成员列表删除
	 * 7. 将公会和游戏的对应关系删除
	 * 8. 将公会游戏群组删除
	 * 9. 将公会游戏群组的成员列表删除
	 * 10. 将公会从招募列表中删除
	 */
	@Override
	public void delete(Guild guild, int status, int event) throws Exception
	{
		try
		{
			long guildId = guild.getGuildId();
			
			///获取公会成员列表，便于发送通知
			Set<String> userIds = guildUserRedis.getUserList(guildId);
			List<GuildUser> userList = null;
			if(null != userIds && userIds.size() > 0)
			{
				userList = new ArrayList<GuildUser>();
				GuildUser guildUser = null;
				for(String strUserId : userIds)
				{
					guildUser = guildUserRedis.getInfo(guildId, Long.parseLong(strUserId));
					if(null != guildUser)
						userList.add(guildUser);
				}
			}
			
			///删除公会信息
			guildRedis.delete(guildId);
			guildDao.updateStatus(guildId, status);
			
			///删除公会成员列表
			guildUserRedis.deleteByGuild(guildId);
			guildUserDao.deleteByGuild(guildId);
			
			///删除公会的默认群组
			Set<String> groupIds = guildGroupRedis.getGuildGroupList(guildId);
			if(null != groupIds)
			{
				for(String groupId : groupIds)
				{
					///删除群组成员
					guildGroupUserRedis.deleteByGroup(Long.parseLong(groupId));
					guildGroupUserDao.deleteByGroup(Long.parseLong(groupId));
					///删除群组
					guildGroupRedis.delete(Long.parseLong(groupId));
					guildGroupDao.delete(Long.parseLong(groupId));
				}
			}
			///删除默认群组列表
			guildGroupRedis.deleteByGuild(guildId);
			guildGroupDao.deleteByGuildId(guildId);
			
			///删除公会游戏的群组
			Set<String> gameIds = guildGameRedis.getGameListByGuild(guildId);
			if(null != gameIds)
			{
				for(String gameId : gameIds)
				{
					Set<String> gameGroupIds = guildGroupRedis.getGuildGameGroupList(guildId, Integer.parseInt(gameId));
					if(null != gameGroupIds)
					{
						for(String groupId : gameGroupIds)
						{
							///删除群组成员
							guildGroupUserRedis.deleteByGroup(Long.parseLong(groupId));
							guildGroupUserDao.deleteByGroup(Long.parseLong(groupId));
							///删除群组
							guildGroupRedis.delete(Long.parseLong(groupId));
							guildGroupDao.delete(Long.parseLong(groupId));
						}
					}
					///删除指定游戏群组列表
					guildGroupRedis.deleteByGame(guildId, Integer.parseInt(gameId));
					///删除公会和游戏的对应关系
					guildGameRedis.delete(guildId, Integer.parseInt(gameId));
					guildGameDao.deleteByGuild(guildId);
				}
			}
			
			///删除公会招募信息
			guildRecruitRedis.deleteByGuild(guildId);
			guildRecruitDao.deleteByGuild(guildId);
			
			///删除公会索引
			IndexComponent.deleteById(String.valueOf(guildId));
			
			///发起公会解散通知
			if(event == 1)   ///会长主动解散
				NotifyPushComponent.dismissGuildByChairman(userList, guild);
			else if(event == 2)  ///系统解散
				NotifyPushComponent.dismissGuildBySystem(userList, guild);
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at GuildServiceImpl.delete throw an error.", e);
			throw e;
		}
	}
	
	@Override
	public void upDown(Guild model, int optType, int listType) throws Exception
	{
		try
		{
			long sequence = 0L;
			if(optType == GuildUpDownType.UP)
				sequence = System.currentTimeMillis() * 2;
			else if(optType == GuildUpDownType.DOWN)
				sequence = 0L;
			
			if(listType == GuildListType.HOT)
			{
				model.setHotSeq(sequence);
				guildRedis.updateHotSequence(model.getGuildId(), sequence, model.getHot());
			}
			else if(listType == GuildListType.NEW)
			{
				model.setNewSeq(sequence);
				guildRedis.updateNewSequence(model.getGuildId(), sequence, model.getCreateTime().getTime());
			}
			
			///保存置顶信息
			guildRedis.update(model);
			guildDao.update(model);
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at GuildServiceImpl.upDown throw an error.", e);
			throw e;
		}
	}
	
	/**
	 * 获取公会信息
	 * 先从缓存里获取，如果缓存中存在，则直接返回，否则需要重新构建公会信息，再将结果添加到缓存中
	 */
	@Override
	public JSONObject getInfo(long guildId) throws Exception
	{
		///缓存里有，直接返回
		String cacheKey = RedisKey.CACHE_GUILD_INFO_KEY_PREFIX + guildId;
		String result = resultCacheRedis.getCache(cacheKey);
		if(!StringUtil.isNullOrEmpty(result))
		{
			JSONObject data = new JSONObject(result);
			return data;
		}
		
		///缓存没有，则需要重新构建列表信息，并将结果存入缓存中
		Guild model = guildRedis.getInfo(guildId);
		if(null == model)
			return null;
		
		JSONObject data  = new JSONObject();
		data.put("id", model.getGuildId());
		data.put("name", model.getGuildName());
		data.put("avatar", model.getAvatar());
		data.put("intro", model.getIntro());
		data.put("notice", model.getNotice());
		data.put("background", model.getBackground());
		data.put("prefix", model.getGuildNamePrefix());
		data.put("level", model.getLevel());
		data.put("upper_count", GlobalConfig.MAX_GUILD_MEMBER_COUNT);
		long memberCount = guildUserRedis.getUserCount(guildId);
		data.put("member_count", memberCount);
		int giftCount = GiftComponent.getGuildGiftCount(guildId);
		int markCount = CheckInComponent.getGuildCheckinNum(guildId);
		data.put("gift_count", giftCount);
		data.put("mark_count", markCount);
		data.put("role", 0);
		
		///获取会长信息
		JSONObject chairman = new JSONObject();
		chairman.put("uid", model.getCreatorId());
		User user = UserComponent.getInfo(model.getCreatorId());
		if(null != user)
		{
			chairman.put("nickname", user.getNickName());
			chairman.put("avatar", user.getAvatar());
			chairman.put("sex", user.getGender());
		}
		data.put("chairman", chairman);
		
		///获取公会默认群组
		JSONObject groupJson = new JSONObject();
		Set<String> groupIds = guildGroupRedis.getGuildGroupList(guildId);
		GuildGroup guildGroupInfo = null;
		if(null != groupIds && groupIds.size() > 0)
		{
			Iterator<String> iterator = groupIds.iterator();
			long groupId = Long.parseLong(iterator.next());
			groupJson.put("id", groupId);
			///获取群组名称
			guildGroupInfo = guildGroupRedis.getInfo(groupId);
			if(null != guildGroupInfo)
				groupJson.put("name", guildGroupInfo.getGroupName());
		}
		data.put("group", groupJson);
		
		///获取公会关联游戏列表
		JSONArray gameJsonArray = new JSONArray();
		Set<String> gameIds = guildGameRedis.getGameListByGuild(guildId);
		if(null != gameIds && gameIds.size() > 0)
		{
			int gameId = 0;
			Game game = null;
			JSONObject gameJson = null;
			for(String gameIdStr : gameIds)
			{
				gameId = Integer.parseInt(gameIdStr);
				gameJson = new JSONObject();
				gameJson.put("id", gameId);
				
				///获取游戏基本信息
				game = GameComponent.getInfo(gameId);
				if(null != game)
				{
					gameJson.put("name", game.getGameName());
					gameJson.put("icon", game.getIcon());
				}
				
				///获取游戏礼包总数
				int gameGiftCount = GiftComponent.getGuildGameGiftCount(guildId, gameId);
				gameJson.put("gift_count", gameGiftCount);
				
				///获取游戏群组列表
				Set<String> gameGroupIds = guildGroupRedis.getGuildGameGroupList(guildId, gameId);
				int gameMemberCount = 0;
				JSONArray groupJsonArray = new JSONArray();
				if(null != gameGroupIds && gameGroupIds.size() > 0)
				{
					long groupId = 0;
					JSONObject gameGroupJson = null;
					for(String groupIdStr : gameGroupIds)
					{
						gameGroupJson = new JSONObject();
						groupId = Long.parseLong(groupIdStr);
						gameMemberCount += guildGroupUserRedis.getUserCount(groupId);
						gameGroupJson.put("id", groupId);
						///获取群组名称
						guildGroupInfo = guildGroupRedis.getInfo(groupId);
						if(null != guildGroupInfo)
							gameGroupJson.put("name", guildGroupInfo.getGroupName());
						
						groupJsonArray.put(gameGroupJson);
					}
				}
				gameJson.put("member_count", gameMemberCount);
				gameJson.put("groups", groupJsonArray);
				gameJsonArray.put(gameJson);
			}
		}
		data.put("games", gameJsonArray);
		
		///存入缓存中
		resultCacheRedis.saveCache(cacheKey, data.toString(), GlobalConfig.GUILD_INFO_EXPIRE);
		return data;
	}

	/**
	 * 根据type获取公会列表
	 * 先从缓存里获取，如果缓存中存在，则直接返回，否则需要重新构建公会信息，再将结果添加到缓存中
	 */
	@Override
	public JSONObject getGuildList(int type, int start, int end) throws Exception
	{
		///缓存里有，直接返回
		String cacheKey = RedisKey.CACHE_GUILD_LIST_KEY_PREFIX + type + "_" + start + "_" + end;
		String result = resultCacheRedis.getCache(cacheKey);
		if(!StringUtil.isNullOrEmpty(result))
		{
			JSONObject data = new JSONObject(result);
			return data;
		}
		
		///缓存没有，则需要重新构建列表信息，并将结果存入缓存中
		Set<String> guildIds = null;
		long guildCount = 0L;
		if(type == GuildListType.HOT)
		{
			guildIds = guildRedis.getHotList(start, end);
			guildCount = guildRedis.getHotCount();
		}
		else if(type == GuildListType.NEW)
		{
			guildIds = guildRedis.getNewList(start, end);
			guildCount = guildRedis.getNewCount();
		}
		
		JSONObject data = new JSONObject();
		if(null == guildIds)
			return data;
		
		data.put("total", guildCount);
		JSONArray guildJsonArray = new JSONArray();
		JSONObject guildJson = null;
		for(String guildId : guildIds)
		{
			guildJson = getInfo(Long.parseLong(guildId));
			if(null == guildJson)
				continue;

			guildJson.remove("group");
			guildJson.remove("games");
			
			///添加到公会列表中
			guildJsonArray.put(guildJson);
		}
		data.put("guilds", guildJsonArray);
		///存入缓存中
		resultCacheRedis.saveCache(cacheKey, data.toString(), GlobalConfig.GUILD_LIST_EXPIRE);
		return data;
	}

	@Override
	public JSONArray getMyGuildList(long userId) throws Exception
	{
		JSONArray data = new JSONArray();
		Set<String> guildIds = guildRedis.getMyList(userId);
		if(null != guildIds)
		{
			JSONObject guildJson = null;
			GuildUser guildUser = null;
			long guildId = 0L;
			for(String guildIdStr : guildIds)
			{
				guildId = Long.parseLong(guildIdStr);
				guildJson = getInfo(guildId);
				if(null == guildJson)
					continue;
				
				//获取公会默认群组的未读数
				JSONObject groupJson = guildJson.getJSONObject("group");
				if(null != groupJson)
				{
					long groupId = groupJson.optLong("id", 0L);
					if(groupId > 0)
					{
						int unreadCount = guildGroupMessageRedis.getUnreadCount(userId, groupId);
						groupJson.put("unread_count", unreadCount);
					}
				}
				
				///获取当前用户在公会的角色
				int role =0;
				guildUser = guildUserRedis.getInfo(guildId, userId);
				if(null != guildUser)
					role = guildUser.getRole();
				guildJson.put("role", role);
				
				///获取公会关联游戏列表
				JSONArray gameJsonArray = guildJson.getJSONArray("games");
				if(null != gameJsonArray)
				{
					JSONObject gameJson = null;
					JSONArray gameGroupJsonArray = null;
					for(int i=0; i<gameJsonArray.length(); i++)
					{
						gameJson = gameJsonArray.getJSONObject(i);
						gameGroupJsonArray = gameJson.getJSONArray("groups");
						JSONObject gameGroupJson = null;
						if(null != gameGroupJsonArray)
						{
							for(int j=0; j<gameGroupJsonArray.length(); j++)
							{
								gameGroupJson = gameGroupJsonArray.getJSONObject(j);
								long groupId = gameGroupJson.getLong("id");
								///判断当前用户是否已在游戏群组中
								boolean exists = guildGroupUserRedis.exists(groupId, userId);
								gameGroupJson.put("exists", exists);
								///获取游戏群组未读数
								int gameUnreadCount = guildGroupMessageRedis.getUnreadCount(userId, groupId);
								gameGroupJson.put("unread_count", gameUnreadCount);
							}
						}
					}
				}
				data.put(guildJson);
				
				///更新用户最后登录时间
				Date lastLoginTime = new Date();
				guildUserRedis.updateLastLoginTime(guildId, userId, lastLoginTime);
				guildUserDao.updateLastLoginTime(guildId, userId, lastLoginTime);
			}
		}
		return data;
	}

	@Override
	public JSONObject getStatData(long guildId) throws Exception
	{
		long rank = guildRedis.getRank(guildId) + 1; 
		long memberCount = guildUserRedis.getUserCount(guildId);
		int markCount = CheckInComponent.getGuildCheckinNum(guildId);
		int newMemberCount = guildUserRedis.getNewMemberCount(guildId);
		int unloginMemberCount7Days = guildUserRedis.getUnloginMemberCount7Days(guildId);
		int unloginMemberCount30Days = guildUserRedis.getUnloginMemberCount30Days(guildId);
		
		JSONObject data = new JSONObject();
		data.put("rank", rank);
		data.put("member_count", memberCount);
		data.put("new_member_count", newMemberCount);
		data.put("mark_count", markCount);
		data.put("unlogin_member_count_7days", unloginMemberCount7Days);
		data.put("unlogin_member_count_30days", unloginMemberCount30Days);
		return data;
	}
	
	public static SolrInputDocument convertToSolrDoc(Guild model, List<Integer> gameIds) throws Exception
	{
		String gameList = "";
		StringBuilder games = new StringBuilder();
		if(null != gameIds && gameIds.size() > 0)
		{
			Game game = null;
			for(Integer gameId : gameIds)
			{
				game = GameComponent.getInfo(gameId);
				if(null != game)
					games.append(game.getGameName() + ",");
			}
		}
		if(games.length() > 0)
			gameList = games.substring(0, games.length() - 1);
		
		SolrInputDocument appBean = new SolrInputDocument();
		appBean.addField("id", model.getGuildId());
		appBean.addField("guild_name", model.getGuildName());
		appBean.addField("level", model.getLevel());
		appBean.addField("avatar", model.getAvatar());
		appBean.addField("intro", model.getIntro());
		appBean.addField("notice", model.getNotice());
		appBean.addField("background", model.getBackground());
		appBean.addField("game_list", gameList);
		return appBean;
	}
}