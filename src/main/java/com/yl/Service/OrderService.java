package com.yl.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.yl.Controller.OrderController;
import com.yl.Utils.CodeUtils;
import com.yl.Utils.GetProperties;
import com.yl.bean.OrderGuide;
import com.yl.bean.WxUser;
import com.yl.bean.Admin;
import com.yl.bean.Order;
import com.yl.mapper.OrderMapper;
import com.yl.mapper.WxScenicMapper;
import com.yl.mapper.WxUserMapper;
import com.yl.Wechat.WechatConfig;
import com.yl.Wechat.WeChatUtil;
import net.sf.json.JSONObject;

@Service
public class OrderService {
	@Autowired
	private WxScenicMapper wxScenicMapper;
	@Autowired
	private WxUserMapper wxUserMapper;
	@Autowired
	private ScenicService scenicService;
	@Autowired
	private OrderMapper orderMapper;
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Logger log = Logger.getLogger(OrderController.class);

	/*
	 * type 1:页面初加载；2：ajax异步回调
	 */
	public JSONObject getOrderGuideList(Map<String, Object> map, String type) {
		List<OrderGuide> list = wxScenicMapper.getOrderGuide(map);
		JSONObject result = new JSONObject();
		boolean flag = false;
		if (list.isEmpty()) {
			if (type.equals("1")) {
				result.put("message", "暂无租用记录");
			} else {
				result.put("code", "error");
				result.put("message", "已无更多租用记录");
			}
		} else {
			flag = true;
			result.put("code", "success");
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
			result.put("list", list);
		}
		result.put("flag", flag);
		return result;
	}

	public String getOrderNo(String userCode) {
		String orderNo = null;
		String orderNoby = null;
		Map<String, Object> map = new HashMap<String, Object>();
		do {
			orderNo = CodeUtils.getorderCode(userCode.substring(userCode.length() - 6));
			map.clear();
			map.put("orderNo", orderNo);
			orderNoby = orderMapper.getOrder(map);
		} while (orderNo == orderNoby);
		return orderNo;
	}

	public Order saveOrder(WxUser wxUser, String tradeNo, Integer status, String remark, Integer orderType,
			BigDecimal totalPrice) {
		Order order = new Order();
		String addTime = df.format(new Date());
		String orderNo = "";
		if (wxUser != null) {
			order.setOpenID(wxUser.getOpenID());
			orderNo = getOrderNo(wxUser.getOpenCode());
		} else {
			order.setOpenID("0");
			orderNo = getOrderNo(CodeUtils.getCode(6));
		}
		order.setOrderNo(orderNo);
		order.setIsDel(0);
		order.setTradeNo(tradeNo);
		order.setOrderType(orderType);
		order.setRemark(remark);
		order.setStatus(status);
		order.setUserId(0);
		order.setAddTime(addTime);
		order.setPaymentAmount(totalPrice);
		BigDecimal val = new BigDecimal("0");
		order.setGoodsAmount(val);
		order.setDeductionAmount(val);
		order.setPostageAmount(val);
		order.setPayableAmount(val);
		orderMapper.saveOrder(order);
		return order;
	}

	/*
	 * operateType 4 景区人员 5 微信公众号用户 6 现金用户
	 */
	public void saveOrderlog(String orderNo, String openid, Integer status, Integer operateType, Integer operateId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("orderNo", orderNo);
		map.put("operateId", operateId);
		map.put("openID", openid);
		map.put("status", status);
		map.put("operateTime", df.format(new Date()));
		map.put("operateType", operateType);
		orderMapper.saveOrderLog(map);
	}

	/*
	 * 订单完结状态修改 (用于景区人员退还)
	 */
	public void updateOrderStatus(String orderNo, String openid, Integer status, Integer operateId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.clear();
		map.put("userId", 0);
		map.put("status", status);
		map.put("orderNo", orderNo);
		if (status == 10) {// 订单完结，添加完结时间
			map.put("endTime", df.format(new Date()));
		}
		map.put("openID", openid);
		orderMapper.updateOrder(map);
		// 订单操作记录
		saveOrderlog(orderNo, openid, status, 4, operateId);
	}

	// 导览笔附属表
	public void saveGuideOrder(WxUser wxUser, String orderNo, Integer scenicId, Integer guideNum, BigDecimal price,
			BigDecimal usePrice) {
		Map<String, Object> guideOrder = new HashMap<String, Object>();
		guideOrder.put("guideNum", guideNum);
		guideOrder.put("guideUseNum", 0);
		guideOrder.put("orderNo", orderNo);
		guideOrder.put("status", 1);
		guideOrder.put("scenicId", scenicId);
		guideOrder.put("price", price);
		guideOrder.put("usePrice", usePrice);
		orderMapper.saveGuideOrder(guideOrder);
	}

	// 微信回调地址的公共方法（导览笔消费）
	/*
	 * type 1 消费
	 * 
	 * paymentType 1微信 2 支付宝 3 游乐币 4现金5 服务号微信支付
	 * 
	 * expenseType 1 充值 2提现 3 消费 4 收入 5退款 6 押金退还 7 部分退还 8 全部退还
	 */

	@Transactional
	public void orderWeChatPayNotify(HttpServletRequest request, HttpServletResponse response, Integer paymentType,
			Integer expenseType) {
		PrintWriter printWriter = null;
		HashMap<String, String> param1 = new HashMap<String, String>();
		param1.put("return_code", "FAIL");
		param1.put("return_msg", "fail");
		String trade_no = null;
		String tradeNo = null;
		try {
			InputStream inStream = request.getInputStream();
			ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = inStream.read(buffer)) != -1) {
				outSteam.write(buffer, 0, len);
			}
			outSteam.close();
			inStream.close();
			String result = new String(outSteam.toByteArray(), "utf-8");// 获取微信调用我们notify_url的返回信息
			Map<String, String> param = WeChatUtil.getInfoByXml(result);
			printWriter = response.getWriter();
			if (param != null && !param.isEmpty()) {
				Map<String, Object> map = new HashMap<String, Object>();
				tradeNo = param.get("out_trade_no");
				BigDecimal total_fee = new BigDecimal(param.get("total_fee"));
				String app_id = param.get("appid");
				String seller_id = param.get("mch_id");
				String openid = param.get("openid");
				trade_no = param.get("transaction_id");
				if (param.get("result_code").equals("SUCCESS")) {
					map.put("tradeNo", tradeNo);
					map.put("openID", openid);
					Order order = orderMapper.getOrderDetails(map);
					if (order != null) {
						map.clear();
						map.put("openID", openid);
						WxUser wu = wxUserMapper.getWxUser(map);
						if (checktradeNo(total_fee.divide(new BigDecimal(100)), order, app_id, seller_id, paymentType)
								&& wu != null) {
							if (order.getStatus() == 1) {
								// 查询订单附属表， 获取景区id
								map.put("orderNo", order.getOrderNo());
								map.put("openID", openid);// 订单详情
								OrderGuide list = wxScenicMapper.getOrderGuideInfo(map);
								// 使用费流水
								paySuccess(wu, trade_no, tradeNo,
										list.getUsePrice().multiply(new BigDecimal(list.getGuideNum())),
										order.getOrderNo(), paymentType, 10, "1", list.getScenicId(), 0, null);
								// 押金流水
								paySuccess(wu, trade_no, tradeNo,
										list.getPrice().multiply(new BigDecimal(list.getGuideNum())),
										order.getOrderNo(), paymentType, 9, "1", list.getScenicId(), 0, "1");
//								paySuccess(wu, trade_no, tradeNo, order.getPaymentAmount(), order.getOrderNo(),
//										paymentType, expenseType, "1", list.getScenicId(), 0);
							}
							log.info("微信服务号订单支付成功：" + param.toString());
							param1.put("return_code", "SUCCESS");
							param1.put("return_msg", "OK");
						}
					}
				} else {
					log.error("微信服务号订单支付失败：" + param.toString());
				}
			}
		} catch (Exception e) {
			log.error("alipay notify error :", e);
			// 失败发送邮箱
			if (param1.get("return_code").equals("FAIL")) {
				// Mail mail = new Mail();
				// mail.setHost(MailConfig.host); // 设置邮件服务器,如果不用163的,自己找找看相关的
				// mail.setSender(MailConfig.sender);
				// mail.setReceiver(MailConfig.receiver); // 接收人
				// mail.setUsername(MailConfig.sender); // 登录账号,一般都是和邮箱名一样吧
				// mail.setPassword(MailConfig.password); // 发件人邮箱的登录密码
				// mail.setSubject("微信回调接口异常");
				// if (type.equals("2")) {// 充值失败
				// mail.setMessage(" 充值失败：第三方交易流水号（trade_no）:" + trade_no +
				// ";平台合并订单号（tradeNo）" + tradeNo);
				// } else {// 消费
				// mail.setMessage(" 消费失败：第三方交易流水号（trade_no）:" + trade_no +
				// ";平台合并订单号（tradeNo）" + tradeNo);
				// }
				// MailUtil.send(mail);
			}
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			printWriter.close();
		} finally {
			if (printWriter != null) {
				String s = WeChatUtil.creatXml(param1);
				System.out.println(s);
				printWriter.print(s);
			}
		}
	}

	// paymentType 1:微信；2：支付宝
	public boolean checktradeNo(BigDecimal total_amount, Order order, String app_id, String seller_id,
			Integer paymentType) {
		boolean flag = false;
		String appID = WechatConfig.AppId;
		String sellerID = WechatConfig.MchId;
		if (total_amount.compareTo(order.getPaymentAmount()) == 0 && appID.equals(app_id)
				&& sellerID.equals(seller_id)) {
			flag = true;
		}
		return flag;
	}

	/**
	 * 
	 * @description
	 * @param expenseType
	 *            1 充值 2提现 3 消费 4 收入 5退款 6 押金退还 7 部分退还 8 全部退还 9 押金消费 10 使用费消费 11
	 *            使用费退还（退款）
	 * @param userType
	 *            1 微信服务号用户2 游乐APP用户 3现金用户
	 * @param paymentType
	 *            1微信 2 支付宝 3 游乐币 4现金5 服务号微信支付
	 * @param type
	 *            1 微信服务号购买 2现金租用
	 * @param operateType
	 *            1 游乐app用户 2 商家用户 3 平台管理者 4 景区用户 5 微信公众号用户1 7 微信公众号用户2
	 * @param payType
	 *            1:支付成功；
	 * @return
	 */
	public JSONObject paySuccess(WxUser userinfo, String serialNo, String tradeNo, BigDecimal paymentAmount,
			String orderNo, Integer paymentType, Integer expenseType, String type, Integer scenicId, Integer operateId,
			String payType) {
		JSONObject result = new JSONObject();
		paymentAmount = paymentAmount.setScale(2, BigDecimal.ROUND_HALF_DOWN);
		Map<String, Object> map = new HashMap<String, Object>();
		// try {
		// 用户消费流水
		map.clear();
		String expenseGuideNo = "";
		map.put("serialNo", serialNo);
		map.put("userId", 0);
		map.put("orderNo", orderNo);
		if (type.equals("1")) {
			expenseGuideNo = CodeUtils
					.gettransactionFlowCode(userinfo.getOpenCode().substring(userinfo.getOpenCode().length() - 6));
			map.put("expenseGuideNo", expenseGuideNo);
			map.put("openID", userinfo.getOpenID());
			map.put("userType", 5);// 服务号用户
			map.put("paymentType", 5);// 服务号微信支付
		} else {
			expenseGuideNo = CodeUtils.gettransactionFlowCode(CodeUtils.getCode(6));
			map.put("expenseGuideNo", expenseGuideNo);
			map.put("openID", "0");
			map.put("userType", 3);// 现金用户
			map.put("paymentType", 4);// 现金支付
		}
		map.put("scenicId", scenicId);
		map.put("operateId", operateId);
		map.put("expenseType", expenseType);
		if (expenseType != 3 && expenseType != 9 && expenseType != 10) {
			map.put("paymentAmount", new BigDecimal("-" + paymentAmount));
		} else {
			map.put("paymentAmount", paymentAmount);
		}
		map.put("addTime", df.format(new Date()));
		map.put("refundReasonId", 0);
		map.put("deductionPrice", new BigDecimal("0.00"));
		if (expenseType == 9) {
			map.put("remark", "导览笔押金消费");
		}
		if (expenseType == 10) {
			map.put("remark", "导览笔使用费消费");
		}
		orderMapper.saveExpenseGuidelog(map);// 导览笔流水
		map.clear();
		map.put("parentId", -1);
		map.put("adminType", 1);
		Admin ad = orderMapper.getAdmin(map);
		String expenseSystemNo = CodeUtils
				.gettransactionFlowCode(ad.getAdminCode().substring(ad.getAdminCode().length() - 6));
		map.put("expenseSystemNo", expenseSystemNo);
		map.put("expenseNo", expenseGuideNo);
		map.put("adminId", ad.getId());
		if (expenseType == 5 || expenseType == 6 || expenseType == 8 || expenseType == 11) {
			paymentAmount = paymentAmount.multiply(new BigDecimal("-1"));
		}
		map.put("amount", paymentAmount);
		map.put("operateType", 7);// 微信公众号用户
		map.put("addTime", df.format(new Date()));
		orderMapper.saveExpenseSystemlog(map);
		if (payType != null && !payType.isEmpty() && payType.equals("1")) {// 消费
			// 订单状态改为支付完成
			map.clear();
			map.put("userId", 0);
//			map.put("expenseId", expenseGuideNo);
			map.put("status", 4);
			map.put("orderNo", orderNo);
			String openID = "0";
			if (type.equals("1")) {
				openID = userinfo.getOpenID();
			} else {
				openID = "0";
			}
			map.put("openID", openID);
			orderMapper.updateOrder(map);
			// 订单操作记录
			saveOrderlog(orderNo, openID, 4, 5, operateId);
		}
		result.put("code", "0001");
		return result;
	}

	/**
	 * 
	 * @description 申请退款和退还
	 * @param expenseType
	 *            1 充值 2提现 3 消费 4 收入 5退款 6 押金退还 7 导览笔损坏扣除 8 全部退还9 押金消费 10 使用费消费
	 *            11 使用费退还（退款）
	 * @param userType
	 *            1 微信服务号用户2 游乐APP用户 3现金用户
	 * @param paymentType
	 *            1微信 2 支付宝 3 游乐币 4现金5 服务号微信支付
	 * @param type
	 *            1 微信服务号购买 2现金租用
	 * @param operateType
	 *            1 游乐app用户 2 商家用户 3 平台管理者 4 景区用户 5 微信公众号用户1 7 微信公众号用户2
	 * @param refundReasonId
	 *            退还原因用于全部退还
	 * @param deductionPrice
	 *            扣除金额
	 * @param rebackType
	 *            1:退款成功 ;
	 * @return
	 */
	public JSONObject refundSuccess(WxUser userinfo, String serialNo, String tradeNo, String refundNo,
			BigDecimal paymentAmount, String orderNo, Integer paymentType, Integer expenseType, String type,
			Integer scenicId, Integer operateId, Integer refundReasonId, BigDecimal deductionPrice, String rebackType) {
		JSONObject result = new JSONObject();
		paymentAmount = paymentAmount.setScale(2, BigDecimal.ROUND_HALF_DOWN);
		Map<String, Object> map = new HashMap<String, Object>();
		// 用户消费流水
		map.clear();
		String expenseGuideNo = "";
		map.put("serialNo", serialNo);
		map.put("userId", 0);
		if (type.equals("1")) {
			expenseGuideNo = CodeUtils
					.gettransactionFlowCode(userinfo.getOpenCode().substring(userinfo.getOpenCode().length() - 6));
			map.put("expenseGuideNo", expenseGuideNo);
			map.put("openID", userinfo.getOpenID());
			map.put("userType", 5);
			map.put("paymentType", 5);
		} else {
			expenseGuideNo = CodeUtils.gettransactionFlowCode(CodeUtils.getCode(6));
			map.put("expenseGuideNo", expenseGuideNo);
			map.put("openID", "0");
			map.put("userType", 3);
			map.put("paymentType", 4);
		}
		if (expenseType == 5 || expenseType == 6 || expenseType == 8 || expenseType == 11) {
			map.put("paymentAmount", new BigDecimal("-" + paymentAmount));
		} else {
			map.put("paymentAmount", paymentAmount);
		}
		map.put("orderNo", orderNo);
		map.put("refundNo", refundNo);
		if (expenseType == 5) {
			map.put("remark", "导览笔退款");
		}
		if (expenseType == 6) {
			map.put("remark", "导览笔押金退还");
		}
		if (expenseType == 7) {
			map.put("remark", "导览笔损坏扣除");
		}
		if (expenseType == 8) {
			map.put("remark", "导览笔全部退还");
		}
		if (expenseType == 11) {
			map.put("remark", "使用费退还（退款）");
		}
		map.put("scenicId", scenicId);
		map.put("operateId", operateId);
		map.put("refundReasonId", refundReasonId);
		map.put("deductionPrice", deductionPrice);
		map.put("expenseType", expenseType);
		map.put("addTime", df.format(new Date()));
		orderMapper.saveExpenseGuidelog(map);// 导览笔消费流水
		map.clear();
		map.put("parentId", -1);
		map.put("adminType", 1);
		Admin ad = orderMapper.getAdmin(map);
		String expenseSystemNo = CodeUtils
				.gettransactionFlowCode(ad.getAdminCode().substring(ad.getAdminCode().length() - 6));
		map.put("expenseSystemNo", expenseSystemNo);
		map.put("expenseNo", expenseGuideNo);
		map.put("adminId", ad.getId());
		if (expenseType == 5 || expenseType == 6 || expenseType == 8 || expenseType == 11) {
			paymentAmount = paymentAmount.multiply(new BigDecimal("-1"));
		}
		map.put("amount", paymentAmount);
		map.put("operateType", 7);// 微信公众号用户
		map.put("addTime", df.format(new Date()));
		orderMapper.saveExpenseSystemlog(map);
		if (rebackType != null && !rebackType.isEmpty() && rebackType.equals("1")) {// 退款
			// 订单状态改为同意退款
			map.clear();
			map.put("userId", 0);
			map.put("status", 8);
			map.put("endTime", df.format(new Date()));
			map.put("orderNo", orderNo);
			String openID = "0";
			if (type.equals("1")) {
				openID = userinfo.getOpenID();
			} else {
				openID = "0";
			}
			map.put("openID", openID);
			orderMapper.updateOrder(map);
			// 订单附属表改为退款状态
			scenicService.updateGuideExpand(orderNo, null, 4);
			// 订单操作记录
			saveOrderlog(orderNo, openID, 8, 4, operateId);
		}
		result.put("code", "0001");
		return result;
	}
}
