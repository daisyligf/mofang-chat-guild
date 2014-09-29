package com.mofang.chat.guild.init.impl;

import java.io.IOException;

import com.mofang.chat.guild.init.AbstractInitializer;
import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.framework.util.IniParser;

/**
 * 
 * @author zhaodx
 *
 */
public class GlobalConfigInitializer extends AbstractInitializer
{
	private String configPath;
	
	public GlobalConfigInitializer(String configPath)
	{
		this.configPath = configPath;
	}
	
	@Override
	public void load() throws IOException 
	{
		IniParser config = new IniParser(configPath);
		GlobalConfig.SERVER_PORT = config.getInt("common", "server_port");
		GlobalConfig.CONN_TIMEOUT = config.getInt("common", "conn_timeout");
		GlobalConfig.READ_TIMEOUT = config.getInt("common", "read_timeout");
		
		GlobalConfig.SCAN_PACKAGE_PATH = config.get("conf", "scan_package_path");
		GlobalConfig.GLOBAL_MESSAGE_PATH = config.get("conf", "global_message_path");
		GlobalConfig.MYSQL_CONFIG_PATH = config.get("conf", "mysql_config_path");
		GlobalConfig.REDIS_MASTER_CONFIG_PATH = config.get("conf", "redis_master_config_path");
		GlobalConfig.REDIS_SLAVE_CONFIG_PATH = config.get("conf", "redis_slave_config_path");
		GlobalConfig.CHAT_SLAVE_CONFIG_PATH = config.get("conf", "chat_slave_config_path");
		GlobalConfig.LOG4J_CONFIG_PATH = config.get("conf", "log4j_config_path");
		GlobalConfig.HTTP_CLIENT_API_CONFIG_PATH = config.get("conf", "http_client_api_config_path");
		GlobalConfig.HTTP_CLIENT_CHATSERVICE_CONFIG_PATH = config.get("conf", "http_client_chatservice_config_path");
		
		
		GlobalConfig.GUILD_ID_START = Long.parseLong(config.get("logic", "guild_id_start"));
		GlobalConfig.GUILD_GROUP_ID_START = Long.parseLong(config.get("logic", "guild_group_id_start"));
		GlobalConfig.MAX_CREATE_GUILD_COUNT = config.getInt("logic", "max_create_guild_count");
		GlobalConfig.MAX_JOIN_GUILD_COUNT = config.getInt("logic", "max_join_guild_count");
		GlobalConfig.MAX_GUILD_GAME_REF_COUNT = config.getInt("logic", "max_guild_game_count");
		GlobalConfig.MAX_GUILD_MEMBER_COUNT = config.getInt("logic", "max_guild_member_count");
		GlobalConfig.MAX_GUILD_RECRUIT_COUNT = config.getInt("logic", "max_guild_recruit_count");
		GlobalConfig.USER_INFO_EXPIRE = config.getInt("logic", "user_info_expire");
		GlobalConfig.GAME_INFO_EXPIRE = config.getInt("logic", "game_info_expire");
		GlobalConfig.GUILD_LIST_EXPIRE = config.getInt("logic", "guild_list_expire");
		GlobalConfig.GUILD_INFO_EXPIRE = config.getInt("logic", "guild_info_expire");
		GlobalConfig.MY_GUILD_LIST_EXPIRE = config.getInt("logic", "my_guild_list_expire");
		GlobalConfig.GAME_GUILD_LIST_EXPIRE = config.getInt("logic", "game_guild_list_expire");
		GlobalConfig.GUILD_USER_LIST_EXPIRE = config.getInt("logic", "guild_user_list_expire");
		GlobalConfig.GUILD_GIFT_COUNT_EXPIRE = config.getInt("logic", "guild_gift_count_expire");
		GlobalConfig.GUILD_GAME_GIFT_COUNT_EXPIRE = config.getInt("logic", "guild_game_gift_count_expire");
		GlobalConfig.GUILD_CHECKIN_NUM_EXPIRE = config.getInt("logic", "guild_checkin_num_expire");
		GlobalConfig.GUILD_GROUP_USER_LIST_EXPIRE = config.getInt("logic", "guild_group_user_list_expire");
		GlobalConfig.MIN_NEW_GUILD_LIST_MEMBER = config.getInt("logic", "min_new_guild_list_member");
		GlobalConfig.MIN_QUIT_JOIN_GUILD_HOURS = config.getInt("logic", "min_quit_join_guild_hours");
		
		GlobalConfig.USER_INFO_URL = config.get("api", "user_info_url");
		GlobalConfig.GAME_INFO_URL = config.get("api", "game_info_url");
		GlobalConfig.GUILD_GIFT_COUNT_URL = config.get("api", "guild_gift_count_url");
		GlobalConfig.GUILD_GAME_GIFT_COUNT_URL = config.get("api", "guild_game_gift_count_url");
		GlobalConfig.GUILD_CHECKIN_NUM_URL = config.get("api", "guild_checkin_num_url");
		GlobalConfig.SOLR_URL = config.get("api", "solr_url");
		GlobalConfig.CHAT_SERVICE_URL = config.get("api", "chat_service_url");
		GlobalConfig.SENSITIVE_WORDS_SERVICE_URL = config.get("api", "sensitive_words_service_url");
		
		GlobalConfig.GUILD_CHECK_DAYS = config.getInt("task", "guild_check_days");
		GlobalConfig.GUILD_FIRST_ALARM_DAYS = config.getInt("task", "guild_first_alarm_days");
		GlobalConfig.GUILD_SECOND_ALARM_DAYS = config.getInt("task", "guild_second_alarm_days");
		GlobalConfig.GUILD_MIN_MEMBER_COUNT = config.getInt("task", "guild_min_member_count");
		GlobalConfig.HOT_GUILD_RANK_MEMBER_RATE = config.getFloat("task", "hot_guild_rank_member_rate");
		GlobalConfig.HOT_GUILD_RANK_MARK_RATE = config.getFloat("task", "hot_guild_rank_makr_rate");
		GlobalConfig.GUILD_USER_LIST_UPDATE_INTERVAL = config.getInt("task", "guild_user_list_update_interval");
		
		GlobalConfig.GUILD_DISMISS_TASK_TIME = config.get("cron", "guild_dismiss_task_time");
		GlobalConfig.HOT_GUILD_RANK_UPDATE_TASK_TIME = config.get("cron", "hot_guild_rank_update_task_time");
		GlobalConfig.NEW_GUILD_LIST_UPDATE_TASK_TIME = config.get("cron", "new_guild_list_update_task_time");
		GlobalConfig.GUILD_NEW_MEMBER_COUNT_CLEAR_TASK_TIME = config.get("cron", "guild_new_member_count_clear_task_time");
		GlobalConfig.GUILD_UNLOGIN_MEMBER_COUNT_7DAYS_CLEAR_TASK_TIME = config.get("cron", "guild_unlogin_member_count_7days_clear_task_time");
		GlobalConfig.GUILD_UNLOGIN_MEMBER_COUNT_30DAYS_CLEAR_TASK_TIME = config.get("cron", "guild_unlogin_member_count_30days_clear_task_time");
		
	}
}