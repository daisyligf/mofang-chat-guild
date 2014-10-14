package com.mofang.chat.guild.controller.guild.hotword;

import com.mofang.chat.guild.controller.AbstractActionExecutor;
import com.mofang.chat.guild.global.ResultValue;
import com.mofang.chat.guild.logic.GuildHotwordLogic;
import com.mofang.chat.guild.logic.impl.GuildHotwordLogicImpl;
import com.mofang.framework.web.server.annotation.Action;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author daisyli
 *
 */
@Action(url="guild/hotword/add")
public class GuildHotwordAddAction extends AbstractActionExecutor 
{
    	private GuildHotwordLogic logic = GuildHotwordLogicImpl.getInstance();

	@Override
	protected ResultValue exec(HttpRequestContext context) throws Exception
	{
		return logic.add(context);
	}
	
	protected boolean needCheckAtom()
	{
		return false;
	}
}
