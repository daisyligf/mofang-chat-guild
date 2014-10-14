package com.mofang.chat.guild.redis;

import java.util.Set;

import com.mofang.chat.guild.model.GuildHotword;

/**
 * 
 * @author daisyli
 *
 */
public interface GuildHotwordRedis 
{
    public boolean add(GuildHotword model) throws Exception;
    
    public boolean del(GuildHotword model) throws Exception;
    
    public Set<String> list() throws Exception;
    
    public Integer getPosition(final String word) throws Exception;
    
}
