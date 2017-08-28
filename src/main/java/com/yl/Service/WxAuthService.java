package com.yl.Service;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import org.apache.http.cookie.Cookie;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.yl.Http.HttpsUtils;
import com.yl.Utils.CodeUtils;
import com.yl.bean.WxAuth;
import com.yl.bean.WxUser;
import com.yl.mapper.WxUserMapper;

import net.sf.json.JSONObject;

/**
 * 
 * @author Administrator 微信授权流程
 */
@Service
public class WxAuthService {
	@Autowired
	private WxUserMapper wxUserMapper;

	private Logger log = Logger.getLogger(WxAuthService.class);
	DateFormat df = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");

	/**
	 * 
	 * @description openId
	 * @param
	 * @return
	 */
	public String getOpenId(HttpServletRequest request, HttpServletResponse response, String code) {
		Cookie[] cookies = (Cookie[]) request.getCookies();
		String openId = "";
		WxAuth wa = new WxAuth();
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
		System.out.println(wa.toString());
		if (wa.getAccess_token() != null && !wa.getAccess_token().isEmpty()) {
			// 校验授权是否有效
			Boolean flag = HttpsUtils.getCheck(wa.getAccess_token(), wa.getOpenid());
			if (!flag) {// 无效
				// 刷新
				JSONObject js = HttpsUtils.getRefresh_token(wa.getRefresh_token(), wa.getOpenid());
				if (!js.containsKey("errcode")) {// 刷新成功
					if (!js.isNullObject() && !js.isEmpty()) {
						wa.setOpenid(js.getString("openid"));
						wa.setAccess_token(js.getString("access_token"));
						wa.setRefresh_token(js.getString("refresh_token"));
						Calendar ca = Calendar.getInstance();
						ca.add(Calendar.HOUR_OF_DAY, 2);
						wa.setExpires_in(df.format(ca.getTime()));
					}
				} else {// 刷新失败，重新获取
					System.out.println("刷新失败，重新获取：" + wa.toString());
					wa = getAuth(code, wa);
				}
			}
		} else {// 授权
			System.out.println("cookie为空：" + wa.toString());
			wa = getAuth(code, wa);
		}
		if (wa.getOpenid() != null && !wa.getOpenid().isEmpty()) {
			SetCookie("openid", wa.getOpenid(), response);
			SetCookie("access_token", wa.getAccess_token(), response);
			SetCookie("refresh_token", wa.getRefresh_token(), response);
			SetCookie("expires_in", wa.getExpires_in(), response);
			openId = wa.getOpenid();
			System.out.println("最终设置wa:" + wa.toString());
		}
		return openId;
	}

	/**
	 * 
	 * @description 授权
	 * @param
	 * @return
	 */
	public WxAuth getAuth(String code, WxAuth wa) {
		JSONObject json = HttpsUtils.getJsonOpenId(code);
		if (!json.isNullObject() && !json.isEmpty() && !json.containsKey("errcode")) {
			wa.setOpenid(json.getString("openid"));
			wa.setAccess_token(json.getString("access_token"));
			wa.setRefresh_token(json.getString("refresh_token"));
			Calendar ca = Calendar.getInstance();
			ca.add(Calendar.HOUR_OF_DAY, 2);
			wa.setExpires_in(df.format(ca.getTime()));
		} else {
			wa.setOpenid(null);
		}
		return wa;
	}

	public void SetCookie(String name, String value, HttpServletResponse response) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(60 * 60 * 24 * 30);
		response.addCookie(cookie);
	}

	/**
	 * 
	 * @description URL编码（utf-8）
	 * @param
	 * @return
	 */
	public static String urlEncodeUTF8(String source) {
		String result = source;
		try {
			result = java.net.URLEncoder.encode(source, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void saveWxUser(String openid) {
		Map<String, Object> map = new HashMap<String, Object>();
		// 直接绑定openId
		String addTime = df.format(new Date());
		map.put("openID", openid);
		map.put("addTime", addTime);
		WxUser wU = wxUserMapper.getWxUser(map);
		if (wU == null) {
			map.put("openCode", CodeUtils.getuserCode());
			wxUserMapper.addWxUser(map);
		}
	}

	public static void main(String args[]) {
		// System.out.println(u);
	}

}
