package com.mofang.chat.guild.logic;

import com.mofang.chat.guild.global.ResultValue;
import com.mofang.framework.web.server.reactor.context.HttpRequestContext;

/**
 * 
 * @author daisyli
 *
 */
public interface GuildHotwordLogic {

    public ResultValue add(HttpRequestContext context) throws Exception;
    
    public ResultValue del(HttpRequestContext context) throws Exception;
    
    public ResultValue list(HttpRequestContext context) throws Exception;
    
}
