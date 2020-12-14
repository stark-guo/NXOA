package com.web.oa.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.web.oa.mapper.BaoxiaoBillMapper;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.pojo.BaoxiaoBillExample;
import com.web.oa.pojo.BaoxiaoBillExample.Criteria;
import com.web.oa.service.BaoxiaoService;

@Service
public class BaoxiaoServiceImpl implements BaoxiaoService {

	@Autowired
	private BaoxiaoBillMapper baoxiaoBillMapper;
	
	@Override
	public void saveBaoxiaoBills(BaoxiaoBill baoxiaoBill) {
		// TODO Auto-generated method stub
		//获取请假单ID
		Long id = baoxiaoBill.getId();
		if(id==null){
			//添加请假数据
			baoxiaoBillMapper.insert(baoxiaoBill);
		}else {
			//更新保存
			baoxiaoBillMapper.updateByPrimaryKey(baoxiaoBill);
		}
		
	}

	@Override
	public List<BaoxiaoBill> findBaoxiaoBillListByUser(Long userid) {
		// TODO Auto-generated method stub
		BaoxiaoBillExample example = new BaoxiaoBillExample();
		Criteria criteria = example.createCriteria();
		criteria.andUserIdEqualTo(userid);
		List<BaoxiaoBill> list = baoxiaoBillMapper.selectByExample(example);
		return list;
	}

	@Override
	public BaoxiaoBill findBaoxiaoBillById(Long id) {
		// TODO Auto-generated method stub
		BaoxiaoBill baoxiaoBill = baoxiaoBillMapper.selectByPrimaryKey(id);
		return baoxiaoBill;
	}

	@Override
	public void deleteBaoxiaoBillById(Long id) {
		// TODO Auto-generated method stub
		baoxiaoBillMapper.deleteByPrimaryKey(id);
	}

	@Override
	public PageInfo<BaoxiaoBill> findLeaveBillListByUser(Integer pageNum,Integer pageSize, Long id) {
		PageHelper.startPage(pageNum, pageSize);
		// TODO Auto-generated method stub
		BaoxiaoBillExample example = new BaoxiaoBillExample();
		Criteria criteria = example.createCriteria();
		criteria.andUserIdEqualTo(id);
		List<BaoxiaoBill> list = baoxiaoBillMapper.selectByExample(example);
		PageInfo<BaoxiaoBill> pageInfo = new PageInfo<>(list);
		return pageInfo;
	}

}
