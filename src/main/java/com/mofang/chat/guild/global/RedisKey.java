package com.mofang.chat.guild.global;

/**
 * 
 * @author zhaodx
 *
 */
public class RedisKey
{
	/**
	 * 公会自增ID rediskey
	 */
	public final static String GUILD_ID_INCREMENT_KEY = "guild_id";
	
	/**
	 * 公会信息key前缀
	 */
	public final static String GUILD_INFO_KEY_PREFIX = "guild_info_";
	
	/**
	 * 最热公会列表key(审核已通过)
	 */
	public final static String GUILD_HOT_LIST_KEY = "guild_hot_list";
	
	/**
	 * 最新公会列表key(审核已通过)
	 */
	public final static String GUILD_NEW_LIST_KEY = "guild_new_list";
	
	/**
	 * 我的公会列表key前缀
	 */
	public final static String MY_GUILD_LIST_KEY_PREFIX = "my_guild_list_";
	
	/**
	 * 公会会员信息key前缀
	 */
	public final static String GUILD_USER_INFO_KEY_PREFIX = "guild_user_info_";
	
	/**
	 * 公会会员列表key前缀
	 */
	public final static String GUILD_USER_LIST_KEY_PREFIX = "guild_user_list_";
	
	/**
	 * 公会待审核会员列表key前缀
	 */
	public final static String GUILD_USER_UNAUDITED_LIST_KEY_PREFIX = "guild_user_unaudited_list_";
	
	/**
	 * 公会游戏列表key前缀
	 */
	public final static String GUILD_GAME_LIST_KEY_PREFIX = "guild_game_list_";
	
	/**
	 * 游戏对应的公会列表key前缀
	 */
	public final static String GAME_GUILD_LIST_KEY_PREFIX = "game_guild_list_";
	
	/**
	 * 公会群组自增ID rediskey
	 */
	public final static String GUILD_GROUP_ID_INCREMENT_KEY = "guild_group_id";
	
	/**
	 * 公会群组信息key前缀
	 */
	public final static String GUILD_GROUP_INFO_KEY_PREFIX = "guild_group_info_";
	
	/**
	 * 公会群组列表key前缀
	 */
	public final static String GUILD_GROUP_LIST_KEY_PREFIX = "guild_group_list_";
	
	/**
	 * 公会游戏群组列表key前缀
	 */
	public final static String GUILD_GAME_GROUP_LIST_KEY_PREFIX = "guild_game_group_list_";
	
	/**
	 * 公会群组用户列表key前缀
	 */
	public final static String GUILD_GROUP_USER_LIST_KEY_PREFIX = "guild_group_user_list_";
	
	/**
	 * 公会招募信息自增IDkey
	 */
	public final static String GUILD_RECRUIT_ID_INCREMENT_KEY = "guild_recruit_id";
	
	/**
	 * 公会招募信息key前缀
	 */
	public final static String GUILD_RECRUIT_INFO_KEY_PREFIX = "guild_recruit_info_";
	
	/**
	 * 公会招募列表key前缀
	 */
	public final static String GUILD_RECRUIT_AUDIT_LIST_KEY = "guild_recruit_audit_list";
	
	/**
	 * 用户信息结果缓存key前缀
	 * 结构: cache_user_info_{userid}
	 */
	public final static String CACHE_USER_INFO_KEY_PREFIX = "cache_user_info_";
	
	/**
	 * 游戏信息key前缀
	 * 结构: cache_game_info_{gameid}
	 */
	public final static String CACHE_GAME_INFO_KEY_PREFIX = "cache_game_info_";
	
	/**
	 * 公会礼包总数key前缀
	 * 结构: cache_guild_gift_count_{guildid}
	 */
	public final static String CACHE_GUILD_GIFT_COUNT_KEY_PREFIX = "cache_guild_gift_count_";
	
	/**
	 * 公会游戏礼包总数key前缀
	 * 结构: cache_guild_game_gift_count_{guildid}_{gameid}
	 */
	public final static String CACHE_GUILD_GAME_GIFT_COUNT_KEY_PREFIX = "cache_guild_game_gift_count_";
	
	/**
	 * 公会每日签到数key前缀
	 * 结构: cache_guild_checkin_num_{guildid}
	 */
	public final static String CACHE_GUILD_CHECKIN_NUM_KEY_PREFIX = "cache_guild_checkin_num_";
	
	/**
	 * 公会列表结果缓存key前缀
	 * 结构: cache_guild_list_{type}_{start}_{end}
	 */
	public final static String CACHE_GUILD_LIST_KEY_PREFIX = "cache_guild_list_";
	
	/**
	 * 公会信息结果缓存key前缀
	 * 结构: cache_guild_info_{guildId}
	 */
	public final static String CACHE_GUILD_INFO_KEY_PREFIX = "cache_guild_info_";
	
	/**
	 * 公会成员列表结果缓存key前缀
	 * 结构: cache_guild_user_list_{guildid}
	 */
	public final static String CACHE_GUILD_USER_LIST_KEY_PREFIX = "cache_guild_user_list_";
	
	/**
	 * 我的公会列表结果缓存key前缀
	 * 结构: cache_my_guild_list_{userid}
	 */
	public final static String CACHE_MY_GUILD_LIST_KEY_PREFIX = "cache_my_guild_list_";
	
	/**
	 * 公会列表数结果缓存key前缀
	 * 结构：cache_guild_list_count_{userid}
	 */
	public final static String CACHE_GUILD_LIST_COUNT_KEY_PREFIX = "cache_guild_list_count_";
	
	/**
	 * 游戏公会列表结果缓存key前缀
	 * 结构: cache_game_guild_list_{gameid}
	 */
	public final static String CACHE_GAME_GUILD_LIST_KEY_PREFIX = "cache_game_guild_list_";
	
	/**
	 * 公会群组用户列表结果缓存key前缀
	 * 结构: cache_guild_group_user_list_{groupid}
	 */
	public final static String CACHE_GUILD_GROUP_USER_LIST_KEY_PREFIX = "cache_guild_group_user_list_";
	
	/**
	 * 用户群组的未读数key前缀
	 * hash结构: user_group_unread_{userid}:{groupid}:{unread_count}
	 * 例如: hset user_group_unread_111 222 {"from_uid" : 111, "content":"xxx", "is_show_notify": true, "click_act":"xxx"}
	 */
	public final static String USER_GROUP_UNREAD_KEY_PREFIX = "user_group_unread_";
	
	/**
	 * 公会今日新增用户数key(每日凌晨清空)
	 * hash结构 guild_new_member_count {guildid}:{count}
	 * 例如 hincrby guild_new_member_count 1000002 1
	 */
	public final static String GUILD_NEW_MEMBER_COUNT_KEY = "guild_new_member_count";
	
	/**
	* 公会7日未登录用户数key
	 * hash结构 guild_unlogin_member_count_7days {guildid}:{count}
	 * 例如 hset guild_unlogin_member_count_7days 1000002 10
	 */
	public final static String GUILD_UNLOGIN_MEMBER_COUNT_7DAYS_KEY = "guild_unlogin_member_count_7days";
	
	/**
	* 公会30日未登录用户数key
	 * hash结构 guild_unlogin_member_count_30days {guildid}:{count}
	 * 例如 hset guild_unlogin_member_count_30days 1000002 10
	 */
	public final static String GUILD_UNLOGIN_MEMBER_COUNT_30DAYS_KEY = "guild_unlogin_member_count_30days";
}