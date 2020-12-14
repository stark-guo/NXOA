package com.web.oa.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

public class CustomFormAuthenticationFilter extends FormAuthenticationFilter{

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest req = (HttpServletRequest)request;
		String randomCode = (String)req.getSession().getAttribute("randomCode");
		String checkCode = req.getParameter("checkCode");
		if(randomCode!=null&&checkCode!=null&&!checkCode.equalsIgnoreCase(randomCode)) {
			req.setAttribute("shiroLoginFailure", "validataCodeMessage");
			return true;
		}
		return super.onAccessDenied(request, response);
	}
	
	
	@Override
	protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request,
			ServletResponse response) throws Exception {
		WebUtils.getAndClearSavedRequest(request);
		WebUtils.redirectToSavedRequest(request, response, "/main");
		return false;
	}
}
