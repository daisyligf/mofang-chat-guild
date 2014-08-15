package com.mofang.chat.guild.controller.guild.user;

import com.mofang.chat.guild.controller.AbstractActionExecutor;
import com.mofang.chat.guild.global.ResultValue;
import com.mofang.chat.guild.logic.GuildUserLogic;
import com.mofang.chat.guild.logic.impl.GuildUserLogicImpl;
import com.mofang.framework.web.server.annotation.Action;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * @author daisyli
 *
 */
@Action(url="guild/user/exists")
public class GuildUserExistsAction extends AbstractActionExecutor
{
    private GuildUserLogic logic = GuildUserLogicImpl.getInstance();
    
    protected ResultValue exec(HttpRequestContext context) throws Exception
    {
	return logic.userExists(context);
    }
}