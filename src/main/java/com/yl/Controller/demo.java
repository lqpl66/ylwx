package com.yl.Controller;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yl.Service.InitiMenuService;
import com.yl.bean.WxAuth;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("/demo")
public class demo {
	DateFormat df = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");

	@SuppressWarnings("unused")
	@RequestMapping(value = "/main", method = RequestMethod.GET)
	public String get(HttpServletRequest request, HttpServletResponse response) {
		// String scenicId = request.getParameter("scenicId");
		// String openid = request.getParameter("openid");
		// System.out.println(scenicId);
		// System.out.println(openid);
		String wxAuth = "";
		WxAuth wa = new WxAuth();
		Cookie[] cookies = (Cookie[]) request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("access_token")) {
					wa.setAccess_token(cookie.getValue());
				}
				if (cookie.getName().equals("refresh_token")) {
					wa.setRefresh_token(cookie.getValue());
				}
				if (cookie.getName().equals("openid")) {
					wa.setOpenid(cookie.getValue());
				}
				if (cookie.getName().equals("expires_in")) {
					wa.setExpires_in(cookie.getValue());
				}
			}
		}
		if (wa.getAccess_token() != null && !wa.getAccess_token().isEmpty()) {
			System.out.println("auth:" + wa.toString());
		} else {
			wa.setOpenid("1");
			wa.setAccess_token("2");
			wa.setRefresh_token("3");
			Calendar ca = Calendar.getInstance();
			ca.add(Calendar.HOUR_OF_DAY, 2);
			wa.setExpires_in(df.format(ca.getTime()));
		}
		SetCookie("openid", wa.getOpenid(), response);
		SetCookie("access_token", wa.getAccess_token(), response);
		SetCookie("refresh_token", wa.getRefresh_token(), response);
		SetCookie("expires_in", wa.getExpires_in(), response);
		return "index";
	}

	public void SetCookie(String name, String value, HttpServletResponse response) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(60 * 60 * 24 * 30);
		response.addCookie(cookie);
	}

	@ResponseBody
	@RequestMapping(value = "/create", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public String createMenu(HttpServletRequest request, HttpServletResponse response) {
		JSONObject m = JSONObject.fromObject(InitiMenuService.getMenu());
		String f = m.toString();
		int s = InitiMenuService.createMenu(f);
		m.put("status", s);
		System.out.println("手动请求菜单：" + m);
		return m.toString();
	}

	@RequestMapping(value = "/getGuideids", method = RequestMethod.GET)
	public String getGuideids(HttpServletRequest request, HttpServletResponse response, ModelMap params) {
		String url = "guideIds";
		int key0 = 0;
		try {
			File file = new File("F:/dev_serial_v2017.bin");
			FileInputStream is = new FileInputStream(file);
			byte[] b = new byte[4];
			is.read(b);
			key0 = bytesToInt(b, 0);
			System.out.println("读取成功：" + key0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		params.put("guideIds", key0);
		return url;
	}

	public static int bytesToInt(byte[] src, int offset) {
		int value;
		value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8) | ((src[offset + 2] & 0xFF) << 16)
				| ((src[offset + 3] & 0xFF) << 24));
		return value;
	}
}
