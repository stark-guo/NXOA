package com.web.oa.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.catalina.authenticator.SpnegoAuthenticator.AuthenticateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.web.oa.mapper.BaoxiaoBillMapper;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.service.WorkFlowService;
import com.web.oa.utils.Constans;

@Service
public class WorkFlowServiceImpl implements WorkFlowService{

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private FormService formService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private BaoxiaoBillMapper baoxiaobillMapper;
	
	/**部署流程定义*/
	@Override
	public void saveDeployProcess(InputStream in, String fileName) {
		// TODO Auto-generated method stub
		ZipInputStream zip = new ZipInputStream(in);
		repositoryService.createDeployment()
		.name(fileName)
		.addZipInputStream(zip)
		.deploy();
		
		
	}
	@Override
	public List<Deployment> findDeploymentList() {
		// TODO Auto-generated method stub
		List<Deployment> list = repositoryService.createDeploymentQuery()
				.orderByDeploymenTime().asc().list();
		return list;
	}
	
	@Override
	public List<ProcessDefinition> findProcessDefinitionList() {
		// TODO Auto-generated method stub
		List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
				.orderByProcessDefinitionVersion().desc().list();
		return list;
	}
	
	@Override
	public void saveStartProcess(long baoxiaoId, String username) {
		// TODO Auto-generated method stub
		//使用当前对象获取到流程定义的key（对象的名称就是流程定义的key）
				String key= Constans.BAOXIAO_KEY;
				/**
				 * 从Session中获取当前任务的办理人，使用流程变量设置下一个任务的办理人
				 * inputUser是流程变量的名称，
				 * 获取的办理人是流程变量的值
				 */
				Map<String, Object> map = new HashMap<String,Object>();
				map.put("inputUser", username);//表示惟一用户

				//格式：baoxiao.id的形式（使用流程变量）
				String objId = key+"."+baoxiaoId;
				map.put("objId", objId);
				//5：使用流程定义的key，启动流程实例，同时设置流程变量，同时向正在执行的执行对象表中的字段BUSINESS_KEY添加业务数据，同时让流程关联业务
				runtimeService.startProcessInstanceByKey(key,objId,map);
	}
	
	@Override
	public List<Task> findTaskListByName(String name) {
		// TODO Auto-generated method stub
		List<Task> list = taskService.createTaskQuery()
				.taskAssignee(name)
				.orderByTaskCreateTime().desc().list();
		return list;
	}
	
	@Override
	public BaoxiaoBill findBaoxiaoBillByTaskId(String taskId) {
		// TODO Auto-generated method stub
		//根据任务Id，获取出task
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		//根据流程实例Id，获取processInstance
		//act_hi_procinst
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(task.getProcessInstanceId()).singleResult();
		//再从processInstance，取出businessKey
		String businessKey = processInstance.getBusinessKey();
		//截取leaveId
		String leaveId = "";
		if(businessKey!=null&&!businessKey.equals("")) {
			leaveId = businessKey.split("\\.")[1];
		}
		//根据leaveId去查询请假单信息leaveBill
		BaoxiaoBill baoxiaoBill = baoxiaobillMapper.selectByPrimaryKey(Long.parseLong(leaveId));
		
		return baoxiaoBill;
	}
	
	@Override
	public List<Comment> findCommentByTaskId(String taskId) {
		// TODO Auto-generated method stub
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		String instanceId = task.getProcessInstanceId();
		List<Comment> list = taskService.getProcessInstanceComments(instanceId);
		return list;
	}
	@Override
	public List<String> findOutComeListByTaskId(String taskId) {
		// TODO Auto-generated method stub
		//返回存放连线的名称集合
		List<String> list = new ArrayList<String>();
		//1:使用任务ID，查询任务对象
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		//2：获取流程定义ID
		String processDefinitionId = task.getProcessDefinitionId();
		//3：查询ProcessDefinitionEntiy对象
		ProcessDefinitionEntity processDefinitionEntity = 
				(ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinitionId);
		//使用任务对象Task获取流程实例ID
		String instanceId = task.getProcessInstanceId();
		//使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(instanceId).singleResult();
		//获取当前活动的id
		String activityId = processInstance.getActivityId();
		//4：获取当前的活动
		ActivityImpl activity = processDefinitionEntity.findActivity(activityId);
		//5：获取当前活动完成之后连线的名称
		List<PvmTransition> transitions = activity.getOutgoingTransitions();
		if(transitions!=null && transitions.size()>0) {
			for (PvmTransition pvmTransition : transitions) {
				String name = (String) pvmTransition.getProperty("name");
				if(name!=null && !name.equals("")) {
					list.add(name);
				}else {
					list.add("默认提交");
				}
			}
		}
		return list;
	}
	@Override
	public void saveSubmitTask(long id, String taskId, String comemnt, String outcome, String username) {
		// TODO Auto-generated method stub
		//1：在完成之前，添加一个批注信息，向act_hi_comment表中添加数据，用于记录对当前申请人的一些审核信息
		//使用任务ID，查询任务对象，获取流程流程实例ID
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		//获取流程实例Id
		String instanceId = task.getProcessInstanceId();
		/**
		 * 注意：添加批注的时候，由于Activiti底层代码是使用：
		   String userId = Authentication.getAuthenticatedUserId();
		   CommentEntity comment = new CommentEntity();
		   comment.setUserId(userId);
		      所有需要从Session中获取当前登录人，作为该任务的办理人（审核人），对应act_hi_comment表中的User_ID的字段，不过不添加审核人，该字段为null
		      所以要求，添加配置执行使用Authentication.setAuthenticatedUserId();添加当前任务的审核人
		 */
		//加当前任务审核人
		Authentication.setAuthenticatedUserId(username);
		//添加批注
		taskService.addComment(taskId, instanceId, comemnt);
		/**
		 * 2：如果连线的名称是“默认提交”，那么就不需要设置，如果不是，就需要设置流程变量
		 * 在完成任务之前，设置流程变量，按照连线的名称，去完成任务
			流程变量的名称：outcome
			流程变量的值：连线的名称
		 */
		Map<String,Object> map = new HashMap<String,Object>();
		if(outcome!=null && !outcome.equals("默认提交")) {
			map.put("message", outcome);
			//3：使用任务ID，完成当前人的个人任务，同时流程变量
			taskService.complete(taskId,map);
		}else {
			taskService.complete(taskId);
		}
		/**
		 * 5：在完成任务之后，判断流程是否结束
   			如果流程结束了，更新请假单表的状态从1变成2（审核中-->审核完成）
		 */
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(instanceId).singleResult();
		//流程结束了
		if(processInstance==null) {
			//更新请假单表的状态从1变成2（审核中-->审核完成）
			BaoxiaoBill baoxiaoBill = baoxiaobillMapper.selectByPrimaryKey(id);
			baoxiaoBill.setState(2);
			baoxiaobillMapper.updateByPrimaryKey(baoxiaoBill);
		}
	}
	
	@Override
	public ProcessDefinition findProcessDefinitionByTaskId(String taskId) {
		// TODO Auto-generated method stub
		//获取任务对象
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		//获取流程定义ID
		String definitionId = task.getProcessDefinitionId();
		//查询流程定义的对象
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
				.processDefinitionId(definitionId)
				.singleResult();
		return processDefinition;
	}
	@Override
	public Map<String, Object> findCoordingByTask(String taskId) {
		// TODO Auto-generated method stub
		Map<String,Object> map = new HashMap<String,Object>();
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		String definitionId = task.getProcessDefinitionId();
		//获取流程定义的实体对象（对应.bpmn文件中的数据）
		ProcessDefinitionEntity processDefinitionEntity = 
				(ProcessDefinitionEntity)repositoryService.getProcessDefinition(definitionId);
		String instanceId = task.getProcessInstanceId();
		//使用流程实例ID，查询正在执行的执行对象表，获取当前活动对应的流程实例对象
		ProcessInstance processInstanceId = runtimeService
				.createProcessInstanceQuery()
				.processInstanceId(instanceId)
				.singleResult();
		//获取当前活动的ID
		String activityId = processInstanceId.getActivityId();
		//获取当前活动对象
		ActivityImpl activity = processDefinitionEntity.findActivity(activityId);
		//获取坐标
		map.put("x", activity.getX());
		map.put("y", activity.getY());
		map.put("width", activity.getWidth());
		map.put("height", activity.getHeight());
		return map;
	}
	
	/**使用部署对象ID和资源图片名称，获取图片的输入流*/
	@Override
	public InputStream findImageInputStream(String deploymentId, String imageName) {
		// TODO Auto-generated method stub
		return repositoryService.getResourceAsStream(deploymentId, imageName);
		
	}
	@Override
	public Task findTaskByBussinessKey(String bUSSINESS_KEY) {
		// TODO Auto-generated method stub
		Task task = taskService.createTaskQuery()
				.processInstanceBusinessKey(bUSSINESS_KEY)
				.singleResult();
		return task;
	}
	
	//根据报销单ID查询历史批注
	@Override
	public List<Comment> findCommentByBaoxiaoBillId(long id) {
		// TODO Auto-generated method stub
		String bussiness_key = Constans.BAOXIAO_KEY+"."+id;
		HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
				.processInstanceBusinessKey(bussiness_key)
				.singleResult();
		List<Comment> commentList = taskService.getProcessInstanceComments(processInstance.getId());
		return commentList;
	}
	//强制删除(级联)
	@Override
	public void deleteProcessDefinitionByDeploymentId(String deploymentId) {
		// TODO Auto-generated method stub
		repositoryService.deleteDeployment(deploymentId, true);
	}
	
	
	
	
}
