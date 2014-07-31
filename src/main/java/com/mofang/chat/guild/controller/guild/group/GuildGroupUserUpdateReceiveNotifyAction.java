package com.mofang.chat.guild.controller.guild.group;

import com.mofang.chat.guild.controller.AbstractActionExecutor;
import com.mofang.chat.guild.global.ResultValue;
import com.mofang.chat.guild.logic.GuildGroupUserLogic;
import com.mofang.chat.guild.logic.impl.GuildGroupUserLogicImpl;
import com.mofang.framework.web.server.annotation.Action;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
@Action(url="guild/group/user/update_receive_notify")
public class GuildGroupUserUpdateReceiveNotifyAction extends AbstractActionExecutor
{
	private GuildGroupUserLogic logic = GuildGroupUserLogicImpl.getInstance();

	@Override
	protected ResultValue exec(HttpRequestContext context) throws Exception
	{
		return logic.updateReceiveNotify(context);
	}
}