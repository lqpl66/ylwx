package com.yl.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yl.Service.InitiMenuService;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("/comm")
public class CommonController {

	@RequestMapping(value = "/redirect", method = {RequestMethod.GET,RequestMethod.POST})
	public String get(HttpServletRequest request, HttpServletResponse response) {
		return "redirect";
	}

	@ResponseBody
	@RequestMapping(value = "/create", method = RequestMethod.GET,produces = "application/json; charset=utf-8")
	public String createMenu(HttpServletRequest request, HttpServletResponse response) {
		JSONObject m = JSONObject.fromObject(InitiMenuService.getMenu());
		String f = m.toString();
		int s =InitiMenuService.createMenu(f);
		m.put("status", s);
		System.out.println("手动请求菜单："+m);
		return m.toString();
	}

}
