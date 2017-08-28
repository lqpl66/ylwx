package com.yl.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.yl.Service.InitiMenuService;
import com.yl.Service.InitiServer;
import com.yl.Service.MessageService;
import com.yl.Utils.XmlToMap;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("/weixinCon")
public class WeixinController {
	@Autowired
	public InitiServer initiServer;
	private Logger log = Logger.getLogger(WeixinController.class);
	@Autowired
	private MessageService messageService;

	@RequestMapping(value = "/main", method = { RequestMethod.GET, RequestMethod.POST })
	public void get(HttpServletRequest request, HttpServletResponse response) {
		boolean isGet = request.getMethod().toLowerCase().equals("get");
		System.out.println("enter main:" + request.getMethod().toLowerCase().equals("get"));
		response.setCharacterEncoding("UTF-8");
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println("异常");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (isGet) {// 服务端验证和初始化菜单
			System.out.println("enter access");
			access(request, response);
		} else {
			// 进入POST聊天处理
			System.out.println("enter post");
			// 接收消息并返回消息
			acceptMessage(request, response);
		}
	}

	private void acceptMessage(HttpServletRequest request, HttpServletResponse response) {
		// 处理接收消息
		Map<String, String> map = XmlToMap.xmlToMap(request);
		System.out.println(map);
		// String xml = MapToXml.getMapToXml(map);
		String xml = messageService.getAutoReply(map);
		System.out.println(xml);
		PrintWriter out = null;
		try {
			out = response.getWriter();
			// 返回消息
			out.write(xml);
			// out.write(xml.getBytes("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
			log.error("处理接收消息", e);
		} finally {
			out.close();
			out = null;
		}

	}

	private void access(HttpServletRequest request, HttpServletResponse response) {
		PrintWriter out = null;
		try {
			out = response.getWriter();
			// 初始化服务器
			String echostr = initiServer.getEchostr(request, response);
			System.out.println("1:" + echostr);
			if (echostr != null) {
				out.write(echostr);
			}
			// 初始化菜单
			JSONObject m = JSONObject.fromObject(InitiMenuService.getMenu());
			String f = m.toString();
			int s = InitiMenuService.createMenu(f);
			System.out.println("初始化菜单:" + s);
			log.info("初始化菜单：" + s);
		} catch (IOException e) {
			e.printStackTrace();
			log.error("初始化", e);
		} finally {
			out.close();
			out = null;
		}
	}

}
