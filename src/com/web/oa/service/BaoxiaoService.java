package com.web.oa.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.web.oa.pojo.BaoxiaoBill;

public interface BaoxiaoService {

	 	void saveBaoxiaoBills(BaoxiaoBill baoxiaoBill);
	 	
	 	List<BaoxiaoBill> findBaoxiaoBillListByUser(Long userid);
		
		BaoxiaoBill findBaoxiaoBillById(Long id);
		
		void deleteBaoxiaoBillById(Long id);

		PageInfo<BaoxiaoBill> findLeaveBillListByUser(Integer pageNum,Integer pageSize,Long id);
}
