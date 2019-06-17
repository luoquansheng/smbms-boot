package cn.smbms.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.smbms.service.UserService;
import cn.smbms.tools.Constants;

@Controller
public class LoginController {
	private Logger logger = Logger.getLogger(LoginController.class);

	@Resource
	private UserService userService;

	@RequestMapping(value = {"", "/", "/login.html"})
	public String login() {
		logger.debug("LoginController welcome SMBMS==================");
		return "login";
	}

	@RequestMapping(value = "/dologin.html", method = RequestMethod.POST)
	public String doLogin(@RequestParam String userCode, @RequestParam String userPassword, HttpServletRequest request,
			HttpSession session, Model model) throws Exception {
		logger.debug("doLogin====================================");

		Subject subject = SecurityUtils.getSubject();

		UsernamePasswordToken token = new UsernamePasswordToken(userCode, userPassword);

		try {
			subject.login(token);
		} catch (UnknownAccountException e) {
			System.out.println("账号不存在");
		} catch (IncorrectCredentialsException e) {
			model.addAttribute("error", "密码错误");
			return "error";
		}

		Object user = subject.getPrincipal();

		if (null != user) {
			session.setAttribute(Constants.USER_SESSION, user);
		}
		
		model.addAttribute("contentPage", "frame");

		return "main";
	}

	@RequestMapping(value = "/logout.html")
	public String logout(HttpSession session) {
		// 清除session
		session.removeAttribute(Constants.USER_SESSION);
		return "redirect:login.html";
	}

	@RequestMapping(value = "/sys/main.html")
	public String main() {
		return "frame";
	}

	@RequestMapping(value = "/syserror.html")
	public String sysError() {
		return "syserror";
	}
}
