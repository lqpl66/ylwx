package com.yl.Controller;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yl.Http.HttpsUtils;
import com.yl.Service.OrderService;
import com.yl.Service.WxAuthService;
import com.yl.Utils.CodeUtils;
import com.yl.bean.Order;
import com.yl.bean.OrderGuide;
import com.yl.bean.Scenic;
import com.yl.bean.WxUser;
import com.yl.mapper.OrderMapper;
import com.yl.mapper.WxScenicMapper;
import com.yl.mapper.WxUserMapper;
import com.yl.Wechat.WeChatUtil;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/order")
public class OrderController {
	private Logger log = Logger.getLogger(OrderController.class);
	@Autowired
	private WxScenicMapper wxScenicMapper;
	@Autowired
	private WxUserMapper wxUserMapper;
	@Autowired
	private OrderMapper orderMapper;
	@Autowired
	private OrderService orderService;
	@Autowired
	private WxAuthService wxAuthService;
	DateFormat df = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");

	@Transactional
	@RequestMapping(value = "/main", method = RequestMethod.GET)
	public String main(HttpServletRequest request, HttpServletResponse response, ModelMap params) {
		String scenicId_str = request.getParameter("scenicId");
		String openid = "";
		Map<String, Object> map = new HashMap<String, Object>();
		String url = "error";
		try {
			 if (openid == null || openid.isEmpty()) {
			 String code = request.getParameter("code");
			 String state = request.getParameter("state");
			 System.out.println("code:" + code);
			 System.out.println("state:" + state);
			 if (code != null && !code.isEmpty()) {// 获取用户的授权之后的openId
			 openid = wxAuthService.getOpenId(request, response, code);
			// openid = HttpsUtils.getOpenId(code);
			 } else {
			 return url;
			 }
			 }
//			openid = "oNEJ_v_BTqEuG1vXw4G7N_TkwCkc";
			if (scenicId_str != null && !scenicId_str.isEmpty() && !openid.isEmpty() && openid != null) {
				Integer scenicId = Integer.valueOf(scenicId_str);
				map.clear();
				map.put("scenicId", scenicId);
				map.put("type", "1");// 导览笔有效期限
				Scenic s = wxScenicMapper.getScenic(map);
				// List<ScenicGuide> sgList =
				// wxScenicMapper.getScenicGuide(map);
				params.put("title", "游乐");
				params.put("openid", openid);
				// openid綁定
				wxAuthService.saveWxUser(openid);
				if (s != null) {
					params.put("scenic", s);
					url = "orderMain";
				} else {
					params.put("message", "暂无数据");
					url = "error";
				}
			} else {
				params.put("message", "参数不全");
				url = "error";
			}
		} catch (Exception e) {
			params.put("message", "服务繁忙");
			log.error("跳转预定单页面异常：", e);
			url = "error";
		}
		return url;
	}

	// public
	/*
	 * 订单记录列表初始页面
	 */
	@RequestMapping(value = "/orderGuideList", method = RequestMethod.GET)
	public String orderGuideList(HttpServletRequest request, HttpServletResponse response, ModelMap params) {
		String openid = "";
		Map<String, Object> map = new HashMap<String, Object>();
		String url = "error";
		try {
			if (openid == null || openid.isEmpty()) {
				String code = request.getParameter("code");
				String state = request.getParameter("state");
				System.out.println("code:" + code);
				System.out.println("state:" + state);
				if (code != null && !code.isEmpty()) {// 获取用户的授权之后的openId
					// openid = HttpsUtils.getOpenId(code);
					openid = wxAuthService.getOpenId(request, response, code);
				} else {
					return url;
				}
			}
			if (!openid.isEmpty() && openid != null) {
				params.put("openid", openid);
				map.clear();
				map.put("openID", openid);
				map.put("start", 0);
				map.put("num", 2);
				JSONObject result = orderService.getOrderGuideList(map, "1");
				if (result.optBoolean("flag") == true) {
					params.put("list", result.getJSONArray("list"));
					params.put("openid", openid);
					params.put("title", "租用记录");
					url = "orderList";
				} else {
					params.put("list", "");
					params.put("title", "租用记录");
					params.put("message", "暂无数据");
					url = "orderList";
				}
			} else {
				params.put("title", "参数不全");
				params.put("message", "参数不全");
				url = "error";
			}

		} catch (Exception e) {
			params.put("title", "服务繁忙");
			params.put("message", "服务繁忙");
			log.error("跳转预定单页面异常：", e);
			url = "error";
		}
		return url;
	}

	/*
	 * 订单记录列表异步加载页面
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/orderAjaxList", method = RequestMethod.POST)
	public Map<String, Object> orderAjaxList(HttpServletRequest request, @RequestBody String jsonparam) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			JSONObject js = JSONObject.fromObject(jsonparam);
			String openid = js.optString("openid");
			Integer num = js.optInt("num");
			Integer pageNum = js.optInt("pageNum");
			if (openid != null && num != null && pageNum != null && !openid.isEmpty() && num > 0 && pageNum > 0) {
				map.clear();
				map.put("openID", openid);
				map.put("start", (pageNum - 1) * num);
				map.put("num", num);
				result = orderService.getOrderGuideList(map, "2");
			} else {
				result.put("code", "error");
				result.put("message", "参数不全");
			}
		} catch (Exception e) {
			log.error("订单记录列表异步加载：", e);
			result.put("code", "error");
			result.put("message", "服务繁忙");
		}
		return result;
	}

	@RequestMapping(value = "/orderDetails", method = RequestMethod.GET)
	public String orderDetails(HttpServletRequest request, HttpServletResponse response, ModelMap params) {
		String orderNo = request.getParameter("orderNo");
		String openid = request.getParameter("openid");
		Map<String, Object> map = new HashMap<String, Object>();
		String url = "error";
		try {
			if (orderNo != null && openid != null && !orderNo.isEmpty() && !openid.isEmpty()) {
				map.clear();
				map.put("orderNo", orderNo);
				map.put("openID", openid);// 订单详情
				List<OrderGuide> list = wxScenicMapper.getOrderGuide(map);
				params.put("title", "租用详情");
				if (list != null && !list.isEmpty()) {
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i).getStatus() == 4) {// 已支付
							list.get(i).setStatusStr("1");
						} else if (list.get(i).getStatus() == 8) {// 已退款
							list.get(i).setStatusStr("2");
						} else if (list.get(i).getStatus() == 10) {
							if (list.get(i).getGuideExpandStatus() == 2) {// 设备已认领
								list.get(i).setStatusStr("3");
							} else if (list.get(i).getGuideExpandStatus() == 3) {// 押金已退
								list.get(i).setStatusStr("4");
							}
						}
					}
					params.put("orderGuide", list.get(0));
					url = "orderDetails";
				} else {
					params.put("message", "暂无数据");
					url = "error";
				}
			} else {
				params.put("message", "参数不全");
				url = "error";
			}
		} catch (Exception e) {
			params.put("message", "服务繁忙");
			log.error("跳转订单详情页面异常：", e);
			url = "error";
		}
		return url;
	}

	/*
	 * 订单生成，并返回微信签名
	 */
	@SuppressWarnings("finally")
	@Transactional
	@ResponseBody
	@RequestMapping(value = "/orderGeneration", method = RequestMethod.POST)
	public Map<String, Object> orderGeneration(HttpServletRequest request, @RequestBody String jsonparam) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			JSONObject js = JSONObject.fromObject(jsonparam);
			String openid = js.optString("openid");
			Integer scenicId = js.optInt("scenicId");
			Integer buyNum = js.optInt("buyNum");
			JSONObject data = new JSONObject();
			if (openid != null && scenicId != null && buyNum != null && !openid.isEmpty() && scenicId > 0
					&& buyNum > 0) {
				map.clear();
				// map.put("openID", openid);
				map.put("scenicId", scenicId);
				map.put("type", "1");
				Scenic s = wxScenicMapper.getScenic(map);
				map.clear();
				map.put("openID", openid);
				WxUser wU = wxUserMapper.getWxUser(map);
				if (s != null && wU != null) {
					BigDecimal totalPrice = s.getPrice().add(s.getUsePrice()).multiply(new BigDecimal(buyNum))
							.setScale(2, BigDecimal.ROUND_UP);
					// 生成订单，订单记录表和导览笔附属表，以及微信签名
					String tradeNo = CodeUtils.gettradeNo();
					Order o = orderService.saveOrder(wU, tradeNo, 1, null, 8, totalPrice);
					orderService.saveOrderlog(o.getOrderNo(), openid, 1, 5, 0);
					orderService.saveGuideOrder(wU, o.getOrderNo(), scenicId, buyNum, s.getPrice(), s.getUsePrice());
					Map<String, String> ss = WeChatUtil.getPreyId(tradeNo, o.getPaymentAmount()
							.multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP).intValue(), null,
							openid);
					if (ss.get(WeChatUtil.ReturnCode).equals("SUCCESS")) {
						data.put("appId", ss.get("appid"));
						data.put("signType", "MD5");
						data.put("prepay_id", "prepay_id=" + ss.get("prepay_id"));
						data.put("nonceStr", ss.get("nonce_str"));
						data.put("timeStamp", WeChatUtil.getTenTimes());
						HashMap<String, String> d = new HashMap<String, String>();
						d.put("appId", data.optString("appId"));
						d.put("signType", data.optString("signType"));
						d.put("package", data.optString("prepay_id"));
						d.put("nonceStr", data.optString("nonceStr"));
						d.put("timeStamp", data.optString("timeStamp"));
						data.put("sign", WeChatUtil.getSign(d));
						result.put("orderNo", o.getOrderNo());
						result.put("openid", openid);
						result.put("data", data);
						result.put("code", "success");
						System.out.println("获取微信签名成功:"+data.toString());
						result.put("message", "获取微信签名成功！");
					} else {
						result.put("code", "error");
						result.put("message", "获取微信支付签名失败！");
					}
				} else {
					result.put("code", "error");
					result.put("message", "服务繁忙");
				}
			} else {
				result.put("code", "error");
				result.put("message", "参数不全");
			}
		} catch (Exception e) {
			log.error("订单生成加载失败：", e);
			result.put("code", "error");
			result.put("message", "服务繁忙");
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} finally {
			return result;
		}
	}

	@RequestMapping(value = "/orderSuccess", method = RequestMethod.GET)
	public String orderSuccess(HttpServletRequest request, HttpServletResponse response, ModelMap params) {
		String orderNo = request.getParameter("orderNo");
		String openid = request.getParameter("openid");
		Map<String, Object> map = new HashMap<String, Object>();
		String url = "error";
		try {
			if (orderNo != null && openid != null && !orderNo.isEmpty() && !openid.isEmpty()) {
				map.clear();
				params.put("orderNo", orderNo);
				params.put("openid", openid);// 订单详情
				params.put("title", "支付成功");
				url = "success";
			} else {
				params.put("title", "支付失败");
				params.put("message", "支付失败");
				url = "error";
			}
		} catch (Exception e) {
			params.put("message", "服务繁忙");
			log.error("跳转订单成功页面异常：", e);
			url = "error";
		}
		return url;
	}

	@RequestMapping(value = "/Wechatpay/guideNotify", method = RequestMethod.POST)
	public void orderPayNotifyGuide(HttpServletRequest request, HttpServletResponse response) {
		log.info("[/order/Wechatpay/guideNotify]");
		orderService.orderWeChatPayNotify(request, response, 5, 3);
	}

	/*
	 * 微信退款
	 */
	@SuppressWarnings("finally")
	@Transactional
	@ResponseBody
	@RequestMapping(value = "/orderRefund", method = RequestMethod.POST)
	public Map<String, Object> orderRefund(HttpServletRequest request, @RequestBody String jsonparam) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, String> re = new HashMap<String, String>();
		try {
			JSONObject js = JSONObject.fromObject(jsonparam);
			String openid = js.optString("openid");
			String orderNo = js.optString("orderNo");
			JSONObject data = new JSONObject();
			if (openid != null && orderNo != null && !openid.isEmpty() && !orderNo.isEmpty()) {
				map.clear();
				map.put("orderNo", orderNo);
				map.put("openID", openid);// 订单详情
				List<OrderGuide> list = wxScenicMapper.getOrderGuide(map);
				map.clear();
				map.put("openID", openid);
				WxUser wU = wxUserMapper.getWxUser(map);
				if (list != null && !list.isEmpty() && wU != null) {
					if (list.get(0).getStatus() == 4 && list.get(0).getGuideExpandStatus() == 1
							&& list.get(0).getGuideUseNum() == 0) {// 订单是支付状态，同时附属订单表的状态也是未使用,
						String tradeNo = list.get(0).getTradeNo();
						String total_fee = String
								.valueOf(list.get(0).getPaymentAmount().multiply(new BigDecimal("100")).intValue());
						String refundNo = orderService.getOrderNo(wU.getOpenCode());
						re = WeChatUtil.refund(tradeNo, refundNo, total_fee, total_fee, null);
						if (re.get("return_code").equals("SUCCESS")&&re.get("result_code").equals("SUCCESS")) {
							log.info("导览笔订单退款成功：" + re.toString());
//							data = orderService.refundSuccess(wU, re.get("refund_id"), tradeNo, refundNo,
//									list.get(0).getPaymentAmount(), orderNo, 5, 5, "1", list.get(0).getScenicId(), 0, 0,
//									new BigDecimal("0.00"));
							// 使用费
							BigDecimal totalUsePrice = list.get(0).getUsePrice()
									.multiply(new BigDecimal(list.get(0).getGuideNum()));
							// 押金
							BigDecimal totalPrice = list.get(0).getPrice()
									.multiply(new BigDecimal(list.get(0).getGuideNum()));
							// 使用费退款
							data = orderService.refundSuccess(wU, re.get("refund_id"), tradeNo, refundNo, totalUsePrice,
									orderNo, 5, 11, "1", list.get(0).getScenicId(), 0, 0, new BigDecimal("0.00"), null);
							// 押金退款
							data = orderService.refundSuccess(wU, re.get("refund_id"), tradeNo, refundNo, totalPrice,
									orderNo, 5, 6, "1", list.get(0).getScenicId(), 0, 0, new BigDecimal("0.00"), "1");
							
							result.put("code", "success");
							result.put("message", "退款成功");
							
						} else {
							log.info("导览笔订单退款失败：" + re.toString());
							result.put("code", "error");
							result.put("message", "服务繁忙");
						}
					} else {
						result.put("code", "error");
						result.put("message", "该订单已无法申请退款，有疑惑请咨询游乐平台");
					}
				} else {
					result.put("code", "error");
					result.put("message", "服务繁忙");
				}
			} else {
				result.put("code", "error");
				result.put("message", "服务繁忙");
			}
		} catch (Exception e) {
			log.error("订单退款失败：", e);
			result.put("code", "error");
			result.put("message", "服务繁忙");
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} finally {
			return result;
		}
	}

}
