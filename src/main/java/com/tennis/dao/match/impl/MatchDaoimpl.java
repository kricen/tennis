package com.tennis.dao.match.impl;

import com.tennis.dao.common.impl.GenericDaoImpl;
import com.tennis.dao.match.IMatchDao;
import com.tennis.model.common.PageResults;
import com.tennis.model.db.Match;

import org.hibernate.Query;
import org.hibernate.ScrollableResults;

import java.util.ArrayList;
import java.util.List;

/**
 * All rights Reserved, Designed By  lixiao
 * Copyright (c) 2017 by Shanghai lixiao
 *
 * @PROJECT_NAME: tennis
 * @author: lixiao
 * @version: V1.0
 * @Date: 2017/3/29
 * @Description:
 */
public class MatchDaoimpl extends GenericDaoImpl<Match, Integer> implements IMatchDao
{

	/**
	 * 根据时间获取用户的比赛记录
	 *
	 * @param userId
	 * @param state
	 * @param startDate
	 * @param endDate   @return
	 */
	public PageResults<Match> getMatchs(int userId, int state, int startDate, int endDate, int page, int pageSize)
	{


		String hql      = "from Match m where 1=1";
		String countHql = "from Match m where 1==1";


		String pingjie = "defenderMainUser=" + userId + " or deferderMinUser=" + userId + " or " + "challengeMainUser=" + userId + " or challengeMinUser=" + userId + " and state=" + state;
		//动态添加参数
		if (startDate > 0 && endDate > 0)
		{
			pingjie += " and startTime >=" + startDate + " and endTime<=" + endDate;
		}
		pingjie += " order by id desc";

		hql += pingjie;
		countHql += pingjie;


		PageResults<Match> matchs = findMatchs(hql, countHql, page, pageSize, null);

		return matchs;
	}

	/**
	 * 获取用户已经参与的场次
	 *
	 * @param userId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Match> getEffectiveMatchs(int userId, int startDate, int endDate)
	{
		String sql = "select * from `match` m where m.defender_main_user=? or m" +
				".deferder_min_user=? or m.challenge_main_user=? or m.challenge_min_user=? and m" +
				".start_time >=? and m.end_time<=?  and state in (1,2) order by id desc";
		List<Match> listBySQL = this.getListBySQL(sql, userId, userId, userId, userId, startDate, endDate);
		return listBySQL;
	}


	//private functions


	/**
	 * @param hql      HQL语句
	 * @param countHql 查询记录条数的HQL语句
	 * @param pageNo   下一页
	 * @param pageSize 一页总条数
	 * @param values   不定Object数组参数
	 * @return PageResults的封装类，里面包含了页码的信息以及查询的数据List集合
	 */
	private PageResults<Match> findMatchs(String hql, String countHql, int pageNo, int pageSize, Object... values)
	{
		PageResults<Match> retValue = new PageResults<Match>();
		Query              query    = this.getSession().createQuery(hql);
		if (values != null)
		{
			for (int i = 0; i < values.length; i++)
			{
				query.setParameter(i, values[i]);
			}
		}
		int currentPage = pageNo > 1 ? pageNo : 1;
		retValue.setCurrentPage(currentPage);
		retValue.setPageSize(pageSize);
		if (countHql != null)
		{
			Long count = countByHql(countHql, values);
			retValue.setTotalCount(count.intValue());

		}
		else
		{
			ScrollableResults results = query.scroll();
			results.last();
			retValue.setTotalCount(results.getRowNumber() + 1);// 设置总记录数
		}

		retValue.resetPageNo();
		List<Match> itemList = query.setFirstResult((currentPage - 1) * pageSize).setMaxResults(pageSize).list();
		if (itemList == null)
		{
			itemList = new ArrayList<Match>();
		}
		retValue.setResults(itemList);

		return retValue;
	}

}