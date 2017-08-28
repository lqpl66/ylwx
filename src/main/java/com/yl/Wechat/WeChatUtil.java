package com.yl.Wechat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.yl.Utils.GetProperties;
import com.yl.Utils.MD5UtilWX;
import com.yl.Utils.MD5Utils;

import net.sf.json.JSONObject;

public class WeChatUtil {

	/**
	 * 返回状态码
	 */
	public static final String ReturnCode = "return_code";

	/**
	 * 返回信息
	 */
	public static final String ReturnMsg = "return_msg";

	/**
	 * 业务结果
	 */
	public static final String ResultCode = "result_code";

	/**
	 * 预支付交易会话标识
	 */
	public static final String PrepayId = "prepay_id";

	/**
	 * 得到微信预付单的返回ID
	 * 
	 * @param orderId
	 *            商户自己的订单号
	 * @param totalFee
	 *            总金额 （分）
	 * @return type
	 */
	public static Map<String, String> getPreyId(String tradeNo, Integer totalFee, Integer type, String openid) {
		HashMap<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("appid", WechatConfig.AppId);
		reqMap.put("mch_id", WechatConfig.MchId);
		reqMap.put("nonce_str", getRandomString());
		// if (type == 1) {
		reqMap.put("body", "YL_Guide_Order");
		reqMap.put("notify_url", GetProperties.getNotify_url_Wechat_guide()); // 通知地址
		// }
		reqMap.put("out_trade_no", tradeNo); // 商户系统内部的订单号,
		reqMap.put("total_fee", totalFee.toString()); // 订单总金额，单位为分
		reqMap.put("spbill_create_ip", "101.81.226.213"); // 用户端实际ip
		reqMap.put("trade_type", "JSAPI"); // 交易类型
		reqMap.put("openid", openid);// 公众号支付，openId
		String sign = getSign(reqMap);
		reqMap.put("sign", sign);
		System.out.println("sign" + sign);
		String reqStr = creatXml(reqMap);
		System.out.println(reqStr);
		String retStr = HttpClientUtil.postHttplient(WechatConfig.PrepayUrl, reqStr);
		System.out.println("retStr" + retStr);
		return getInfoByXml(retStr);
	}

	/**
	 * 关闭订单
	 * 
	 * @param orderId
	 *            商户自己的订单号
	 * @return
	 */
	// public static Map<String, String> closeOrder(String orderId){
	// Map<String, String> reqMap = new HashMap<String, String>();
	// reqMap.put("appid", WechatConfig.AppId);
	// reqMap.put("mch_id", WechatConfig.MchId);
	// reqMap.put("nonce_str", getRandomString());
	// reqMap.put("out_trade_no", orderId); //商户系统内部的订单号,
	// reqMap.put("sign", getSign(reqMap));
	//
	// String reqStr = creatXml(reqMap);
	// String retStr = HttpClientUtil.postHttplient(WechatConfig.CloseOrderUrl,
	// reqStr);
	// return getInfoByXml(retStr);
	// }

	/**
	 * 查询订单
	 * 
	 * @param orderId
	 *            商户自己的订单号
	 * @return
	 */
	// public static String getOrder(String orderId){
	// Map<String, String> reqMap = new HashMap<String, String>();
	// reqMap.put("appid", WechatConfig.AppId);
	// reqMap.put("mch_id", WechatConfig.MchId);
	// reqMap.put("nonce_str", getRandomString());
	// reqMap.put("out_trade_no", orderId); //商户系统内部的订单号,
	// reqMap.put("sign", getSign(reqMap));
	//
	// String reqStr = creatXml(reqMap);
	// String retStr = HttpClientUtil.postHttplient(WechatConfig.OrderUrl,
	// reqStr);
	// return retStr;
	// }

	/**
	 * 退款
	 * 
	 * @param orderId
	 *            商户订单号
	 * @param refundId
	 *            退款单号
	 * @param totralFee
	 *            总金额（分）
	 * @param refundFee
	 *            退款金额（分）
	 * @param opUserId
	 *            操作员ID
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, String> refund (String tradeNo, String refundNo, String totralFee, String refundFee,
			String opUserId) throws Exception {
		HashMap<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("appid", WechatConfig.AppId);
		reqMap.put("mch_id", WechatConfig.MchId);
		reqMap.put("nonce_str", getRandomString());
		reqMap.put("out_trade_no", tradeNo); // 商户系统内部的订单号,
		reqMap.put("out_refund_no", refundNo); // 商户退款单号
		reqMap.put("total_fee", totralFee); // 总金额
		reqMap.put("refund_fee", refundFee); // 退款金额
		reqMap.put("op_user_id", WechatConfig.MchId); // 操作员
		String sign = getSign(reqMap);
		reqMap.put("sign", sign);
		String reqStr = creatXml(reqMap);
		String retStr = "";
			retStr = HttpClientUtil.postHttplientNeedSSL(WechatConfig.RefundUrl, reqStr, GetProperties.refund_notify_url_guide(),
					WechatConfig.MchId);
		return getInfoByXml(retStr);
	}

	/**
	 * 退款查询
	 * 
	 * @param refundId
	 *            退款单号
	 * @return
	 */
	// public static Map<String, String> getRefundWeiInfo(String refundId){
	// Map<String, String> reqMap = new HashMap<String, String>();
	// reqMap.put("appid", WechatConfig.AppId);
	// reqMap.put("mch_id", WechatConfig.MchId);
	// reqMap.put("nonce_str", getRandomString());
	// reqMap.put("out_refund_no", refundId); //商户退款单号
	// reqMap.put("sign", getSign(reqMap));
	//
	// String reqStr = creatXml(reqMap);
	// String retStr = HttpClientUtil.postHttplient(WechatConfig.RefundQueryUrl,
	// reqStr);
	// return getInfoByXml(retStr);
	// }
	/**
	 * 传入map 生成头为XML的xml字符串，例：<xml><key>123</key></xml>
	 * 
	 * @param reqMap
	 * @return
	 */
	// public static String creatXml(Map<String, String> reqMap){
	// Set<String> set = reqMap.keySet();
	// FXmlNode rootXml = new FXmlNode();
	// rootXml.setName("xml");
	// for(String key : set){
	// rootXml.createNode(key, reqMap.get(key));
	// }
	// return rootXml.xml().toString();
	// }

	public static String creatXml(Map<String, String> map) {
		String xmlResult = "";
		StringBuffer sb = new StringBuffer();
		String[] keys = map.keySet().toArray(new String[0]);
		Arrays.sort(keys);
		// StringBuffer reqStr = new StringBuffer();
		// for (String key : keys) {
		// String v = map.get(key);
		// if (v != null && !v.equals("")) {
		// reqStr.append(key).append("=").append(v).append("&");
		// }
		// }
		sb.append("<xml>");
		for (String key : keys) {
			// System.out.println(key + "========" + map.get(key));
			String value = "<![CDATA[" + map.get(key) + "]]>";
			// String value =map.get(key) ;
			sb.append("<" + key + ">" + value + "</" + key + ">");
			System.out.println();
		}
		sb.append("</xml>");
		xmlResult = sb.toString();
		// System.out.println("sss:" + xmlResult);
		return xmlResult;
	}

	/**
	 * 得到加密值
	 * 
	 * @param map
	 * @return
	 */
	public static String getSign(HashMap<String, String> map) {
		String[] keys = map.keySet().toArray(new String[0]);
		Arrays.sort(keys);
		StringBuffer reqStr = new StringBuffer();
		for (String key : keys) {
			String v = map.get(key);
			if (v != null && !v.equals("")) {
				reqStr.append(key).append("=").append(v).append("&");
			}
		}
		reqStr.append("key").append("=").append(WechatConfig.AppSercret);
		System.out.println(reqStr.toString());
		// MD5加密
		return MD5UtilWX.MD5Encode(reqStr.toString(), "UTF-8").toUpperCase();
	}

	/**
	 * 得到10 位的时间戳 如果在JAVA上转换为时间要在后面补上三个0
	 * 
	 * @return
	 */
	public static String getTenTimes() {
		String t = new Date().getTime() + "";
		t = t.substring(0, t.length() - 3);
		return t;
	}

	/**
	 * 得到随机字符串
	 * 
	 * @param length
	 * @return
	 */
	public static String getRandomString() {
		int length = 32;
		String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < length; ++i) {
			int number = random.nextInt(62);// [0,62)
			sb.append(str.charAt(number));
		}
		return sb.toString();
	}

	/**
	 * 得到本地机器的IP
	 * 
	 * @return
	 */
	private static String getHostIp() {
		String ip = "";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ip;
	}

	/**
	 * 将XML转换为Map 验证加密算法 然后返回
	 * 
	 * @param xml
	 * @return
	 */
	// public static Map<String, String> getInfoByXml(String xml){
	// try{
	// FXmlDocument xdoc = new FXmlDocument();
	// FXmlNode nodeRoot = xdoc.formatStringToXml(xml);
	// FXmlNodes allNodes = nodeRoot.allNodes();
	// Map<String, String> map = new HashMap<String, String>();
	// for(FXmlNode fXmlNode : allNodes){
	// map.put(fXmlNode.name(), fXmlNode.text());
	// }
	// //对返回结果做校验.去除sign 字段再去加密
	// String retSign = map.get("sign");
	// map.remove("sign");
	// String rightSing = getSign(map);
	// if(rightSing.equals(retSign)){
	// return map;
	// }
	// }catch(Exception e){
	// return null;
	// }
	// return null;
	// }
	public static Map<String, String> getInfoByXml(String xml) {
		System.out.println(xml);
		HashMap<String, String> map = new HashMap<String, String>();
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(xml); // 将字符串转为XML
			Element rootElt = doc.getRootElement(); // 获取根节点
			@SuppressWarnings("unchecked")
			List<Element> list = rootElt.elements();// 获取根节点下所有节点
			for (Element element : list) { // 遍历节点
				map.put(element.getName(), element.getText()); // 节点的name为map的key，text为map的value
			}
			// 对返回结果做校验.去除sign 字段再去加密
			String retSign = map.get("sign");
			map.remove("sign");
			String rightSing = getSign(map);
			System.out.println(retSign);
			if (rightSing.equals(retSign)) {
				map.put("sign", retSign);
				return map;
			}
		} catch (DocumentException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return map;
	}

	/**
	 * 将金额转换成分
	 * 
	 * @param fee
	 *            元格式的
	 * @return 分
	 */
	public static String changeToFen(Double fee) {
		String priceStr = "";
		if (fee != null) {
			int p = (int) (fee * 100); // 价格变为分
			priceStr = Integer.toString(p);
		}
		return priceStr;
	}

	public static void main(String args[]) {
		// System.out.println(getTenTimes());

		 Map<String, String> s = getPreyId("12345611",100,
		 2,"o7lml0hDp-A7cCQQYFr1OAGTCuU8");
//		 String s = getRandomString();
//		 JSONObject data = new JSONObject();
//		 data.put("appId", ss.get("appid"));
//		 data.put("signType", "MD5");
//		 data.put("prepay_id", "prepay_id=" + ss.get("prepay_id"));
//		 data.put("nonceStr", ss.get("nonce_str"));
//		 data.put("timeStamp", WeChatUtil.getTenTimes());
//		 HashMap<String, String> d = new HashMap<String, String>();
//		 d.put("appId", ss.get("appId"));
//		 d.put("signType", ss.get("signType"));
//		 d.put("package", ss.get("prepay_id"));
//		 d.put("nonceStr", "nonceStr");
//		 d.put("timeStamp", data.optString("timeStamp"));
//		 data.put("sign", WeChatUtil.getSign(d));
		 System.out.println(s);

	}

}
