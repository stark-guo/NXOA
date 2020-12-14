package com.web.oa.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.service.BaoxiaoService;
import com.web.oa.service.WorkFlowService;
import com.web.oa.utils.Constans;

@Controller
public class BaoXiaoController {

	@Autowired
	private WorkFlowService workFlowService;
	@Autowired
	private BaoxiaoService baoxiaoService;
	
	@RequestMapping("/main")
	public String main(Model model) {
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("activeUser",activeUser);
		return "index";
	}
	
	@RequestMapping("/myBaoxiaoBill")
	public String myBaoxiaoBill(@RequestParam(value="pageNum",defaultValue="1")Integer pageNum,Model model) {
		//1：查询所有的请假信息（对应a_leavebill），返回List<LeaveBill>
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		PageInfo<BaoxiaoBill> list = baoxiaoService.findLeaveBillListByUser(pageNum,5,activeUser.getId());
		for ( BaoxiaoBill page :list.getList() ) {
			System.out.println(page);
		}
		
		model.addAttribute("baoxiaoList", list.getList());
		model.addAttribute("pageInfo",list);
		return "baoxiaobill";
	}
	
	
	
	//查看当前流程图
	@RequestMapping("/viewCurrentImageByBill")
	public String viewCurrentImageByBill(long billId,ModelMap model){
		 String BUSSINESS_KEY = Constans.BAOXIAO_KEY+"."+billId;
		 Task task = workFlowService.findTaskByBussinessKey(BUSSINESS_KEY);
		 //通过任务ID获取任务对象,通过任务对象获取流程定义Id,查询流程定义对象
		 ProcessDefinition processDefinition = workFlowService.findProcessDefinitionByTaskId(task.getId());
		 
		 model.addAttribute("deploymentId", processDefinition.getDeploymentId());
		 model.addAttribute("imageName", processDefinition.getDiagramResourceName());
		 /**查看当前活动，获取当期活动对应的坐标x,y,width,height，将4个值存放到Map<String,Object>中*/
		 Map<String, Object> map = workFlowService.findCoordingByTask(task.getId());
		 model.addAttribute("acs", map);
		return "viewimage";
	}
	
}
