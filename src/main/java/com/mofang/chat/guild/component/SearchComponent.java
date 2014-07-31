package com.mofang.chat.guild.component;

import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.mofang.chat.guild.global.GlobalObject;

/**
 * 
 * @author zhaodx
 *
 */
public class SearchComponent
{
	/**
	 * 简单搜索
	 * @param keyword
	 * @param type
	 * @param start
	 * @param rows
	 * @return
	 */
	public static QueryResponse simpleSearch(String keyword, int start, int rows, List<SortClause> sortClauses) throws Exception
	{
		SolrQuery query = createDefaultQuery(keyword);
		addSortQuery(query, sortClauses);
		query.setStart(start);
		query.setRows(rows);
		return request(query);
	}
	
	/**
	 * 默认搜索
	 * 采用组合方式搜索
	 * @param keyword
	 * @param start
	 * @param rows
	 * @return
	 */
	public static QueryResponse defaultSearch(String keyword, int start, int rows) throws Exception
	{
		QueryResponse queryResponse = null;
		queryResponse = simpleSearch(keyword, start, rows, null);
		return queryResponse;
	}
	
	/**
	 * 请求搜索
	 * @param solrQuery 搜索信息资料
	 * @return
	 */
	public static QueryResponse request(SolrQuery solrQuery) throws Exception
	{
		try 
		{
			QueryResponse response = GlobalObject.SEARCH_SOLR_SERVER.query(solrQuery);
			return response;
		} 
		catch (Exception e) 
		{
			throw e;
		}
	}
	
	/**
	 * 创建默认搜索条件
	 * @param keyword
	 * @param type
	 * @return
	 */
	public static SolrQuery createDefaultQuery(String keyword)
	{
		SolrQuery query = new SolrQuery();
		query.setQuery("(guild_name:\""+keyword+"\"^1.6 OR "+"game_list:\""+keyword+"\")");
		return query;
	}
	
	/**
	 * 添加排序条件
	 * @param query
	 * @param sortClauses 排序条件
	 * @return
	 */
	public static SolrQuery addSortQuery(SolrQuery query, List<SortClause> sortClauses)
	{
		if(sortClauses != null)
			for (Iterator<SortClause> iterator = sortClauses.iterator(); iterator.hasNext();)
				query.addSort(iterator.next());
		
		return query;
	}
}