package com.mofang.chat.guild.logic.impl;

import java.util.Date;
import java.util.Iterator;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.component.GiftComponent;
import com.mofang.chat.guild.component.SearchComponent;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.ResultValue;
import com.mofang.chat.guild.global.ReturnCode;
import com.mofang.chat.guild.global.common.GuildInformStatus;
import com.mofang.chat.guild.global.common.GuildStatus;
import com.mofang.chat.guild.logic.GuildLogic;
import com.mofang.chat.guild.model.Guild;
import com.mofang.chat.guild.model.GuildInform;
import com.mofang.chat.guild.model.GuildUser;
import com.mofang.chat.guild.mysql.GuildDao;
import com.mofang.chat.guild.mysql.GuildInformDao;
import com.mofang.chat.guild.mysql.impl.GuildDaoImpl;
import com.mofang.chat.guild.mysql.impl.GuildInformDaoImpl;
import com.mofang.chat.guild.redis.GuildRedis;
import com.mofang.chat.guild.redis.GuildUserRedis;
import com.mofang.chat.guild.redis.impl.GuildRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildUserRedisImpl;
import com.mofang.chat.guild.service.GuildService;
import com.mofang.chat.guild.service.impl.GuildServiceImpl;
import com.mofang.framework.util.StringUtil;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
public class GuildLogicImpl implements GuildLogic
{
	private final static GuildLogicImpl LOGIC = new GuildLogicImpl();
	private GuildRedis guildRedis = GuildRedisImpl.getInstance();
	private GuildDao guildDao = GuildDaoImpl.getInstance();
	private GuildUserRedis guildUserRedis = GuildUserRedisImpl.getInstance();
	private GuildInformDao guildInformDao = GuildInformDaoImpl.getInstance();
	private GuildService guildService = GuildServiceImpl.getInstance();
	
	private GuildLogicImpl()
	{}
	
	public static GuildLogicImpl getInstance()
	{
		return LOGIC;
	}

	@Override
	public ResultValue create(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		String uidString = context.getParameters("uid");
		if(!StringUtil.isLong(uidString))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage("参数无效");
			return result;
		}
		
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			return result;
		}
		
		try
		{
			long userId = Long.parseLong(uidString);
			long createCount = guildDao.getCreateCount(userId);
			if(createCount >= GlobalConfig.MAX_CREATE_GUILD_COUNT)
			{
				result.setCode(ReturnCode.USER_CAN_NOT_CREATE_GUILD);
				result.setMessage("您的公会已到达创建上限");
				return result;
			}
			
			JSONObject json = new JSONObject(postData);
			String guildName = json.optString("name", "");
			if(StringUtil.isNullOrEmpty(guildName))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("请输入公会名称");
				return result;
			}
			String avatar = json.optString("avatar", "");
			if(StringUtil.isNullOrEmpty(avatar))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("请上传公会头像");
				return result;
			}
			String guildNamePrefix = json.optString("prefix", "");
			String intro = json.optString("intro", "");
			String notice = json.optString("notice", "");
			String background = json.optString("background", "");
			JSONArray games = json.getJSONArray("game_ids");
			if(null != games && games.length() > GlobalConfig.MAX_GUILD_GAME_REF_COUNT)
			{
				result.setCode(ReturnCode.OVER_GUILD_GAME_MAX_COUNT);
				result.setMessage("超过公会关联游戏最大数");
				return result;
			}
			
			///构建公会信息
			long guildId = guildRedis.getMaxId();
			Guild model = new Guild();
			model.setGuildId(guildId);
			model.setGuildName(guildName);
			model.setAvatar(avatar);
			model.setGuildNamePrefix(guildNamePrefix);
			model.setIntro(intro);
			model.setNotice(notice);
			model.setBackground(background);
			model.setLevel(1);
			model.setCreatorId(userId);
			model.setStatus(GuildStatus.NORMAL);
			model.setHot(0.0);
			model.setHotSeq(0L);
			model.setNewSeq(0L);
			model.setCreateTime(new Date());
			
			///保存公会
			guildService.create(model, games);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			JSONObject data = new JSONObject();
			data.put("guild_id", guildId);
			result.setData(data);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildLogicImpl.create throw an error.", e);
		}
	}

	@Override
	public ResultValue edit(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		String uidString = context.getParameters("uid");
		if(!StringUtil.isLong(uidString))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage("参数无效");
			return result;
		}
		
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage("参数无效");
			return result;
		}
		
		try
		{
			long userId = Long.parseLong(uidString);
			JSONObject json = new JSONObject(postData);
			long guildId = json.optLong("guild_id", 0L);
			if(0 == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("参数无效");
				return result;
			}
			
			Guild model = guildRedis.getInfo(guildId);
			if(null == model)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage("公会不存在");
				return result;
			}
			
			if(model.getCreatorId() != userId)
			{
				result.setCode(ReturnCode.NO_PRIVILEGE_TO_OPERATE);
				result.setMessage("您无权修改公会信息");
				return result;
			}
			
			String avatar = json.optString("avatar", "");
			if(StringUtil.isNullOrEmpty(avatar))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("请上传公会头像");
				return result;
			}
			String guildNamePrefix = json.optString("prefix", "");
			String intro = json.optString("intro", "");
			String notice = json.optString("notice", "");
			String background = json.optString("background", "");
			
			model.setAvatar(avatar);
			model.setGuildNamePrefix(guildNamePrefix);
			model.setIntro(intro);
			model.setNotice(notice);
			model.setBackground(background);
			
			///编辑公会
			guildService.edit(model);
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildLogicImpl.edit throw an error.", e);
		}
	}

	@Override
	public ResultValue editByAdmin(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage("参数无效");
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			long guildId = json.optLong("guild_id", 0L);
			if(0 == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("参数无效");
				return result;
			}
			
			Guild model = guildRedis.getInfo(guildId);
			if(null == model)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage("公会不存在");
				return result;
			}
			
			String avatar = json.optString("avatar", "");
			if(StringUtil.isNullOrEmpty(avatar))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("请上传公会头像");
				return result;
			}
			String guildNamePrefix = json.optString("prefix", "");
			String intro = json.optString("intro", "");
			String notice = json.optString("notice", "");
			String background = json.optString("background", "");
			
			model.setAvatar(avatar);
			model.setGuildNamePrefix(guildNamePrefix);
			model.setIntro(intro);
			model.setNotice(notice);
			model.setBackground(background);
			
			///编辑公会
			guildService.edit(model);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildLogicImpl.editByAdmin throw an error.", e);
		}
	}
	
	/**
	 * 解散公会
	 * 1. 删除公会信息(redis删除，mysql修改状态)
	 * 2. 
	 */
	@Override
	public ResultValue dismiss(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		String uidString = context.getParameters("uid");
		if(!StringUtil.isLong(uidString))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage("参数无效");
			return result;
		}
		
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage("参数无效");
			return result;
		}
		
		try
		{
			long userId = Long.parseLong(uidString);
			JSONObject json = new JSONObject(postData);
			long guildId = json.optLong("guild_id", 0L);
			if(0 == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("参数无效");
				return result;
			}
			
			Guild model = guildRedis.getInfo(guildId);
			if(null == model)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage("公会不存在");
				return result;
			}
			
			if(model.getCreatorId() != userId)
			{
				result.setCode(ReturnCode.NO_PRIVILEGE_TO_OPERATE);
				result.setMessage("您无权解散公会");
				return result;
			}
			
			///删除公会
			guildService.delete(model, GuildStatus.DISMISS, 1);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildLogicImpl.dismiss throw an error.", e);
		}
	}

	@Override
	public ResultValue updown(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage("参数无效");
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			long guildId = json.optLong("guild_id", 0L);
			int optType = json.optInt("opt_type", 0);
			int listType = json.optInt("list_type", 0);
			if(0 == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("公会ID无效");
				return result;
			}
			if(0 == optType)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("操作类型无效");
				return result;
			}
			if(0 == listType)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("列表类型无效");
				return result;
			}
			
			Guild model = guildRedis.getInfo(guildId);
			if(null == model)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage("公会不存在");
				return result;
			}
			
			///置顶/取消置顶公会
			guildService.upDown(model, optType, listType);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildLogicImpl.closeDown throw an error.", e);
		}
	}
	
	@Override
	public ResultValue delete(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage("参数无效");
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			long guildId = json.optLong("guild_id", 0L);
			if(0 == guildId)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("参数无效");
				return result;
			}
			
			Guild model = guildRedis.getInfo(guildId);
			if(null == model)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage("公会不存在");
				return result;
			}
			
			///删除公会
			guildService.delete(model, GuildStatus.DELETE, 2);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildLogicImpl.delete throw an error.", e);
		}
	}

	@Override
	public ResultValue inform(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		String uidString = context.getParameters("uid");
		if(!StringUtil.isLong(uidString))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage("参数无效");
			return result;
		}
		
		String postData = context.getPostData();
		if(StringUtil.isNullOrEmpty(postData))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage("参数无效");
			return result;
		}
		
		try
		{
			JSONObject json = new JSONObject(postData);
			long guildId = json.optLong("guild_id", 0L);
			int informType = json.optInt("inform_type", 0);
			String reason = json.optString("reason", "");
			if(0 == guildId || 0 == informType)
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("参数无效");
				return result;
			}
			
			///构建举报信息
			GuildInform model = new GuildInform();
			model.setGuildId(guildId);
			model.setUserId(Long.parseLong(uidString));
			model.setInformType(informType);
			model.setReason(reason);
			model.setStatus(GuildInformStatus.UNHANDLED);
			model.setCreateTime(new Date());
			guildInformDao.add(model);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildLogicImpl.inform throw an error.", e);
		}
	}

	@Override
	public ResultValue getInfo(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		try
		{
			String strGuildId = context.getParameters("guild_id");
			if(!StringUtil.isLong(strGuildId) || "0".equals(strGuildId))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("公会ID无效");
				return result;
			}
			
			long guildId = Long.parseLong(strGuildId);
			JSONObject data = guildService.getInfo(guildId);
			if(null == data)
			{
				result.setCode(ReturnCode.GUILD_NOT_EXISTS);
				result.setMessage("公会不存在");
				return result;
			}
			
			///如果参数中包含了原子封装，则需要添加role字段，来表示当前用户在公会的角色，0代表不在当前公会
			int role = 0;
			String uidString = context.getParameters("uid");
			if(StringUtil.isLong(uidString))
			{
				///判断用户是否存在公会成员中
				boolean exists = guildUserRedis.exists(guildId, Long.parseLong(uidString));
				if(exists)
				{
					///获取用户在公会的角色
					GuildUser guildUser = guildUserRedis.getInfo(guildId, Long.parseLong(uidString));
					if(null != guildUser)
						role = guildUser.getRole();
				}
			}
			data.put("role", role);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			result.setData(data);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildLogicImpl.getInfo throw an error.", e);
		}
	}

	@Override
	public ResultValue getList(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		try
		{
			String strType = context.getParameters("type");
			String strStart = context.getParameters("start");
			String strSize = context.getParameters("size");
			if(!StringUtil.isInteger(strType) || "0".equals(strType))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("列表类型无效");
				return result;
			}
			
			int type = Integer.parseInt(strType);
			int start = 0;
			int size = 50;
			if(StringUtil.isInteger(strStart))
				start = Integer.parseInt(strStart);
			if(StringUtil.isInteger(strSize))
				size = Integer.parseInt(strSize);
			
			int end = start + size - 1;
			
			///获取公会列表数据
			JSONObject data = guildService.getGuildList(type, start, end);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			result.setData(data);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildLogicImpl.getList throw an error.", e);
		}
	}

	@Override
	public ResultValue getMyList(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		String uidString = context.getParameters("uid");
		if(!StringUtil.isLong(uidString))
		{
			result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
			result.setMessage("参数无效");
			return result;
		}
		
		try
		{
			///获取“我的公会”列表
			long userId = Long.parseLong(uidString);
			JSONArray data = guildService.getMyGuildList(userId);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			result.setData(data);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildLogicImpl.getMyList throw an error.", e);
		}
	}
	
	@Override
	public ResultValue getListCount(HttpRequestContext context) throws Exception
	{
	    ResultValue result = new ResultValue();
	    String uidString = context.getParameters("uid");
	    if(!StringUtil.isLong(uidString))
	    {
		result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
		result.setMessage("参数无效");
		return result;
	    }
	    
	    try
	    {
		// 获取公会列表数
		long userId = Long.parseLong(uidString);
		JSONObject data = guildService.getGuildListCount(userId);
		
		// 返回结果
		result.setCode(ReturnCode.SUCCESS);
		result.setMessage("OK");
		result.setData(data);
		return result;
		
	    }
	    catch(Exception e)
	    {
		throw new Exception("at GuildLogicImpl.getListCount throw an error.", e);
	    }
	}

	@Override
	public ResultValue search(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		try
		{
			String keyword = context.getParameters("keyword");
			String strStart = context.getParameters("start");
			String strSize = context.getParameters("size");
			if(StringUtil.isNullOrEmpty(keyword))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("关键词不能为空");
				return result;
			}
			
			int start = 0;
			int size = 50;
			if(StringUtil.isInteger(strStart))
				start = Integer.parseInt(strStart);
			if(StringUtil.isInteger(strSize))
				size = Integer.parseInt(strSize);
			
			JSONObject data = new JSONObject();
			JSONArray guildJsonArray = new JSONArray();
			JSONObject guildJson = null;
			
			///如果是数值型，则先按公会ID查找
			if(StringUtil.isLong(keyword))
			{
				Guild guild = guildRedis.getInfo(Long.parseLong(keyword));
				if(null != guild)
				{
					guildJson = new JSONObject();
					guildJson.put("id", guild.getGuildId());
					guildJson.put("name", guild.getGuildName());
					guildJson.put("avatar", guild.getAvatar());
					guildJson.put("intro", guild.getIntro());
					guildJson.put("notice", guild.getNotice());
					guildJson.put("background", guild.getBackground());
					guildJson.put("level", guild.getLevel());
					guildJson.put("upper_count", GlobalConfig.MAX_GUILD_MEMBER_COUNT);
					guildJson.put("member_count", guildUserRedis.getUserCount(guild.getGuildId()));
					int giftCount = GiftComponent.getGuildGiftCount(guild.getGuildId());
					guildJson.put("gift_count", giftCount);
					guildJsonArray.put(guildJson);
					data.put("total", 1);
					data.put("guilds", guildJsonArray);
					
					///返回结果
					result.setCode(ReturnCode.SUCCESS);
					result.setMessage("OK");
					result.setData(data);
					return result;
				}
			}
			
			///从solr中查询
			QueryResponse  response = SearchComponent.defaultSearch(keyword, start, size);
			SolrDocumentList docList = response.getResults();
			for(Iterator<SolrDocument> iterator = docList.iterator(); iterator.hasNext();)
			{
				SolrDocument document = iterator.next();
				guildJson = convertDocToJson(document);
				if(null != guildJson)
					guildJsonArray.put(guildJson);
			}
			long total = docList.getNumFound();
			data.put("total", total);
			data.put("guilds", guildJsonArray);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			result.setData(data);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildLogicImpl.getList throw an error.", e);
		}
	}

	@Override
	public ResultValue getStatData(HttpRequestContext context) throws Exception
	{
		ResultValue result = new ResultValue();
		try
		{
			String strGuildId = context.getParameters("guild_id");
			if(!StringUtil.isLong(strGuildId) || strGuildId.equals("0"))
			{
				result.setCode(ReturnCode.CLIENT_REQUEST_DATA_IS_INVALID);
				result.setMessage("公会ID无效");
				return result;
			}
			
			///获取公会数据
			long guildId = Long.parseLong(strGuildId);
			JSONObject data = guildService.getStatData(guildId);
			
			///返回结果
			result.setCode(ReturnCode.SUCCESS);
			result.setMessage("OK");
			result.setData(data);
			return result;
		}
		catch(Exception e)
		{
			throw new Exception("at GuildLogicImpl.getList throw an error.", e);
		}
	}
	
	private JSONObject convertDocToJson(SolrDocument doc)
	{
		JSONObject json = new JSONObject(); 
		long guildId = Long.parseLong((doc.getFieldValue("id").toString()));
		String guildName = String.valueOf(doc.getFieldValue("guild_name"));
		String level = String.valueOf(doc.getFieldValue("level"));
		
		String avatar = "";
		Object objAvatar = doc.getFieldValue("avatar");
		if(null != objAvatar)
			avatar = objAvatar.toString();
		
		String intro = "";
		Object objInfo = doc.getFieldValue("intro");
		if(null != objInfo)
			intro = objInfo.toString();
		
		String notice = "";
		Object objNotice = doc.getFieldValue("notice");
		if(null != objNotice)
			notice = objNotice.toString();
		
		String background = "";
		Object objBackground = doc.getFieldValue("background");
		if(null != objBackground)
			background = objBackground.toString();
		
		try
		{
			json.put("id", guildId);
			json.put("name", guildName);
			json.put("avatar", avatar);
			json.put("intro", intro);
			json.put("notice", notice);
			json.put("background", background);
			json.put("level", Integer.parseInt(level));
			json.put("upper_count", GlobalConfig.MAX_GUILD_MEMBER_COUNT);
			json.put("member_count", guildUserRedis.getUserCount(guildId));
			
			///获取礼包总数
			int giftCount = GiftComponent.getGuildGiftCount(guildId);
			json.put("gift_count", giftCount);
			return json;
		}
		catch(Exception e)
		{
			return null;
		}
	}
}