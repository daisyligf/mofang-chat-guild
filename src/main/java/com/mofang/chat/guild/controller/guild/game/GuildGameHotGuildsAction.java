package com.mofang.chat.guild.controller.guild.game;

import com.mofang.chat.guild.controller.AbstractActionExecutor;
import com.mofang.chat.guild.global.ResultValue;
import com.mofang.chat.guild.logic.GuildGameLogic;
import com.mofang.chat.guild.logic.impl.GuildGameLogicImpl;
import com.mofang.framework.web.server.annotation.Action;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author daisyli
 *
 */
@Action(url="guild/game/hotguilds")
public class GuildGameHotGuildsAction extends AbstractActionExecutor 
{
    	private GuildGameLogic logic = GuildGameLogicImpl.getInstance();
    
    	@Override
	protected ResultValue exec(HttpRequestContext context) throws Exception
	{
		return logic.getHotGuilds(context);
	}
}
