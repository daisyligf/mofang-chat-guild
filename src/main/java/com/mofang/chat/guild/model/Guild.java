package com.mofang.chat.guild.model;

import java.util.Date;

import org.json.JSONObject;

import com.mofang.chat.guild.global.common.GuildStatus;
import com.mofang.framework.data.mysql.core.annotation.ColumnName;
import com.mofang.framework.data.mysql.core.annotation.PrimaryKey;
import com.mofang.framework.data.mysql.core.annotation.TableName;

/**
 * 
 * @author zhaodx
 *
 */
@TableName(name="guild")
public class Guild
{
	@PrimaryKey
	@ColumnName(name="guild_id")
	private Long guildId;
	@ColumnName(name="guild_name")
	private String guildName;
	@ColumnName(name="guild_name_original")
	private String guildNameOriginal;
	@ColumnName(name="guild_name_mark")
	private String guildNameMark;
	@ColumnName(name="guild_name_prefix")
	private String guildNamePrefix;
	@ColumnName(name="guild_name_prefix_original")
	private String guildNamePrefixOriginal;
	@ColumnName(name="guild_name_prefix_mark")
	private String guildNamePrefixMark;
	@ColumnName(name="avatar")
	private String avatar;
	@ColumnName(name="level")
	private Integer level;
	@ColumnName(name="intro")
	private String intro;
	@ColumnName(name="intro_original")
	private String introOriginal;
	@ColumnName(name="intro_mark")
	private String introMark;
	@ColumnName(name="notice")
	private String notice;
	@ColumnName(name="notice_original")
	private String noticeOriginal;
	@ColumnName(name="notice_mark")
	private String noticeMark;
	@ColumnName(name="background")
	private String background;
	@ColumnName(name="creator_id")
	private Long creatorId;
	@ColumnName(name="status")
	private Integer status;
	@ColumnName(name="hot")
	private Double hot;
	@ColumnName(name="hot_seq")
	private Long hotSeq;
	@ColumnName(name="new_seq")
	private Long newSeq;
	@ColumnName(name="create_time")
	private Date createTime;
	@ColumnName(name="need_audit")
	private Integer needAudit;
	
	public Long getGuildId()
	{
		return guildId;
	}
	
	public void setGuildId(Long guildId)
	{
		this.guildId = guildId;
	}
	
	public String getGuildName()
	{
		return guildName;
	}
	
	public void setGuildName(String guildName)
	{
		this.guildName = guildName;
	}
	
	public String getGuildNamePrefix() 
	{
		return guildNamePrefix == null ? "" : guildNamePrefix;
	}
	
	public void setGuildNamePrefix(String guildNamePrefix) 
	{
		this.guildNamePrefix = guildNamePrefix;
	}
	
	public String getAvatar()
	{
		return avatar == null ? "" : avatar;
	}
	
	public void setAvatar(String avatar)
	{
		this.avatar = avatar;
	}
	
	public Integer getLevel()
	{
		return level;
	}
	
	public void setLevel(Integer level)
	{
		this.level = level;
	}
	
	public String getIntro()
	{
		return intro == null ? "" : intro;
	}
	
	public void setIntro(String intro)
	{
		this.intro = intro;
	}
	
	public String getNotice() 
	{
		return notice == null ? "" : notice;
	}

	public void setNotice(String notice) 
	{
		this.notice = notice;
	}

	public String getBackground()
	{
		return background == null ? "" : background;
	}

	public void setBackground(String background)
	{
		this.background = background;
	}

	public Long getCreatorId()
	{
		return creatorId;
	}
	
	public void setCreatorId(Long creatorId)
	{
		this.creatorId = creatorId;
	}
	
	public Integer getStatus() 
	{
		return status;
	}
	
	public void setStatus(Integer status)
	{
		this.status = status;
	}
	
	public Double getHot()
	{
		return hot;
	}

	public void setHot(Double hot)
	{
		this.hot = hot;
	}

	public Long getHotSeq() 
	{
		return hotSeq;
	}

	public void setHotSeq(Long hotSeq)
	{
		this.hotSeq = hotSeq;
	}

	public Long getNewSeq()
	{
		return newSeq;
	}

	public void setNewSeq(Long newSeq)
	{
		this.newSeq = newSeq;
	}

	public Date getCreateTime() 
	{
		return createTime;
	}
	
	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}
	
	public String getGuildNameOriginal()
	{
	    return guildNameOriginal;
	}

	public void setGuildNameOriginal(String guildNameOriginal)
	{
	    this.guildNameOriginal = guildNameOriginal;
	}
	
	public String getGuildNameMark() {
	    return guildNameMark;
	}

	public void setGuildNameMark(String guildNameMark) {
	    this.guildNameMark = guildNameMark;
	}

	public String getGuildNamePrefixOriginal()
	{
	    return guildNamePrefixOriginal;
	}

	public void setGuildNamePrefixOriginal(String guildNamePrefixOriginal)
	{
	    this.guildNamePrefixOriginal = guildNamePrefixOriginal;
	}

	public String getIntroOriginal()
	{
	    return introOriginal;
	}

	public void setIntroOriginal(String introOriginal)
	{
	    this.introOriginal = introOriginal;
	}
	
	public String getNoticeOriginal() 
	{
	    return noticeOriginal;
	}

	public void setNoticeOriginal(String noticeOriginal) 
	{
	    this.noticeOriginal = noticeOriginal;
	}
	
	public String getGuildNamePrefixMark() {
	    return guildNamePrefixMark;
	}

	public void setGuildNamePrefixMark(String guildNamePrefixMark) 
	{
	    this.guildNamePrefixMark = guildNamePrefixMark;
	}

	public String getIntroMark() {
	    return introMark;
	}

	public void setIntroMark(String introMark) 
	{
	    this.introMark = introMark;
	}

	public String getNoticeMark() {
	    return noticeMark;
	}

	public void setNoticeMark(String noticeMark) 
	{
	    this.noticeMark = noticeMark;
	}
	
	public Integer getNeedAudit() {
		return needAudit;
	}

	public void setNeedAudit(Integer needAudit) {
		this.needAudit = needAudit;
	}

	public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put("id", guildId);
			json.put("name", guildName == null ? "" : guildName);
			json.put("name_original", guildNameOriginal == null ? "" : guildNameOriginal);
			json.put("name_mark", guildNameMark == null ? "" : guildNameMark);
			json.put("prefix", guildNamePrefix == null ? "" : guildNamePrefix);
			json.put("prefix_original", guildNamePrefixOriginal== null ? "" : guildNamePrefixOriginal);
			json.put("prefix_mark", guildNamePrefixMark == null ? "" : guildNamePrefixMark);
			json.put("avatar", avatar == null ? "" : avatar);
			json.put("level", level);
			json.put("intro", intro == null ? "" : intro);
			json.put("intro_original", introOriginal == null ? "" : introOriginal);
			json.put("intro_mark", introMark == null ? "" : introMark);
			json.put("notice", notice == null ? "" : notice);
			json.put("notice_original", noticeOriginal == null ? "" : noticeOriginal);
			json.put("notice_mark", noticeMark == null ? "" : noticeMark);
			json.put("background", background == null ? "" : background);
			json.put("creatorId", creatorId);
			json.put("status", status);
			json.put("hot", hot);
			json.put("hotSeq", hotSeq);
			json.put("newSeq", newSeq);
			json.put("createTime", createTime == null ? System.currentTimeMillis() : createTime.getTime());
			json.put("need_audit", needAudit);
			return json;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	public static Guild buildByJson(JSONObject json)
	{
		Guild model = new Guild();
		try
		{
			model.setGuildId(json.optLong("id", 0L));
			model.setGuildName(json.optString("name", ""));
			model.setGuildNameOriginal(json.optString("name_original", ""));
			model.setGuildNameMark(json.optString("name_mark", ""));
			model.setGuildNamePrefix(json.optString("prefix", ""));
			model.setGuildNamePrefixOriginal(json.optString("prefix_original", ""));
			model.setGuildNamePrefixMark(json.optString("prefix_mark", ""));
			model.setAvatar(json.optString("avatar", ""));
			model.setLevel(json.optInt("level", 1));
			model.setIntro(json.optString("intro", ""));
			model.setIntroOriginal(json.optString("intro_original", ""));
			model.setIntroMark(json.optString("intro_mark", ""));
			model.setNotice(json.optString("notice", ""));
			model.setNoticeOriginal(json.optString("notice_original", ""));
			model.setNoticeMark(json.optString("notice_mark", ""));
			model.setBackground(json.optString("background", ""));
			model.setCreatorId(json.optLong("creatorId", 0L));
			model.setStatus(json.optInt("status", GuildStatus.NORMAL));
			model.setHot(json.optDouble("hot", 0));
			model.setHotSeq(json.optLong("hotSeq", 0));
			model.setNewSeq(json.optLong("newSeq", 0));
			long time = json.optLong("createTime", System.currentTimeMillis());
			model.setCreateTime(new Date(time));
			model.setNeedAudit(json.optInt("need_audit", 1));
			return model;
		}
		catch(Exception e)
		{
			return null;
		}
	}
}