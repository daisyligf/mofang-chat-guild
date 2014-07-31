package com.mofang.chat.guild.global;

public class ReturnCode
{
	/**
	 * 操作成功
	 */
	public final static int SUCCESS = 0;
	
	/**
	 * 无效参数
	 */
	public final static int CLIENT_REQUEST_DATA_IS_INVALID = 400;
	
	/**
	 * 请求参数格式不正确
	 */
	public final static int CLIENT_REQUEST_PARAMETER_FORMAT_ERROR = 401;
	
	/**
	 * 请求缺少必要参数
	 */
	public final static int CLIENT_REQUEST_LOST_NECESSARY_PARAMETER = 402;
	
	/**
	 * 服务器错误
	 */
	public final static int SERVER_ERROR = 500;
	
	/**
	 * 用户无法创建公会（已达创建上限）
	 */
	public final static int USER_CAN_NOT_CREATE_GUILD = 910;
	
	/**
	 * 超过公会关联游戏最大数
	 */
	public final static int OVER_GUILD_GAME_MAX_COUNT = 911;
	
	/**
	 * 公会不存在
	 */
	public final static int GUILD_NOT_EXISTS = 912;
	
	/**
	 * 无权操作
	 */
	public final static int NO_PRIVILEGE_TO_OPERATE = 913;
	
	/**
	 * 公会用户数已达上限
	 */
	public final static int GUILD_MEMBER_FULL = 915;
	
	/**
	 * 用户加入的公会数以到达上限
	 */
	public final static int USER_JOIN_GUILD_UPPER_LIMIT = 916;
	
	/**
	 * 公会群组不存在
	 */
	public final static int GUILD_GROUP_NOT_EXISTS = 917;
	
	/**
	 * 公会招募信息不存在
	 */
	public final static int GUILD_RECRUIT_NOT_EXISTS = 918;
	
	/**
	 * 公会成员已存在
	 */
	public final static int GUILD_MEMBER_EXISTS = 919;
	
	/**
	 * 公会群组成员已存在
	 */
	public final static int GUILD_GROUP_MEMBER_EXISTS = 920;
	
	/**
	 * 公会待审核成员已存在
	 */
	public final static int GUILD_UNAUDIT_MEMBER_EXISTS = 921;
}