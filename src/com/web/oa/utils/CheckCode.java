package com.web.oa.utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.Response;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.ShearCaptcha;

@WebServlet(urlPatterns="/checkcode")
public class CheckCode extends HttpServlet{
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LineCaptcha captcha = CaptchaUtil.createLineCaptcha(135,50);
		HttpSession session = request.getSession();
		session.setAttribute("randomCode", captcha.getCode());
		captcha.write(response.getOutputStream());
	}
	
	/*@Override
	public void doget(HttpServletRequest request,HttpServletResponse response){
		LineCaptcha captcha = CaptchaUtil.createLineCaptcha(100, 20);
		HttpSession session = request.getSession();
		session.setAttribute("randomCode", captcha.getCode());
		captcha.write(response.getOutputStream());
	}*/
	
}
