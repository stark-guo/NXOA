package com.web.oa.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.pojo.Employee;
import com.web.oa.service.BaoxiaoService;
import com.web.oa.service.WorkFlowService;
import com.web.oa.utils.Constans;

@Controller
public class WorkFlowController {

	
	@Autowired
	private BaoxiaoService baoxiaoService;
	@Autowired
	private WorkFlowService workFlowService;
	
	@RequestMapping("/deployProcess")
	public String saveDeployProcess(MultipartFile fileName,String processName) throws IOException {
		workFlowService.saveDeployProcess(fileName.getInputStream(), processName);
		return "add_process";
	}
	
	@RequestMapping("/processDefinitionList")
	public ModelAndView processDefinitionList() {
		ModelAndView mav = new ModelAndView();
		//1:查询部署对象信息，对应表（act_re_deployment）
		List<Deployment> deploymentList = workFlowService.findDeploymentList();
		//2:查询流程定义的信息，对应表（act_re_procdef）
		List<ProcessDefinition> definitionList = workFlowService.findProcessDefinitionList();
		//放置到上下文对象中
		mav.addObject("depList", deploymentList);
		mav.addObject("pdList", definitionList);
		
		mav.setViewName("workflow_list");
		return mav;
	}
	
	@RequestMapping("/saveStartBaoxiao")
	public String saveStartBaoxiao(BaoxiaoBill baoxiaoBill,HttpSession session) {
		//设置当前时间
		baoxiaoBill.setCreatdate(new Date());
		//请假单状态
		//流程运行中 默认为1
		//流程结束 2
		baoxiaoBill.setState(1);
		
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		baoxiaoBill.setUserId(activeUser.getId());
		baoxiaoService.saveBaoxiaoBills(baoxiaoBill);
		
		workFlowService.saveStartProcess(baoxiaoBill.getId(),activeUser.getUsername());
		
		return "redirect:/myTaskList";
	}
	
	@RequestMapping("/myTaskList")
	public ModelAndView getTaskList(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		ActiveUser activeUser = (ActiveUser)SecurityUtils.getSubject().getPrincipal();
		List<Task> list = workFlowService.findTaskListByName(activeUser.getUsername());
		mav.addObject("taskList",list);
		mav.setViewName("workflow_task");
		return mav;
	}
	
	@RequestMapping("/viewTaskForm")
	public ModelAndView viewTaskForm(String taskId) {
		ModelAndView mav = new ModelAndView();
		BaoxiaoBill baoxiaoBill = workFlowService.findBaoxiaoBillByTaskId(taskId);
		List<Comment> commentList = workFlowService.findCommentByTaskId(taskId);
		List<String> outcomeList = workFlowService.findOutComeListByTaskId(taskId);
		mav.addObject("baoxiaoBill",baoxiaoBill);
		mav.addObject("commentList",commentList);
		mav.addObject("outcomeList", outcomeList);
		mav.addObject("taskId", taskId);
		
		mav.setViewName("approve_baoxiao");
		return mav;
	}
	
	//查看历史批注信息
	@RequestMapping("/viewHisComment")
	public String viewHisComment(long id,Model model) {
		//1：使用报销单ID，查询报销单对象
		BaoxiaoBill baoxiaoBill = baoxiaoService.findBaoxiaoBillById(id);
		model.addAttribute("baoxiaoBill", baoxiaoBill);
		//2：使用请假单ID，查询历史的批注信息
		List<Comment> list = workFlowService.findCommentByBaoxiaoBillId(id);
		model.addAttribute("commentList", list);
		return "workflow_commentlist";
	}
	
	@RequestMapping("/leaveBillAction_delete")
	public String deleteBaoxiaoBill(long id) {
		baoxiaoService.deleteBaoxiaoBillById(id);
		return "redirect:/myBaoxiaoBill";
	}
	
	@RequestMapping("/submitTask")
	public String submitTask(long id,String taskId,String comment,String outcome,HttpSession session) {
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		String username = activeUser.getUsername();
		workFlowService.saveSubmitTask(id, taskId, comment, outcome, username);
		return "redirect:/myTaskList";
	}
	
	@RequestMapping("/delDeployment")
	public String delDeployment(String deploymentId) {
		//通过部署对象Id,删除流程定义
		workFlowService.deleteProcessDefinitionByDeploymentId(deploymentId);
		return "redirect:/processDefinitionList";
	}
	
	//查看流程图
	@RequestMapping("/viewImage")
	public String viewImage(String deploymentId,String imageName,HttpServletResponse response) throws Exception {
		InputStream inputStream = workFlowService.findImageInputStream(deploymentId, imageName);
		OutputStream outputStream = response.getOutputStream();
		
		for (int i =-1; (i=inputStream.read())!=-1;) {
			outputStream.write(i);
		}
		
		outputStream.close();
		inputStream.close();
		
		return null;
	}
	
	//查看当前流程图
	@RequestMapping("/viewCurrentImage")
	public String viewCurrentImage(String taskId,Model model) {
		ProcessDefinition processDefinition = workFlowService.findProcessDefinitionByTaskId(taskId);
		model.addAttribute("deploymentId", processDefinition.getDeploymentId());
		model.addAttribute("imageName", processDefinition.getDiagramResourceName());
		
		Map<String, Object> map = workFlowService.findCoordingByTask(taskId);
		model.addAttribute("acs", map);
		return "viewimage";
	}
}


