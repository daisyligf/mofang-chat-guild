package com.mofang.chat.guild.controller.guild;

import com.mofang.chat.guild.controller.AbstractActionExecutor;
import com.mofang.chat.guild.global.ResultValue;
import com.mofang.chat.guild.logic.GuildLogic;
import com.mofang.chat.guild.logic.impl.GuildLogicImpl;
import com.mofang.framework.web.server.annotation.Action;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author daisyli
 *
 */
@Action(url="guild/set_need_audit")
public class GuildSetNeedAuditAction extends AbstractActionExecutor
{
	private GuildLogic logic = GuildLogicImpl.getInstance();

	@Override
	protected ResultValue exec(HttpRequestContext context) throws Exception
	{
		return logic.setNeedAudit(context);
	}
}
