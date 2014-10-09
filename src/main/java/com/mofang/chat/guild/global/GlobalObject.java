package com.mofang.chat.guild.global;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.mofang.framework.data.mysql.pool.BoneCPPool;
import com.mofang.framework.data.mysql.pool.MysqlPool;
import com.mofang.framework.data.redis.RedisExecutor;
import com.mofang.framework.data.redis.pool.RedisPoolConfig;
import com.mofang.framework.data.redis.pool.RedisPoolProvider;
import com.mofang.framework.net.http.HttpClientConfig;
import com.mofang.framework.net.http.HttpClientProvider;

public class GlobalObject
{
	/**
	 * Master Redis Executor Instance
	 */
	public final static RedisExecutor REDIS_MASTER_EXECUTOR = new RedisExecutor();
	
	/**
	 * Slave Redis Executor Instance
	 */
	public final static RedisExecutor REDIS_SLAVE_EXECUTOR = new RedisExecutor();
	
	/**
	 * Chat Slave Redis Executor Instance
	 */
	public final static RedisExecutor CHAT_SLAVE_EXECUTOR = new RedisExecutor();
	
	/**
	 * Indexing Solr Server
	 */
	public final  static SolrServer INDEX_SOLR_SERVER = new HttpSolrServer(GlobalConfig.SOLR_URL);
	
	/**
	 * Search Solr Server
	 */
	public final  static SolrServer SEARCH_SOLR_SERVER = new HttpSolrServer(GlobalConfig.SOLR_URL);
	
	/**
	 * 
	 */
	public final static GlobalMessage GLOBAL_MESSAGE = new GlobalMessage();
	
	/**
	 * Mysql Pool Instance
	 */
	public static MysqlPool MYSQL_CONNECTION_POOL = null;
	
	/**
	 * Global PHP API Http Client Instance
	 */
	public static CloseableHttpClient HTTP_CLIENT_API;
	
	/**
	 * Global ChatService Http Client Instance
	 */
	public static CloseableHttpClient HTTP_CLIENT_CHATSERVICE;
	
	/**
	 * Global Info Logger Instance 
	 */
	public final static Logger INFO_LOG = Logger.getLogger("guild.info");
	
	/**
	 * Global Error Logger Instance
	 */
	public final static Logger ERROR_LOG = Logger.getLogger("guild.error");
	
	/***************************************初始化系统对象*************************************/
	private final static String JdbcUrlFat = "jdbc:mysql://%s:%s/%s?user=%s&password=%s&useUnicode=true&characterEncoding=%s&autoReconnect=true&failOverReadOnly=false";
	private final static String Driver = "com.mysql.jdbc.Driver";
	
	public static void initRedisMaster(String configPath) throws Exception
	{
		RedisPoolConfig config = getRedisConfig(configPath);
		JedisPool pool = RedisPoolProvider.getRedisPool(config);
		REDIS_MASTER_EXECUTOR.setJedisPool(pool);
	}
	
	public static void initRedisSlave(String configPath) throws Exception
	{
		RedisPoolConfig config = getRedisConfig(configPath);
		JedisPool pool = RedisPoolProvider.getRedisPool(config);
		REDIS_SLAVE_EXECUTOR.setJedisPool(pool);
	}
	
	public static void initChatSlave(String configPath) throws Exception
	{
		RedisPoolConfig config = getRedisConfig(configPath);
		JedisPool pool = RedisPoolProvider.getRedisPool(config);
		CHAT_SLAVE_EXECUTOR.setJedisPool(pool);
	}
	
	private static RedisPoolConfig getRedisConfig(String configPath) throws Exception
	{
        try
        {
        	Properties configurations = loadConfig(configPath);
			String host = configurations.getProperty("host");
			int port = Integer.valueOf(configurations.getProperty("port"));
			int timeout = Integer.valueOf(configurations.getProperty("timeout"));
			int maxActive = Integer.valueOf(configurations.getProperty("maxActive"));
			int maxIdle = Integer.valueOf(configurations.getProperty("maxIdle"));
			boolean testOnBorrow = Boolean.valueOf(configurations.getProperty("testOnBorrow"));
			
			RedisPoolConfig config = new RedisPoolConfig();
			JedisPoolConfig poolConf = new JedisPoolConfig();
			poolConf.setMaxActive(maxActive);
			poolConf.setMaxIdle(maxIdle);
			poolConf.setTestOnBorrow(testOnBorrow);
			config.setConfig(poolConf);
			config.setHost(host);
			config.setPort(port);
			config.setTimeout(timeout);
			return config;
        }
        catch(Exception e)
        {
        	throw e;
        }
	}

	public static void initMysql(String configPath) throws Exception
	{
		BoneCPConfig config = getMysqlConfig(configPath);
		Class.forName(Driver);
		BoneCP pool = new BoneCP(config);
		MYSQL_CONNECTION_POOL = new BoneCPPool(pool);
	}
	
	private static BoneCPConfig getMysqlConfig(String configPath) throws Exception
	{
        try
        {
        	Properties configurations = loadConfig(configPath);
			String host = configurations.getProperty("host");
			String port = configurations.getProperty("port");
			String user = configurations.getProperty("user");
			String password = configurations.getProperty("password");
			String charset = configurations.getProperty("charset");
			String dbname = configurations.getProperty("dbname");
			int partitionCount = Integer.valueOf(configurations.getProperty("partitionCount"));
			int maxConnectionsPerPartition = Integer.valueOf(configurations.getProperty("maxConnectionsPerPartition"));
			int minConnectionsPerPartition = Integer.valueOf(configurations.getProperty("minConnectionsPerPartition"));
			int acquireIncrement = Integer.valueOf(configurations.getProperty("acquireIncrement"));
			int releaseHelperThreads = Integer.valueOf(configurations.getProperty("releaseHelperThreads"));
			
			String jdbcUrl = String.format(JdbcUrlFat, host, port, dbname, user, password, charset);
			BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl(jdbcUrl);
			config.setPartitionCount(partitionCount);
			config.setMaxConnectionsPerPartition(maxConnectionsPerPartition);
			config.setMinConnectionsPerPartition(minConnectionsPerPartition);
			config.setAcquireIncrement(acquireIncrement);
			config.setReleaseHelperThreads(releaseHelperThreads);
			config.setIdleMaxAge(240, TimeUnit.SECONDS);
			config.setIdleConnectionTestPeriod(60, TimeUnit.SECONDS);
			config.setIdleMaxAgeInSeconds(1800);
			return config;
        }
        catch(Exception e)
        {
        	throw e;
        }
	}
	
	public static void initApiHttpClient(String configPath) throws Exception
	{
        try
        {
        	HttpClientProvider provider = getHttpClientProvider(configPath);
			HTTP_CLIENT_API = provider.getHttpClient();
        }
        catch(Exception e)
        {
        	throw e;
        }
	}
	
	public static void initChatServiceHttpClient(String configPath) throws Exception
	{
        try
        {
        	HttpClientProvider provider = getHttpClientProvider(configPath);
			HTTP_CLIENT_CHATSERVICE = provider.getHttpClient();
        }
        catch(Exception e)
        {
        	throw e;
        }
	}
	
	public static void initGlobalMessage(String configPath) throws Exception
	{
	    try 
	    {
		
		Properties configurations = loadConfig(configPath);
		GLOBAL_MESSAGE.SUCCESS = configurations.getProperty("success");
		GLOBAL_MESSAGE.CLIENT_REQUEST_DATA_IS_INVALID = configurations.getProperty("client_request_data_is_invalid");
		GLOBAL_MESSAGE.CLIENT_REQUEST_PARAMETER_FORMAT_ERROR = configurations.getProperty("client_request_parameter_format_error");
		GLOBAL_MESSAGE.CLIENT_REQUEST_LOST_NECESSARY_PARAMETER = configurations.getProperty("client_request_lost_necessary_parameter");
		GLOBAL_MESSAGE.SERVER_ERROR = configurations.getProperty("server_error");
		GLOBAL_MESSAGE.GUILD_ID_INVALID = configurations.getProperty("guild_id_invalid");
		GLOBAL_MESSAGE.USER_CAN_NOT_CREATE_GUILD = configurations.getProperty("user_can_not_create_guild");
		GLOBAL_MESSAGE.OVER_GUILD_GAME_MAX_COUNT = configurations.getProperty("over_guild_game_max_count");
		GLOBAL_MESSAGE.GUILD_NOT_EXISTS = configurations.getProperty("guild_not_exists");
		GLOBAL_MESSAGE.GUILD_CREATOR_CAN_NOT_QUIT = configurations.getProperty("guild_creator_can_not_quit");
		GLOBAL_MESSAGE.NO_PRIVILEGE_TO_OPERATE = configurations.getProperty("no_privilege_to_operate");
		GLOBAL_MESSAGE.NO_PRIVILEGE_TO_OPERATE_AUDIT = configurations.getProperty("no_privilege_to_operate_audit");
		GLOBAL_MESSAGE.NO_PRIVILEGE_TO_EDIT_GUILD_GAME = configurations.getProperty("no_privilege_to_edit_guild_game");
		GLOBAL_MESSAGE.NO_PRIVILEGE_TO_EDIT_GUILD_INFO = configurations.getProperty("no_privilege_to_edit_guild_info");
		GLOBAL_MESSAGE.NO_PRIVILEGE_TO_DISMISS_GUILD = configurations.getProperty("no_privilege_to_dismiss_guild");
		GLOBAL_MESSAGE.NO_PRIVILEGE_TO_CHANGE_ROLE = configurations.getProperty("no_privilege_to_change_role");
		GLOBAL_MESSAGE.NO_PRIVILEGE_TO_POST_RECRUIT = configurations.getProperty("no_privilege_to_post_recruit");
		GLOBAL_MESSAGE.NO_PRIVILEGE_TO_DEL_GROUP_USER = configurations.getProperty("no_privilege_to_del_group_user");
		GLOBAL_MESSAGE.GUILD_MEMBER_FULL = configurations.getProperty("guild_member_full");
		GLOBAL_MESSAGE.USER_JOIN_GUILD_UPPER_LIMIT = configurations.getProperty("user_join_guild_upper_limit");
		GLOBAL_MESSAGE.HE_JOIN_GUILD_UPPER_LIMIT = configurations.getProperty("he_join_guild_upper_limit");
		GLOBAL_MESSAGE.GUILD_GROUP_NOT_EXISTS = configurations.getProperty("guild_group_not_exists");
		GLOBAL_MESSAGE.GUILD_RECRUIT_NOT_EXISTS = configurations.getProperty("guild_recruit_not_exists");
		GLOBAL_MESSAGE.GUILD_MEMBER_EXISTS = configurations.getProperty("guild_member_exists");
		GLOBAL_MESSAGE.HE_JOINED_GUILD_ALREADY = configurations.getProperty("he_joined_guild_already");
		GLOBAL_MESSAGE.GUILD_GROUP_MEMBER_EXISTS = configurations.getProperty("guild_group_member_exists");
		GLOBAL_MESSAGE.GUILD_UNAUDIT_MEMBER_EXISTS = configurations.getProperty("guild_unaudit_member_exists");
		GLOBAL_MESSAGE.USER_QUIT_JOIN_GUILD_LIMIT = configurations.getProperty("user_quit_join_guild_limit");
		GLOBAL_MESSAGE.APPLY_UID_INVALID = configurations.getProperty("apply_uid_invalid");
		GLOBAL_MESSAGE.INVALID_OPERATION = configurations.getProperty("invalid_operation");
		GLOBAL_MESSAGE.GAME_ID_INVALID = configurations.getProperty("game_id_invalid");
		GLOBAL_MESSAGE.GAME_ARRAY_INVALID = configurations.getProperty("game_array_invalid");
		GLOBAL_MESSAGE.GROUP_ID_INVALID = configurations.getProperty("group_id_invalid");
		GLOBAL_MESSAGE.TYPE_INVALID = configurations.getProperty("type_invalid");
		GLOBAL_MESSAGE.USER_ID_INVALID = configurations.getProperty("user_id_invalid");
		GLOBAL_MESSAGE.MEMBER_ID_INVALID = configurations.getProperty("member_id_invalid");
		GLOBAL_MESSAGE.ROLE_INVALID = configurations.getProperty("role_invalid");
		GLOBAL_MESSAGE.NEED_GUILD_NAME = configurations.getProperty("need_guild_name");
		GLOBAL_MESSAGE.NEED_GUILD_AVATAR = configurations.getProperty("need_guild_avatar");
		GLOBAL_MESSAGE.RECRUIT_ID_INVALID = configurations.getProperty("recruit_id_invalid");
		GLOBAL_MESSAGE.RECRUIT_MESSAGE_CAN_NOT_BE_EMPTY = configurations.getProperty("recruit_message_can_not_be_empty");
		GLOBAL_MESSAGE.OP_TYPE_INVALID = configurations.getProperty("op_type_invalid");
		GLOBAL_MESSAGE.LIST_TYPE_INVALID = configurations.getProperty("list_type_invalid");
		GLOBAL_MESSAGE.KEYWORD_CAN_NOT_BE_EMPTY = configurations.getProperty("keyword_can_not_be_empty");
	    }
	    catch(Exception e)
	    {
		throw e;
	    }
	}
	
	private static HttpClientProvider getHttpClientProvider(String configPath) throws Exception
	{
		Properties configurations = loadConfig(configPath);
		String host = configurations.getProperty("host");
		int port = Integer.valueOf(configurations.getProperty("port"));
		int maxTotal = Integer.valueOf(configurations.getProperty("maxTotal"));
		String charset = configurations.getProperty("charset");
		int connTimeout = Integer.valueOf(configurations.getProperty("connTimeout"));
		int socketTimeout = Integer.valueOf(configurations.getProperty("socketTimeout"));
		int keepAliveTimeout = Integer.valueOf(configurations.getProperty("keepAliveTimeout"));
		int checkIdleInitialDelay = Integer.valueOf(configurations.getProperty("checkIdleInitialDelay"));
		int checkIdlePeriod = Integer.valueOf(configurations.getProperty("checkIdlePeriod"));
		int closeIdleTimeout = Integer.valueOf(configurations.getProperty("closeIdleTimeout"));
		
		HttpClientConfig config = new HttpClientConfig();
		config.setHost(host);
		config.setPort(port);
		config.setMaxTotal(maxTotal);
		config.setCharset(charset);
		config.setConnTimeout(connTimeout);
		config.setSocketTimeout(socketTimeout);
		config.setDefaultKeepAliveTimeout(keepAliveTimeout);
		config.setCheckIdleInitialDelay(checkIdleInitialDelay);
		config.setCheckIdlePeriod(checkIdlePeriod);
		config.setCloseIdleTimeout(closeIdleTimeout);
		
		HttpClientProvider provider = new HttpClientProvider(config);
		return provider;
	}
	
	private static Properties loadConfig(String configPath) throws Exception
	{
		Properties configurations = new Properties();
        File file = new File(configPath);
        try
        {
        	configurations.load(new FileInputStream(file));
        	return configurations;
        }
        catch(Exception e)
        {
        	throw e;
        }
	}
}