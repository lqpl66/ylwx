package com.yl.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mchange.v2.codegen.CodegenUtils;
import com.yl.Utils.CodeUtils;
import com.yl.Utils.GetProperties;
import com.yl.Utils.MapToXml;
import com.yl.bean.Scenic;
import com.yl.bean.WxUser;
import com.yl.mapper.WxScenicMapper;
import com.yl.mapper.WxUserMapper;

/*
 * 消息处理
 */
@Service
public class MessageService {
	@Autowired
	private WxUserMapper wxUserMapper;
	@Autowired
	private WxScenicMapper wxScenicMapper;
	// 景区路径回显
	private String scenicImgUrl = GetProperties.getscenicImgUrl();
	private String serverUrl = GetProperties.getServerUrl();
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public String getAutoReply(Map<String, String> map) {
		Map<String, Object> hasmap = new HashMap<String, Object>();
		String messageInfo = "";
		Map<String, String> messageInfomap = new HashMap<String, String>();
		Map<String, String> UrlInfomap = new HashMap<String, String>();
		// 关注公众号,保存openID
		String openId = map.get("FromUserName");
		System.out.println(map.toString());
		String addTime = df.format(new Date());
		String MsgType = map.get("MsgType");
		if (MsgType.equals("event")) {// 事件回复
			messageInfomap.put("ToUserName", openId);
			messageInfomap.put("FromUserName", map.get("ToUserName"));
			long longTime1 = System.currentTimeMillis();
			messageInfomap.put("CreateTime", String.valueOf(longTime1 / 1000));
			String Event = map.get("Event");
			switch (Event) {
			case "subscribe":
				hasmap.put("openID", openId);
				hasmap.put("addTime", addTime);
				WxUser wU = wxUserMapper.getWxUser(hasmap);
				if (wU == null) {
					hasmap.put("openCode", CodeUtils.getuserCode());
					wxUserMapper.addWxUser(hasmap);
				}
				if (map.containsKey("Ticket")) {// 扫描未关注的带参数二维码事件
					System.out.println("扫描未关注的带参数二维码事件:" + map.toString());
					String scenicId_str = map.get("EventKey").substring(8);
					messageInfomap.put("MsgType", "news");
					messageInfomap.put("ArticleCount", "1");
					Integer scenicId = Integer.valueOf(scenicId_str);
					hasmap.clear();
					hasmap.put("scenicId", scenicId);
					Scenic s = wxScenicMapper.getScenic(hasmap);
					if (s != null) {
						if (s.getCoverImg() != null && s.getCoverImg() != "") {
							s.setCoverImg(scenicImgUrl + s.getFileCode() + "/" + s.getCoverImg());
						}
					}
					// Map<String, String>UrlInfomap = new HashMap<String,
					// String>();
					String Url = serverUrl + "/order/main?scenicId=" + s.getScenicId() + "&openid=" + openId;
					UrlInfomap.put("Title", "点击这里进行选择租用");
					UrlInfomap.put("Description", "欢迎使用佑途游乐语音导览笔");
					UrlInfomap.put("PicUrl", s.getCoverImg());
					UrlInfomap.put("Url", Url);
					messageInfomap.put("Articles", "");
					messageInfo = MapToXml.getMapToXml(messageInfomap, UrlInfomap, "Articles");
				} else {
					// 回复文本消息
					messageInfomap.put("MsgType", "text");
					messageInfomap.put("Content", "欢迎来到游乐");
					messageInfo = MapToXml.getMapToXml(messageInfomap, null, null);
				}
				break;
			case "SCAN":// 扫描已关注的带参数二维码事件
				System.out.println("扫描已关注的带参数二维码事件:" + map.toString());
				String scenicId_str = map.get("EventKey");
				messageInfomap.put("MsgType", "news");
				messageInfomap.put("ArticleCount", "1");
				Integer scenicId = Integer.valueOf(scenicId_str);
				hasmap.clear();
				hasmap.put("scenicId", scenicId);
				Scenic s = wxScenicMapper.getScenic(hasmap);
				if (s != null) {
					if (s.getCoverImg() != null && s.getCoverImg() != "") {
						s.setCoverImg(scenicImgUrl + s.getFileCode() + "/" + s.getCoverImg());
					}
				}
				String Url = serverUrl + "/order/main?scenicId=" + s.getScenicId() + "&openid=" + openId;
				UrlInfomap.put("Title", "点击这里进行选择租用");
				UrlInfomap.put("Description", "欢迎使用佑途游乐语音导览笔");
				UrlInfomap.put("PicUrl", s.getCoverImg());
				UrlInfomap.put("Url", Url);
				messageInfomap.put("Articles", "");
				messageInfo = MapToXml.getMapToXml(messageInfomap, UrlInfomap, "Articles");
				break;
			case "CLICK":// 菜单点击事件
				// messageInfomap.put("ToUserName", openId);
				// messageInfomap.put("FromUserName", map.get("ToUserName"));
				// long longTime1 = System.currentTimeMillis();
				// messageInfomap.put("CreateTime", String.valueOf(longTime1 /
				// 1000));
				if (map.get("EventKey").equals("how_rent")) {// 如何租用
					messageInfomap.put("MsgType", "text");
					messageInfomap.put("Content",
							"租用导览笔方式：\n" + "1.扫描游乐景区二维码，进入租用界面，选择数量，" + "支付完成后向景区工作人员出示订单码，领取导览笔；\n"
									+ "2.打开游乐APP，找到导览笔租用入口选择将要去游玩的景区，选择租用数量，完成支付，向景区工作人员出示订单码，领取导览笔；\n"
									+ "提示：使用导览笔退还至景区工作人员后，押金将自动退还；");
					messageInfo = MapToXml.getMapToXml(messageInfomap, null, null);
				} else if (map.get("EventKey").equals("download_app")) {
					// vbyH7Rd0N5XEY20vWYQGgYMqVthATa7Ym9Wl8kzRf8OWcVlM-d__cNZYSB-kHGct
					//OAKYbJygl7pKuQXE9Tcnq4TaR70pWVMMHseSqiwzHAs
					messageInfomap.put("MsgType", "image");
					messageInfomap.put("MediaId", "OAKYbJygl7pKuQXE9Tcnq4TaR70pWVMMHseSqiwzHAs");
					messageInfo = MapToXml.getMapToXml(messageInfomap, null, "Image");
				} else if (map.get("EventKey").equals("cooperation")) {
					// 请联系客服邮箱：service@uto168.com
					messageInfomap.put("MsgType", "text");
					messageInfomap.put("Content", "请联系客服邮箱：service@uto168.com");
					messageInfo = MapToXml.getMapToXml(messageInfomap, null, null);
				} else if (map.get("EventKey").equals("rentLog")) {
					messageInfomap.put("MsgType", "news");
					messageInfomap.put("ArticleCount", "1");
					UrlInfomap.clear();
					Url = serverUrl + "/order/orderGuideList?openid=" + openId;
					UrlInfomap.put("Title", "点击此处查看您的租用记录");
					UrlInfomap.put("Description", "");
					UrlInfomap.put("PicUrl", "");
					UrlInfomap.put("Url", Url);
					messageInfomap.put("Articles", "");
					messageInfo = MapToXml.getMapToXml(messageInfomap, UrlInfomap, "Articles");
				}
				break;
			default:
				break;
			}
		} else if (MsgType.equals("text") || MsgType.equals("image") || MsgType.equals("voice")
				|| MsgType.equals("video") || MsgType.equals("shortvideo") || MsgType.equals("location")
				|| MsgType.equals("link")) {// 消息回复
			if (MsgType.equals("text")) {// 文本回复

			}
		}
		// switch (MsgType) {
		// case "event":
		//
		// break;
		// case "text":
		// break;
		// default:
		// break;
		// }
		return messageInfo;
	}

}
