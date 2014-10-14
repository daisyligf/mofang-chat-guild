package com.mofang.chat.guild.component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONObject;

import com.mofang.chat.guild.global.GlobalConfig;
import com.mofang.chat.guild.global.GlobalObject;
import com.mofang.framework.net.http.HttpClientSender;


/**
 * 
 * @author daisyli
 *
 */
public class TaskExecComponent 
{

    private final static int THREADS = Runtime.getRuntime().availableProcessors() + 1;
    private final ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
    
    private static TaskExecComponent instance = new TaskExecComponent();
    
    private TaskExecComponent() {}
    
    public static TaskExecComponent getInstance()
    {
	return instance;
    }
    
    
    public void exec(long userId, int eventId) throws Exception
    {
	TaskExec taskExec = new TaskExec(userId, eventId);
	Future<Integer> result = executorService.submit(taskExec);
	if (result.get() != 0)
	{
	    GlobalObject.ERROR_LOG.error("TaskExecComponent exec failed.");
	}
    }
    
    public class TaskExec implements Callable<Integer>
    {
	private long userId;
	private int eventId;
	
	public TaskExec(long userId, int eventId) {
	    this.userId = userId;
	    this.eventId = eventId;
	}
	
	@Override
	public Integer call() 
	{
	    String url = GlobalConfig.TASK_EXEC_URL;
	    try {
		JSONObject json = new JSONObject();
		json.put("uid", userId);
		json.put("event", eventId);
		String result = HttpClientSender.post(
			GlobalObject.HTTP_CLIENT_TASKSERVICE, url,
			json.toString());
		JSONObject res = new JSONObject(result);
		int code = res.optInt("code", -1);
		return code;
	    } catch (Exception e) {
		GlobalObject.ERROR_LOG.error("TaskExecComponent.TaskExec.call throws an error." + e);
		return -1;
	    }

	}
	
    }
}
