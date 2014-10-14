package com.mofang.chat.guild.mysql;

import java.util.List;

import com.mofang.chat.guild.model.GuildHotword;

/**
 * 
 * @author daisyli
 *
 */
public interface GuildHotwordDao 
{
    public GuildHotword get(int hotwordId) throws Exception;
    
    public GuildHotword find(String word) throws Exception;
    
    public boolean add(GuildHotword model) throws Exception;
    
    public boolean del(int hotwordId) throws Exception;
    
    public List<GuildHotword> list() throws Exception;
}
