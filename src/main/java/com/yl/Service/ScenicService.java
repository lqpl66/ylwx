package com.yl.Service;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alipay.api.domain.PaymentSchedule;
import com.sun.tools.doclint.Checker.Flag;
import com.yl.Pojo.GuideClickPOJO;
import com.yl.Utils.AESUtils;
import com.yl.Utils.CodeUtils;
import com.yl.Utils.CommonDateParseUtil;
import com.yl.Utils.Constant;
import com.yl.Utils.GetProperties;
import com.yl.Utils.InitUtils;
import com.yl.Utils.MD5Utils;
import com.yl.Wechat.WeChatUtil;
import com.yl.bean.BaseGuideFile;
import com.yl.bean.BindGuideLog;
import com.yl.bean.GuideClick;
import com.yl.bean.Order;
import com.yl.bean.OrderGuide;
import com.yl.bean.Scenic;
import com.yl.bean.ScenicAdmin;
import com.yl.bean.ScenicGuide;
import com.yl.bean.ScenicGuideFee;
import com.yl.bean.WxUser;
import com.yl.mapper.OrderMapper;
import com.yl.mapper.WxScenicMapper;
import com.yl.mapper.WxUserMapper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class ScenicService {
	@Autowired
	private WxScenicMapper wxScenicMapper;
	@Autowired
	private WxUserMapper wxUserMapper;
	@Autowired
	private OrderService orderService;
	@Autowired
	private OrderMapper orderMapper;
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
	private Logger log = Logger.getLogger(ScenicService.class);
	private static DateFormat df3 = new SimpleDateFormat("yyyyMMdd");

	/*
	 * 景区用户登录校验
	 */
	public JSONObject getScenicAdmin(JSONObject json) {
		JSONObject result = new JSONObject();
		JSONObject data = new JSONObject();
		Map<String, Object> map = new HashMap<String, Object>();
		String adminName = json.optString("adminName");
		String adminPwd = json.optString("adminPwd");
		boolean flag = false;
		if (adminName != null && adminPwd != null && !adminName.isEmpty() && !adminPwd.isEmpty()) {
			map.clear();
			map.put("adminName", adminName);
			map.put("adminPwd", MD5Utils.string2MD5(adminPwd));
			map.put("adminType", 2);
			ScenicAdmin scenicAdmin = wxScenicMapper.getScenicAdmin(map);
			if (scenicAdmin != null) {// 用户状态是否正常，
				if (scenicAdmin.getStatus() == 2) {
					result.put("code", "0003");
					result.put("message", "该用户已被冻结，请联系景区管理员");
				} else if (scenicAdmin.getParentId() == 0) {
					result.put("code", "0006");
					result.put("message", "该账号不是子管理员账号，不允许登录");
				} else {// 重置UUID,
					Calendar time = Calendar.getInstance();
					map.clear();
					map.put("modifyTime", df.format((Date) time.getTime()));
					time.add(Calendar.HOUR_OF_DAY, 8);
					String uuIDExpiry = df.format((Date) time.getTime());
					System.out.println(uuIDExpiry);
					String uuID = InitUtils.getUUID();
					map.put("adminName", adminName);
					map.put("uuIDExpiry", uuIDExpiry);
					map.put("uuID", uuID);
					map.put("modifyUser", scenicAdmin.getId());
					scenicAdmin.setUuID(uuID);
					scenicAdmin.setUuIDExpiry(uuIDExpiry);
					wxScenicMapper.updateScenicAdmin(map);
					data.put("uuID", uuID);
					data.put("scenicId", scenicAdmin.getFk());
					result.put("data", data);
					result.put("code", "0001");
					result.put("message", "登录成功");
					operateLoginLog(scenicAdmin.getId(), 1, 4);
					flag = true;
				}
			} else {
				result.put("code", "0004");
				result.put("message", "用户名或密码错误");
			}
		} else {
			result.put("code", "0002");
			result.put("message", "参数不全");
		}
		result.put("flag", flag);
		return result;
	}

	/*
	 * 景区用户状态校验 type 1 :无景区子管理员账号信息 2:有子账号管理员信息
	 */
	public JSONObject getScenicAdminUUID(JSONObject json, String type) {
		JSONObject result = new JSONObject();
		Map<String, Object> map = new HashMap<String, Object>();
		String uuID = json.optString("uuID");
		boolean flag = false;
		if (uuID != null && !uuID.isEmpty()) {
			map.clear();
			map.put("uuID", uuID);
			map.put("adminType", 2);
			ScenicAdmin scenicAdmin = wxScenicMapper.getScenicAdmin(map);
			if (scenicAdmin != null) {// 用户状态是否正常，
				if (scenicAdmin.getStatus() == 2) {
					result.put("code", "0003");
					result.put("message", "该用户已被冻结，请联系景区管理员");
				} else if (scenicAdmin.getParentId() == 0) {
					result.put("code", "0006");
					result.put("message", "该账号不是子管理员账号，不允许登录");
				} else {// 是否超時
					Calendar time = Calendar.getInstance();
					Date nowdate = CommonDateParseUtil.date2date(new Date());
					Date uuIDExpiry = CommonDateParseUtil.string2date(scenicAdmin.getUuIDExpiry());
					if (nowdate.getTime() < uuIDExpiry.getTime()) {
						if (type.equals("2")) {
							result.put("scenicAdmin", scenicAdmin);
						}
						flag = true;
					} else {
						result.put("code", "0005");
						result.put("message", "用户登录已过期，请重新登录！");
					}
				}
			} else {
				result.put("code", "0005");
				result.put("message", "用户登录已过期，请重新登录！");
			}
		} else {
			result.put("code", "0002");
			result.put("message", "参数不全");
		}
		result.put("flag", flag);
		return result;
	}

	/*
	 * 导览笔授权激活
	 */
	public JSONObject getGuideAuthorization(JSONObject json, ScenicAdmin scenicAdmin) {
		JSONObject result = new JSONObject();
		Map<String, Object> map = new HashMap<String, Object>();
		String orderNo = json.optString("orderNo");
		result = getOrderStatus(orderNo, "1");
		boolean flag = false;
		if (result.optBoolean("flag")) {
			String guideNo = json.optString("guideNo");
			Integer scenicSiteId = json.optInt("scenicSiteId");
			Integer guideIds = json.optInt("guideIds");
			String openid = result.optString("openid");
			if (guideNo != null && scenicSiteId != null && openid != null && !guideNo.isEmpty() && !openid.isEmpty()
					&& scenicSiteId > 0 && guideIds != null && guideIds > 0) {
				// guideNo = AESUtils.decrypt(guideNo);
				// 校验该导览笔状态为正常状态，该编号的绑定记录无或者最新一条记录为解绑记录
				map.clear();
				map.put("guideNo", guideNo);
				map.put("scenicId", scenicAdmin.getFk());
				List<ScenicGuide> sgList = wxScenicMapper.getScenicGuide(map);
				map.clear();
				map.put("guideNo", guideNo);
				List<BindGuideLog> bglist = wxScenicMapper.getBindGuideLog(map);
				if (sgList != null && !sgList.isEmpty() && sgList.get(0).getStatus() == 1) {
					if (bglist != null && (bglist.isEmpty() || bglist.get(0).getStatus() == 2)) {// 授权
						// 添加授权记录，修改导览笔附属表使用个数，领用完成之后，订单状态修改为完成状态和附属表状态为已领用
						saveBindGuideLog(orderNo, openid, guideNo, 0, scenicAdmin.getId(), scenicAdmin.getFk(),
								scenicSiteId, 1);
						Integer status = null;
						if (result.optString("last").equals("one")) {
							status = 2;
							orderService.updateOrderStatus(orderNo, openid, 10, scenicAdmin.getId());
						}
						updateGuideExpand(orderNo, 1, status);
						map.clear();
						map.put("scenicId", scenicAdmin.getFk());
						Scenic s = wxScenicMapper.getScenic(map);
						JSONObject authorization = getGuideAuthorizationTime(guideIds, s.getLongTime());
						result.put("data", authorization);
						flag = true;
						result.put("code", Constant.code.CODE_1);
						result.put("message", Constant.message.MESSAGE_1);
					} else {
						result.put("code", Constant.code.CODE_13);
						result.put("message", Constant.message.MESSAGE_13);
					}
				} else {
					result.put("code", Constant.code.CODE_12);
					result.put("message", Constant.message.MESSAGE_12);
				}
			} else {
				result.put("code", Constant.code.CODE_2);
				result.put("message", Constant.message.MESSAGE_2);
			}
		}
		result.put("flag", flag);
		result.remove("last");
		result.remove("openid");
		return result;
	}

	/*
	 * 导览笔退还
	 * 
	 * type 1:线上授权激活;2现金授权激活 ;3:退还押金；4：退还部分押金；5 不退还 6 全部退还
	 * 
	 */
	public JSONObject getGuideRefund(JSONObject json, ScenicAdmin scenicAdmin, String type) throws Exception {
		JSONObject result = new JSONObject();
		Map<String, Object> map = new HashMap<String, Object>();
		boolean flag = false;
		String orderNo = json.optString("orderNo");
		Map<String, String> re = new HashMap<String, String>();
		result = getOrderStatus(orderNo, "2");
		if (result.optBoolean("flag")) {
			String guideNo = json.optString("guideNo");
			Integer scenicSiteId = json.optInt("scenicSiteId");
			String openid = result.optString("openid");
			String last = result.optString("last");
			Integer source = result.optInt("source");
			OrderGuide orderGuide = (OrderGuide) JSONObject.toBean(result.optJSONObject("orderGuide"),
					OrderGuide.class);
			if (guideNo != null && scenicSiteId != null && !guideNo.isEmpty() && scenicSiteId > 0) {// 查询当前导览笔与当前订单号是否已授权激活关系
				// 该编号的绑定记录最新一条记录为授权激活记录
				map.clear();
				map.put("guideNo", guideNo);
				List<ScenicGuide> sgList = wxScenicMapper.getScenicGuide(map);
				List<BindGuideLog> bglist = wxScenicMapper.getBindGuideLog(map);
				if (sgList != null && !sgList.isEmpty()) {
					if (bglist != null && !bglist.isEmpty()) {
						if (bglist.get(0).getStatus() == 1) {
							if (bglist.get(0).getOrderNo().equals(orderNo)) {
								// 获取附属订单信息，授权景区和解除授权保持一致
								if (orderGuide.getScenicId() == scenicAdmin.getFk()) {
									// 退还操作，并退还金额
									if (source == 1) {// 线上
										if (type.equals("3")) {// 退还押金
											result.clear();
											result = getRefundOnline(orderGuide, guideNo, scenicAdmin, scenicSiteId,
													last, orderGuide.getPrice(), 0, new BigDecimal("0.00"), 6);
										} else if (type.equals("4")) {// 退还部分押金
											BigDecimal decutionPrice = GetProperties.refund_deductionPrice();
											result = getRefundOnline(orderGuide, guideNo, scenicAdmin, scenicSiteId,
													last, orderGuide.getPrice().subtract(decutionPrice), 0,
													decutionPrice, 7);
										} else if (type.equals("5")) {// 不返还（butuikuan）
											if (last.equals("one")) {
												updateGuideExpand(orderGuide.getOrderNo(), null, 3);
											}
											saveBindGuideLog(orderGuide.getOrderNo(), orderGuide.getOpenID(), guideNo,
													0, scenicAdmin.getId(), scenicAdmin.getFk(), scenicSiteId, 2);
											// 设为损坏
											updateGuideStatus(guideNo, 2);
											result.put("code", Constant.code.CODE_1);
											result.put("data", new BigDecimal("0.00"));
										} else if (type.equals("6")) {// 全部返还（一支笔的押金和使用费）
											Integer refundReasonId = json.optInt("refundReasonId");
											if (refundReasonId != null && refundReasonId > 0) {
												result = getRefundOnline(orderGuide, guideNo, scenicAdmin, scenicSiteId,
														last, orderGuide.getPrice().add(orderGuide.getUsePrice()),
														refundReasonId, new BigDecimal("0.00"), 8);
											} else {
												result.put("code", Constant.code.CODE_2);
												result.put("message", Constant.message.MESSAGE_2);
											}
										}
									} else if (source == 3) {// 线下
										if (type.equals("3")) {// 退还押金
											result.clear();
											result = getRefundOffline(orderGuide, guideNo, scenicAdmin, scenicSiteId,
													last, orderGuide.getPrice(), 0, new BigDecimal("0.00"), 6);
										} else if (type.equals("4")) {// 退还部分押金
											BigDecimal decutionPrice = GetProperties.refund_deductionPrice();
											result = getRefundOffline(orderGuide, guideNo, scenicAdmin, scenicSiteId,
													last, orderGuide.getPrice().subtract(decutionPrice), 0,
													decutionPrice, 7);
										} else if (type.equals("5")) {// 不返还（butuikuan）
											if (last.equals("one")) {
												updateGuideExpand(orderGuide.getOrderNo(), null, 3);
											}
											saveBindGuideLog(orderGuide.getOrderNo(), "0", guideNo, 0,
													scenicAdmin.getId(), scenicAdmin.getFk(), scenicSiteId, 2);
											updateGuideStatus(guideNo, 2);
											result.put("code", Constant.code.CODE_1);
											result.put("data", new BigDecimal("0.00"));
										} else if (type.equals("6")) {// 全部返还（一支笔的押金和使用费）
											Integer refundReasonId = json.optInt("refundReasonId");
											if (refundReasonId != null && refundReasonId > 0) {
												result = getRefundOffline(orderGuide, guideNo, scenicAdmin,
														scenicSiteId, last,
														orderGuide.getPrice().add(orderGuide.getUsePrice()),
														refundReasonId, new BigDecimal("0.00"), 8);
											} else {
												result.put("code", Constant.code.CODE_2);
												result.put("message", Constant.message.MESSAGE_2);
											}
										}
									}
								} else {
									result.put("code", Constant.code.CODE_26);
									result.put("message", Constant.message.MESSAGE_26);
								}
							} else {
								result.put("code", Constant.code.CODE_16);
								result.put("message", Constant.message.MESSAGE_16);
							}
						} else {
							result.put("code", Constant.code.CODE_15);
							result.put("message", Constant.message.MESSAGE_15);
						}
					} else {
						result.put("code", Constant.code.CODE_14);
						result.put("message", Constant.message.MESSAGE_14);
					}
				} else {
					result.put("code", Constant.code.CODE_14);
					result.put("message", Constant.message.MESSAGE_14);
				}
			} else {
				result.put("code", Constant.code.CODE_2);
				result.put("message", Constant.message.MESSAGE_2);
			}
		}
		result.remove("last");
		result.remove("openid");
		result.remove("orderGuide");
		result.remove("limtPrice");
		result.remove("authlist");
		result.remove("scenicAdmin");
		result.put("flag", flag);
		return result;
	}

	/*
	 * 线上退款
	 */
	public JSONObject getRefundOnline(OrderGuide orderGuide, String guideNo, ScenicAdmin scenicAdmin,
			Integer scenicSiteId, String last, BigDecimal refundPrice, Integer refundReasonId,
			BigDecimal deductionPrice, Integer expenseType) throws Exception {
		Map<String, String> re = new HashMap<String, String>();
		JSONObject result = new JSONObject();
		Map<String, Object> map = new HashMap<String, Object>();
		map.clear();
		map.put("openID", orderGuide.getOpenID());
		WxUser wU = wxUserMapper.getWxUser(map);
		String tradeNo = orderGuide.getTradeNo();
		String refundFee = String.valueOf(refundPrice.multiply(new BigDecimal("100")).intValue());
		String total_fee = String.valueOf(orderGuide.getPaymentAmount().multiply(new BigDecimal("100")).intValue());
		String refundNo = orderService.getOrderNo(wU.getOpenCode());
		re = WeChatUtil.refund(tradeNo, refundNo, total_fee, refundFee, null);
		if (re.get("return_code").equals("SUCCESS") && re.get("result_code").equals("SUCCESS")) {
			// result = orderService.refundSuccess(wU, re.get("refund_id"),
			// tradeNo, refundNo, refundPrice,
			// orderGuide.getOrderNo(), 5, expenseType, "1",
			// scenicAdmin.getFk(), scenicAdmin.getId(),
			// refundReasonId, deductionPrice);
			if (expenseType == 6) {// 押金退还
				result = orderService.refundSuccess(wU, re.get("refund_id"), tradeNo, refundNo, refundPrice,
						orderGuide.getOrderNo(), 5, expenseType, "1", scenicAdmin.getFk(), scenicAdmin.getId(),
						refundReasonId, deductionPrice, null);
			} else if (expenseType == 7) {// 部分退还
				// 押金退还
				result = orderService.refundSuccess(wU, re.get("refund_id"), tradeNo, refundNo, orderGuide.getPrice(),
						orderGuide.getOrderNo(), 5, 6, "1", scenicAdmin.getFk(), scenicAdmin.getId(), refundReasonId,
						deductionPrice, null);
				// 损坏费用
				result = orderService.refundSuccess(wU, re.get("refund_id"), tradeNo, refundNo, deductionPrice,
						orderGuide.getOrderNo(), 5, expenseType, "1", scenicAdmin.getFk(), scenicAdmin.getId(),
						refundReasonId, deductionPrice, null);
			} else if (expenseType == 8) {// 全部退还
				// 一支笔的押金退还
				result = orderService.refundSuccess(wU, re.get("refund_id"), tradeNo, refundNo, orderGuide.getPrice(),
						orderGuide.getOrderNo(), 5, 6, "1", scenicAdmin.getFk(), scenicAdmin.getId(), refundReasonId,
						deductionPrice, null);
				// 一支笔的使用费退还
				result = orderService.refundSuccess(wU, re.get("refund_id"), tradeNo, refundNo,
						orderGuide.getUsePrice(), orderGuide.getOrderNo(), 5, 11, "1", scenicAdmin.getFk(),
						scenicAdmin.getId(), refundReasonId, deductionPrice, null);
			}
			result.put("message", "退还成功");
			if (last.equals("one")) {
				updateGuideExpand(orderGuide.getOrderNo(), null, 3);
			}
			saveBindGuideLog(orderGuide.getOrderNo(), orderGuide.getOpenID(), guideNo, 0, scenicAdmin.getId(),
					scenicAdmin.getFk(), scenicSiteId, 2);
			result.put("code", Constant.code.CODE_1);
			result.put("data", refundPrice);
		} else {
			log.info("导览笔订单退还失败（线上退还押金）：" + re.toString());
			result.put("code", Constant.code.CODE_0);
			result.put("message", Constant.message.MESSAGE_0);
		}
		return result;
	}

	/*
	 * 线下退款
	 */
	public JSONObject getRefundOffline(OrderGuide orderGuide, String guideNo, ScenicAdmin scenicAdmin,
			Integer scenicSiteId, String last, BigDecimal refundPrice, Integer refundReasonId,
			BigDecimal deductionPrice, Integer expenseType) throws Exception {
		JSONObject result = new JSONObject();
		String tradeNo = orderGuide.getTradeNo();
		String refundNo = orderService.getOrderNo(CodeUtils.getCode(6));
		if (expenseType == 6) {// 押金退还
			result = orderService.refundSuccess(null, null, tradeNo, refundNo, refundPrice, orderGuide.getOrderNo(), 4,
					expenseType, "2", scenicAdmin.getFk(), scenicAdmin.getId(), refundReasonId, deductionPrice, null);
		} else if (expenseType == 7) {// 部分退还
			// 押金退还
			result = orderService.refundSuccess(null, null, tradeNo, refundNo, orderGuide.getPrice(),
					orderGuide.getOrderNo(), 4, 6, "2", scenicAdmin.getFk(), scenicAdmin.getId(), refundReasonId,
					deductionPrice, null);
			// 损坏费用
			result = orderService.refundSuccess(null, null, tradeNo, refundNo, deductionPrice, orderGuide.getOrderNo(),
					4, expenseType, "2", scenicAdmin.getFk(), scenicAdmin.getId(), refundReasonId, deductionPrice,
					null);
		} else if (expenseType == 8) {// 全部退还
			// 一支笔的押金退还
			result = orderService.refundSuccess(null, null, tradeNo, refundNo, orderGuide.getPrice(),
					orderGuide.getOrderNo(), 4, 6, "2", scenicAdmin.getFk(), scenicAdmin.getId(), refundReasonId,
					deductionPrice, null);
			// 一支笔的使用费退还
			result = orderService.refundSuccess(null, null, tradeNo, refundNo, orderGuide.getUsePrice(),
					orderGuide.getOrderNo(), 4, 11, "2", scenicAdmin.getFk(), scenicAdmin.getId(), refundReasonId,
					deductionPrice, null);
		}
		result.put("message", "退还成功");
		updateGuideExpand(orderGuide.getOrderNo(), null, 3);
		saveBindGuideLog(orderGuide.getOrderNo(), "0", guideNo, 0, scenicAdmin.getId(), scenicAdmin.getFk(),
				scenicSiteId, 2);
		result.put("code", Constant.code.CODE_1);
		result.put("data", refundPrice);
		return result;
	}

	/*
	 * 订单状态校验
	 * 
	 */
	public JSONObject getOrderStatus(String orderNo, String type) {
		JSONObject result = new JSONObject();
		Map<String, Object> map = new HashMap<String, Object>();
		boolean flag = false;
		map.put("orderNo", orderNo);
		List<OrderGuide> list = wxScenicMapper.getOrderGuide(map);
		if (list != null && !list.isEmpty()) {
			if (type.equals("1")) {// 授权激活状态
				if (list.get(0).getStatus() == 4 && list.get(0).getGuideExpandStatus() == 1) {
					if (list.get(0).getGuideNum() > list.get(0).getGuideUseNum()) {
						flag = true;
						result.put("openid", list.get(0).getOpenID());
						if (list.get(0).getGuideNum() - list.get(0).getGuideUseNum() == 1) {// 最后一个待领取的导览笔
							result.put("last", "one");
						} else {
							result.put("last", "more");
						}
					} else {
						result.put("code", Constant.code.CODE_11);
						result.put("message", Constant.message.MESSAGE_11);
					}
				} else {
					result.put("code", Constant.code.CODE_18);
					result.put("message", Constant.message.MESSAGE_18);
				}
			} else if (type.equals("2")) {// 退还和校验用户来源
				// 订单状态为支付成功且导览笔附属表的状态为未使用或者订单为完成状态且导览笔附属表状态为已领用
				if ((list.get(0).getStatus() == 4 && list.get(0).getGuideExpandStatus() == 1
						&& list.get(0).getGuideUseNum() > 0)
						|| (list.get(0).getStatus() == 10 && list.get(0).getGuideExpandStatus() == 2)) {
					map.clear();
					map.put("status", 2);
					map.put("orderNo", orderNo);
					List<BindGuideLog> refundlist = wxScenicMapper.getBindGuideLog(map);
					if (!(refundlist.size() == list.get(0).getGuideNum())) {
						if (list.get(0).getGuideNum() - refundlist.size() == 1
								&& list.get(0).getGuideNum() == list.get(0).getGuideUseNum()) {// 退还最后一个
							result.put("last", "one");
						} else {
							result.put("last", "more");
						}
						flag = true;
						if (list.get(0).getOpenID().equals("0") && (list.get(0).getUserId() == 0)) {
							result.put("source", 3);
						}
						if ((list.get(0).getOpenID() != null && !list.get(0).getOpenID().equals("0"))
								&& (list.get(0).getUserId() == 0)) {
							result.put("source", 1);
						}
						result.put("openid", list.get(0).getOpenID());
						result.put("orderGuide", list.get(0));
					} else {
						result.put("code", Constant.code.CODE_17);
						result.put("message", Constant.message.MESSAGE_17);
					}
				} else {
					result.put("code", Constant.code.CODE_19);
					result.put("message", Constant.message.MESSAGE_19);
				}
			}
		} else {
			result.put("code", Constant.code.CODE_9);
			result.put("message", Constant.message.MESSAGE_9);
		}
		result.put("flag", flag);
		return result;
	}

	/*
	 * 授权有效时间
	 */
	public static JSONObject getGuideAuthorizationTime(Integer guideIds, BigDecimal longTime) {
		JSONObject result = new JSONObject();
		Integer key1 = Integer.valueOf("1234");
		Integer key2 = Integer.valueOf("4321");
		result.put("guideIds", guideIds ^ key1);
		longTime = longTime.multiply(new BigDecimal("60"));
		int time = longTime.intValue();
		result.put("time", time ^ guideIds);
		result.put("date", Integer.parseInt(df3.format(new Date())) ^ key2);
		result.put("key1", key1);
		result.put("key2", key2);
		return result;
	}

	/*
	 * 授权和退还记录
	 */
	public void saveBindGuideLog(String orderNo, String openid, String guideNo, Integer userId, Integer operateId,
			Integer scenicId, Integer scenicSiteId, Integer status) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("orderNo", orderNo);
		map.put("openID", openid);
		map.put("guideNo", guideNo);
		map.put("userId", userId);
		map.put("operateId", operateId);
		map.put("scenicId", scenicId);
		map.put("scenicSiteId", scenicSiteId);
		map.put("status", status);
		map.put("addTime", df.format(new Date()));
		wxScenicMapper.saveBindGuideLog(map);
	}

	/*
	 * 修改导览笔附属表状态和领取状态
	 */
	public void updateGuideExpand(String orderNo, Integer guideUseNum, Integer status) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("orderNo", orderNo);
		map.put("guideUseNum", guideUseNum);
		// map.put("scenicId", scenicId);
		if (status != null) {
			map.put("status", status);
		}
		orderMapper.updateGuideExpand(map);
	}

	/*
	 * 修改导览笔附属表状态和领取状态
	 */
	public void updateGuideStatus(String guideNo, Integer status) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("guideNo", guideNo);
		map.put("status", status);
		orderMapper.updateGuideStatus(map);
	}

	public void operateGuideClick(List<GuideClickPOJO> list, Integer scenicId) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<GuideClickPOJO> list1 = new ArrayList<GuideClickPOJO>();
		for (int i = 0; i < list.size(); i++) {// 查询文件是否存在
			map.clear();
			map.put("baseCode", list.get(i).getBaseCode());
			map.put("scenicId", scenicId);
			BaseGuideFile bf = wxScenicMapper.getBaseGuideFile(map);
			if (bf == null || list.get(i).getClickNum() == 0) {
			} else {
				list.get(i).setBgfId(bf.getId());
				list1.add(list.get(i));
			}
		}
		for (GuideClickPOJO gcp : list1) {
			map.clear();
			map.put("bgfId", gcp.getBgfId());
			GuideClick gc = wxScenicMapper.getGuideClick(map);
			if (gc != null) {// 更新点击次数
				map.clear();
				map.put("bgfId", gcp.getBgfId());
				map.put("clickNum", gcp.getClickNum());
				wxScenicMapper.updateGuideClick(map);
			} else {// 新增一天记录
				map.clear();
				map.put("bgfId", gcp.getBgfId());
				map.put("clickNum", gcp.getClickNum());
				map.put("addDate", df1.format(new Date()));
				wxScenicMapper.saveGuideClick(map);
			}
		}

	}

	public JSONObject getGuideRefundByCash(JSONObject json, ScenicAdmin sa, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * 现金激活授权
	 */
	public JSONObject getGuideAuthorizationByCash(JSONObject json, ScenicAdmin sa) {
		Map<String, Object> map = new HashMap<String, Object>();
		Integer guideIds = json.optInt("guideIds");
		String guideNo = json.optString("guideNo");
		Integer scenicSiteId = json.optInt("scenicSiteId");
		boolean flag = false;
		JSONObject result = new JSONObject();
		if (guideNo != null && !guideNo.isEmpty() && scenicSiteId != null && scenicSiteId > 0 && guideIds != null
				&& guideIds > 0) {
			map.clear();
			map.put("guideNo", guideNo);
			map.put("scenicId", sa.getFk());
			// 查看导览笔是否存在和状态是否正常
			List<ScenicGuide> sgList = wxScenicMapper.getScenicGuide(map);
			map.put("scenicId", sa.getFk());
			// 获取导览笔当天租用费用
			ScenicGuideFee sgf = wxScenicMapper.getScenicGuideFee(map);
			map.clear();
			map.put("guideNo", guideNo);
			// 获取该导览笔是否可以授权
			List<BindGuideLog> bglist = wxScenicMapper.getBindGuideLog(map);
			if (sgList != null && !sgList.isEmpty() && sgList.get(0).getStatus() == 1) {
				if (bglist != null && (bglist.isEmpty() || bglist.get(0).getStatus() == 2)) {
					if (sgf != null) {// 授权
						String tradeNo = CodeUtils.gettradeNo();
						Order o = orderService.saveOrder(null, tradeNo, 1, null, 8,
								sgf.getPrice().add(sgf.getUsePrice()));// 创建订单
						orderService.saveOrderlog(o.getOrderNo(), null, 1, 6, sa.getId());// 操作记录
						orderService.saveGuideOrder(null, o.getOrderNo(), sa.getFk(), 1, sgf.getPrice(),
								sgf.getUsePrice());// 保存附属订单信息
						orderService.paySuccess(null, null, tradeNo, sgf.getPrice(), o.getOrderNo(), 4, 9, "2",
								sa.getFk(), sa.getId(), null);
						orderService.paySuccess(null, null, tradeNo, sgf.getUsePrice(), o.getOrderNo(), 4, 10, "2",
								sa.getFk(), sa.getId(), "1");
						saveBindGuideLog(o.getOrderNo(), "0", guideNo, 0, sa.getId(), sa.getFk(), scenicSiteId, 1);// 授权成功
						orderService.updateOrderStatus(o.getOrderNo(), null, 10, sa.getId());// 订单完结
						updateGuideExpand(o.getOrderNo(), 1, 2);// 附属订单已领用
						map.clear();
						map.put("scenicId", sa.getFk());
						Scenic s = wxScenicMapper.getScenic(map);
						JSONObject authorization = getGuideAuthorizationTime(guideIds, s.getLongTime());
						result.put("data", authorization);
						flag = true;
						result.put("code", Constant.code.CODE_1);
						result.put("message", Constant.message.MESSAGE_1);
					} else {
						result.put("code", Constant.code.CODE_23);
						result.put("message", Constant.message.MESSAGE_23);
					}
				} else {
					result.put("code", Constant.code.CODE_13);
					result.put("message", Constant.message.MESSAGE_13);
				}
			} else {
				result.put("code", Constant.code.CODE_12);
				result.put("message", Constant.message.MESSAGE_12);
			}
		} else {
			result.put("code", Constant.code.CODE_2);
			result.put("message", Constant.message.MESSAGE_2);
		}
		result.put("flag", flag);
		return result;
	}

	/**
	 * 
	 * @description 用户操作记录表
	 * @param platformType
	 *            1 平台管理PC端 2 商家管理PC端 3 景区管理PC端 4 景区租赁APP端 5 景区管理APP端 6 商家管理APP端
	 * @param loginType
	 *            1 登录 2 持久化
	 * @return
	 */
	public void operateLoginLog(Integer id, Integer loginType,Integer platformType) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("operateId", id);
		map.put("loginType", loginType);
		map.put("platformType", platformType);
		wxScenicMapper.operateLoginLog(map);
	}
}
