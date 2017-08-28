package com.yl.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.yl.Service.InitiMenuService;
import com.yl.Service.InitiServer;
import com.yl.Service.MessageService;
import com.yl.Utils.XmlToMap;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/wxAuth")
public class WxAuthController {
	public static InitiServer initiServer;
	private Logger log = Logger.getLogger(WxAuthController.class);
	@Autowired
	private MessageService messageService;

	/**
	 * 
	 * @description 授权处理
	 * @param type:
	 *            1:二维码扫描调转；2：公众号菜单URL事件调转
	 * @return
	 */
	@RequestMapping(value = "/main", method = { RequestMethod.GET, RequestMethod.POST })
	public void get(HttpServletRequest request, HttpServletResponse response) {
		String type = request.getParameter("type");
		String  url = request.getScheme() + "://" + request.getServerName() + ":"+ request.getServerPort()+"/ylwx" ;
		if(type.equals("1")){
			String scenicId = request.getParameter("scenicId");
			url  = url +"/order/main?scenicId="+scenicId; 
		}else if(type.equals("1")){
			url  = url +"/order/orderGuideList"; 
		}
		 
	}
}
