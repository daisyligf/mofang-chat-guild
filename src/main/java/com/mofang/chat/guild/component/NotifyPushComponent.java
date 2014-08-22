package com.mofang.chat.guild.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mofang.chat.guild.entity.User;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.chat.guild.global.common.GuildUserRole;
import com.mofang.chat.guild.model.Guild;
import com.mofang.chat.guild.model.GuildUser;
import com.mofang.chat.guild.redis.GuildRedis;
import com.mofang.chat.guild.redis.GuildUserRedis;
import com.mofang.chat.guild.redis.impl.GuildRedisImpl;
import com.mofang.chat.guild.redis.impl.GuildUserRedisImpl;
import com.mofang.framework.net.http.HttpClientSender;

/**
 * 
 * @author zhaodx
 *
 */
public class NotifyPushComponent
{
	private final static int PUSH_THREADS = Runtime.getRuntime().availableProcessors() + 1;
	private final static ExecutorService PUSH_EXECUTOR = Executors.newFixedThreadPool(PUSH_THREADS);
	
	/**
	 * 发送会长解散公会通知
	 * 所有公会成员需要接收通知
	 * @throws Exception
	 */
	public static void dismissGuildByChairman(final List<GuildUser> userList, final Guild guild) throws Exception
	{
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final String msgCategory = "dismiss_guild";
					long creatorId = guild.getCreatorId();
					User chairman = UserComponent.getInfo(creatorId);
					String chairmanName = "";
					if(null != chairman)
						chairmanName = chairman.getNickName();
					String title = "公会被残忍的解散了";
					String detail = "骚年，你加入的" + guild.getGuildName() + "公会被会长大人" + chairmanName + "残忍的解散了……不过别灰心，我们这儿还有更多更好的公会等待着你加入~";
					
					List<Long> userIdList = new ArrayList<Long>();
					for(GuildUser user : userList)
					{
						if(user.getUserId() == creatorId)
							continue;
						
						userIdList.add(user.getUserId());
					}
					pushNotify(userIdList, msgCategory, title, detail, null);
				}
				catch(Exception e)
				{
					GlobalObject.ERROR_LOG.error("at NotifyPushComponent.dismissByChairman throw an error.", e);
				}
			}
		};
		PUSH_EXECUTOR.execute(task);
	}
	
	/**
	 * 发送系统解散公会通知
	 * 会长接收通知
	 * @throws Exception
	 */
	public static void dismissGuildBySystem(final List<GuildUser> userList, final Guild guild) throws Exception
	{
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final String msgCategory = "dismiss_guild";
					long creatorId = guild.getCreatorId();
					String title = "公会被残忍的解散了";
					String detail = "会长大人，由于经营不善，您的公会已经关门大吉了……节哀……";
					List<Long> userIdList = new ArrayList<Long>();
					userIdList.add(creatorId);
					pushNotify(userIdList, msgCategory, title, detail, null);
				}
				catch(Exception e)
				{
					GlobalObject.ERROR_LOG.error("at NotifyPushComponent.dismissBySystem throw an error.", e);
				}
			}
		};
		PUSH_EXECUTOR.execute(task);
	}
	
	/**
	 * 发送申请加入公会通知
	 * 会长及管理员接收通知
	 * @throws Exception
	 */
	public static void applyGuild(final long applyUserId, final long guildId) throws Exception
	{
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					GuildUserRedis guildUserRedis = GuildUserRedisImpl.getInstance();
					
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
					
					///获取申请人信息
					User userInfo = UserComponent.getInfo(applyUserId);
					String nickName = "";
					if(null != userInfo)
						nickName = userInfo.getNickName();
					
					String msgCategory = "apply_join_guild";
					String title = "大人，您的公会有人希望加入";
					String detail = "大人安好，" + nickName + "请求你同意加入咱公会，请示下~";
					JSONObject source = new JSONObject();
					source.put("guild_id", guildId);
					
					for(GuildUser user : userList)
					{
						if(user.getRole() != GuildUserRole.CHAIRMAN && user.getRole() != GuildUserRole.ADMIN)
							continue;
						
						source.put("role", user.getRole());
						List<Long> userIdList = new ArrayList<Long>();
						userIdList.add(user.getUserId());
						pushNotify(userIdList, msgCategory, title, detail, source);
					}
				}
				catch(Exception e)
				{
					GlobalObject.ERROR_LOG.error("at NotifyPushComponent.apply throw an error.", e);
				}
			}
		};
		PUSH_EXECUTOR.execute(task);
	}
	
	/**
	 * 发送加入公会成功通知
	 * 申请发起人接收通知
	 * @throws Exception
	 */
	public static void joinGuild(final long applyUserId, final long guildId) throws Exception
	{
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					///获取公会信息
					GuildRedis guildRedis = GuildRedisImpl.getInstance();
					Guild guildInfo = guildRedis.getInfo(guildId);
					String guildName = "";
					if(null != guildInfo)
						guildName = guildInfo.getGuildName();
					
					String msgCategory = "join_guild_success";
					String title = "骚年，恭喜你加入" + guildName + "公会";
					String detail = "骚年，恭喜你加入" + guildName + "公会，入会后要好好表现哈，被踢很没面子的哈~";
					List<Long> userIdList = new ArrayList<Long>();
					userIdList.add(applyUserId);
					pushNotify(userIdList, msgCategory, title, detail, null);
				}
				catch(Exception e)
				{
					GlobalObject.ERROR_LOG.error("at NotifyPushComponent.join throw an error.", e);
				}
			}
		};
		PUSH_EXECUTOR.execute(task);
	}
	
	/**
	 * 发送被设置为管理员通知
	 * 被设置者接收通知
	 * @throws Exception
	 */
	public static void changeAdmin(final long userId, final long guildId) throws Exception
	{
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					///获取公会信息
					GuildRedis guildRedis = GuildRedisImpl.getInstance();
					Guild guildInfo = guildRedis.getInfo(guildId);
					String guildName = "";
					if(null != guildInfo)
						guildName = guildInfo.getGuildName();
					
					String msgCategory = "set_member_to_admin";
					String title = "骚年，恭喜你成为" + guildName + "公会的管理员";
					String detail = "骚年，鉴于你的优秀表现，" + guildName + "的会长大人把你提升为管理员了，快去抖抖威风吧~";
					JSONObject source = new JSONObject();
					source.put("guild_id", guildId);
					List<Long> userIdList = new ArrayList<Long>();
					userIdList.add(userId);
					pushNotify(userIdList, msgCategory, title, detail, source);
				}
				catch(Exception e)
				{
					GlobalObject.ERROR_LOG.error("at NotifyPushComponent.changeAdmin throw an error.", e);
				}
			}
		};
		PUSH_EXECUTOR.execute(task);
	}
	
	/**
	 * 发送被解除管理员职务通知
	 * 被设置者接收通知
	 */
	public static void changeMember(final long userId, final long guildId) throws Exception
	{
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					///获取公会信息
					GuildRedis guildRedis = GuildRedisImpl.getInstance();
					Guild guildInfo = guildRedis.getInfo(guildId);
					String guildName = "";
					if(null != guildInfo)
						guildName = guildInfo.getGuildName();
					
					String msgCategory = "set_admin_to_member";
					String title = "骚年，你在" + guildName + "公会的管理员职务被解除了……节哀……";
					String detail = "骚年你干了什么！？你在" + guildName + "公会中的管理员职务被会长大人解除了，节哀……";
					List<Long> userIdList = new ArrayList<Long>();
					userIdList.add(userId);
					pushNotify(userIdList, msgCategory, title, detail, null);
				}
				catch(Exception e)
				{
					GlobalObject.ERROR_LOG.error("at NotifyPushComponent.changeMember throw an error.", e);
				}
			}
		};
		PUSH_EXECUTOR.execute(task);
	}
	
	/**
	 * 发送被踢出公会通知
	 * 被移出者接收通知
	 * @throws Exception
	 */
	public static void deleteMember(final long userId, final long guildId) throws Exception
	{
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					///获取公会信息
					GuildRedis guildRedis = GuildRedisImpl.getInstance();
					Guild guildInfo = guildRedis.getInfo(guildId);
					String guildName = "";
					if(null != guildInfo)
						guildName = guildInfo.getGuildName();
					
					String msgCategory = "del_member_from_guild";
					String title = "骚年，你被踢出了" + guildName + "公会……节哀……";
					String detail = "骚年你干了什么！？你被" + guildName + "公会的会长大人踢出公会了，节哀……";
					List<Long> userIdList = new ArrayList<Long>();
					userIdList.add(userId);
					pushNotify(userIdList, msgCategory, title, detail, null);
				}
				catch(Exception e)
				{
					GlobalObject.ERROR_LOG.error("at NotifyPushComponent.deleteMember throw an error.", e);
				}
			}
		};
		PUSH_EXECUTOR.execute(task);
	}
	
	/**
	 * 发送公会成员退出公会通知
	 * 会长及管理员接收通知
	 * @throws Exception
	 */
	public static void quitGuild(final long userId, final long guildId) throws Exception
	{
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					///获取公会信息
					GuildRedis guildRedis = GuildRedisImpl.getInstance();
					Guild guildInfo = guildRedis.getInfo(guildId);
					if(null == guildInfo)
						return;
					
					///获取公会成员列表，便于发送通知
					GuildUserRedis guildUserRedis = GuildUserRedisImpl.getInstance();
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
					
					///获取退出公会的用户信息
					User userInfo = UserComponent.getInfo(userId);
					String nickName = "";
					String gender = "他";
					if(null != userInfo)
					{
						nickName = userInfo.getNickName();
						if(userInfo.getGender() == 0)
							gender = "她";
					}
					
					///给会长和管理员发送通知
					String msgCategory = "quit_guild";
					String title = "大人，有人离开了你的公会";
					String detail = "大大，您的会员" + nickName + "从公会出走了，要不要去把" + gender + "抓回来？";
					List<Long> userIdList = new ArrayList<Long>();
					for(GuildUser user : userList)
					{
						if(user.getRole() != GuildUserRole.CHAIRMAN && user.getRole() != GuildUserRole.ADMIN)
							continue;
						
						userIdList.add(user.getUserId());
					}
					pushNotify(userIdList, msgCategory, title, detail, null);
				}
				catch(Exception e)
				{
					GlobalObject.ERROR_LOG.error("at NotifyPushComponent.quitGuild throw an error.", e);
				}
			}
		};
		PUSH_EXECUTOR.execute(task);
	}
	
	public static void beforeDismiss(final Guild guild, final int type) throws Exception 
	{
	    	Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
			    try {
				String title = "大人，您的公会人太少啦";
				String detail = "";
				if (type == 1) {
				    detail = "会长大人，今天是您公会成立的第10天，但您的公会成员还没有达到10人，加油拉人啊~";
				} else if (type == 2) {
				    detail = "会长大人，今天是您公会成立的第13天，但您的公会成员还没有达到10人，加油拉人啊，再过两天，如果还未达到10人，公会就要关门大吉啦~~~";
				}
				long userId = guild.getCreatorId();
				List<Long> userIdList = new ArrayList<Long>();
				userIdList.add(userId);
				String msgCategory = "before_dismiss";
				    
				pushNotify(userIdList, msgCategory, title, detail, null);
			    }
			    catch(Exception e) 
			    {
			    	GlobalObject.ERROR_LOG.error("at NotifyPushComponent.beforeDismiss throw an error.", e);
			    }

			}
		};
		PUSH_EXECUTOR.execute(task);
	}
	
	private static void pushNotify(List<Long> userIdList, String msgCategory, String title, String detail, JSONObject source) throws Exception
	{
		JSONArray uids = new JSONArray(userIdList);
		JSONObject pushJson = new JSONObject();
		pushJson.put("act", "push_sys_msg");
		pushJson.put("uid_list", uids);
		JSONObject msgJson = new JSONObject();
		msgJson.put("msg_type", 1);
		msgJson.put("msg_category", msgCategory);
		JSONObject contentJson = new JSONObject();
		contentJson.put("title", title);
		contentJson.put("detail", detail);
		if(null != source)
			contentJson.put("source", source);
		
		msgJson.put("content", contentJson);
		pushJson.put("msg", msgJson);
		pushJson.put("is_show_notify", false);
		pushJson.put("click_act", "");
		
		///发送通知
		String result = HttpClientSender.post(GlobalObject.HTTP_CLIENT_CHATSERVICE, GlobalConfig.CHAT_SERVICE_URL, pushJson.toString());
		GlobalObject.INFO_LOG.info("push notify:" + pushJson.toString() + " result:" + result);
	}
}