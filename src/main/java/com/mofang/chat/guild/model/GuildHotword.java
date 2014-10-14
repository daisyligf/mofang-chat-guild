package com.mofang.chat.guild.model;

import java.util.Date;

import org.json.JSONObject;

import com.mofang.framework.data.mysql.core.annotation.ColumnName;
import com.mofang.framework.data.mysql.core.annotation.PrimaryKey;
import com.mofang.framework.data.mysql.core.annotation.TableName;

/**
 * 
 * @author daisyli
 *
 */
@TableName(name="guild_hotword")
public class GuildHotword 
{
    @PrimaryKey
    @ColumnName(name="id")
    private int id;
    
    @ColumnName(name="word")
    private String word;
    
    @ColumnName(name="position")
    private int position;
    
    @ColumnName(name="time")
    private Date time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
    
    public JSONObject toJson() 
    {
	JSONObject json = new JSONObject();
	try {
	    json.put("id", id);
	    json.put("word", word);
	    json.put("position", position);
	    json.put("time", time.getTime());
	    return json;
	} catch (Exception e) {
	    return null;
	}
    }
    
}
