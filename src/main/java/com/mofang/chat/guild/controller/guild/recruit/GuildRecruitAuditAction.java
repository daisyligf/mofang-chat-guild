package com.mofang.chat.guild.controller.guild.recruit;

import com.mofang.chat.guild.controller.AbstractActionExecutor;
import com.mofang.chat.guild.global.ResultValue;
import com.mofang.chat.guild.logic.GuildRecruitLogic;
import com.mofang.chat.guild.logic.impl.GuildRecruitLogicImpl;
import com.mofang.framework.web.server.annotation.Action;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author zhaodx
 *
 */
@Action(url="guild/recruit/audit")
public class GuildRecruitAuditAction extends AbstractActionExecutor
{
	private GuildRecruitLogic logic = GuildRecruitLogicImpl.getInstance(); 

	@Override
	protected ResultValue exec(HttpRequestContext context) throws Exception
	{
		return logic.audit(context);
	}
	
	protected boolean needCheckAtom()
	{
		return false;
	}
}