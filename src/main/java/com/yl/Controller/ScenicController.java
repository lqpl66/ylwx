package com.yl.Controller;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.yl.Pojo.GuideClickPOJO;
import com.yl.Pojo.OrderGuidePojo;
import com.yl.Service.OrderService;
import com.yl.Service.ScenicService;
import com.yl.Utils.Constant;
import com.yl.Utils.FileBin;
import com.yl.Utils.MD5Utils;
import com.yl.bean.BindGuideLog;
import com.yl.bean.ExpenseGuideLog;
import com.yl.bean.Guide;
import com.yl.bean.OrderGuide;
import com.yl.bean.OrderGuideVo;
import com.yl.bean.RefundReason;
import com.yl.bean.Scenic;
import com.yl.bean.ScenicAdmin;
import com.yl.bean.ScenicGuideFee;
import com.yl.bean.ScenicSite;
import com.yl.bean.Version;
import com.yl.mapper.OrderMapper;
import com.yl.mapper.WxScenicMapper;
import com.yl.mapper.WxUserMapper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/*
 * 景区人员操作接口
 */
@Controller
@RequestMapping(value = "/scenic", produces = "text/html;charset=UTF-8")
public class ScenicController {

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
	private ScenicService scenicService;
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/*
	 * 登录
	 */
	@SuppressWarnings("finally")
	@Transactional
	@ResponseBody
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String login(HttpServletRequest request, @RequestBody String jsonparam) {
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject result = new JSONObject();
		try {
			JSONObject json = JSONObject.fromObject(jsonparam);
			result = scenicService.getScenicAdmin(json);
			if (result.optBoolean("flag")) {// 登录成功
				// 查询景区信息
				map.clear();
				JSONObject data = JSONObject.fromObject(result.get("data"));
				map.put("scenicId", data.optInt("scenicId"));
				Scenic s = wxScenicMapper.getScenic(map);
				if (s != null) {
					data.put("scenicName", s.getScenicName());
					result.put("data", data);
				} else {
					result.clear();
					result.put("code", "0007");
					result.put("message", "该账号出现异常，请联系景区管理员");
				}
			}
		} catch (Exception e) {
			log.error("游乐景区用户登录异常：", e);
			result.put("code", "0000");
			result.put("message", "服务繁忙");
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} finally {
			result.remove("flag");
			return result.toString();
		}
	}

	/*
	 * 获取站点信息
	 */
	@SuppressWarnings("finally")
	@Transactional
	@ResponseBody
	@RequestMapping(value = "/getScenicSite", method = RequestMethod.POST)
	public String getScenicSite(HttpServletRequest request, @RequestBody String jsonparam) {
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject result = new JSONObject();
		try {
			JSONObject json = JSONObject.fromObject(jsonparam);
			result = scenicService.getScenicAdminUUID(json, "1");
			if (result.optBoolean("flag")) {// 验证成功
				// 查询景区站点信息
				Integer scenicId = json.optInt("scenicId");
				if (scenicId != null && scenicId > 0) {
					map.put("scenicId", scenicId);
					List<ScenicSite> list = wxScenicMapper.getScenicSite(map);
					if (list != null && !list.isEmpty()) {
						result.clear();
						result.put("code", "0001");
						result.put("message", "success");
						result.put("data", list);
					} else {
						result.clear();
						result.put("code", "0007");
						result.put("message", "景区站点出现异常，请联系景区管理员");
					}
				} else {
					result.put("code", "0002");
					result.put("message", "参数不全");
				}
			}
		} catch (Exception e) {
			log.error("景区获取站点异常：", e);
			result.put("code", "0000");
			result.put("message", "服务繁忙");
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} finally {
			result.remove("flag");
			return result.toString();
		}
	}

	/*
	 * 获取退还原因
	 */
	@SuppressWarnings("finally")
	@ResponseBody
	@RequestMapping(value = "/getRefundReason", method = RequestMethod.POST)
	public String getRefundReason(HttpServletRequest request, @RequestBody String jsonparam) {
		JSONObject result = new JSONObject();
		try {
			List<RefundReason> rr = wxScenicMapper.getRefundReason();
			result.put("data", rr);
			result.put("code", Constant.code.CODE_1);
			result.put("message", Constant.message.MESSAGE_1);
		} catch (Exception e) {
			log.error("景区获取退还原因异常：", e);
			result.put("code", "0000");
			result.put("message", "服务繁忙");
		} finally {
			return result.toString();
		}
	}

	/*
	 * 修改密码
	 */
	@SuppressWarnings("finally")
	@Transactional
	@ResponseBody
	@RequestMapping(value = "/modifyPwd", method = RequestMethod.POST)
	public String modifyPwd(HttpServletRequest request, @RequestBody String jsonparam) {
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject result = new JSONObject();
		try {
			JSONObject json = JSONObject.fromObject(jsonparam);
			result = scenicService.getScenicAdminUUID(json, "2");
			if (result.optBoolean("flag")) {// 验证成功
				String prePwd = json.optString("prePwd");
				String nowPwd = json.optString("nowPwd");
				String uuID = json.optString("uuID");
				if (nowPwd != null && prePwd != null && uuID != null && !uuID.isEmpty() && !prePwd.isEmpty()
						&& !nowPwd.isEmpty()) {
					ScenicAdmin sa = (ScenicAdmin) JSONObject.toBean(result.optJSONObject("scenicAdmin"),
							ScenicAdmin.class);
					if (sa != null) {
						if (MD5Utils.string2MD5(prePwd).equals(sa.getAdminPwd())) {
							String nowPwdMD5 = MD5Utils.string2MD5(nowPwd);
							map.clear();
							map.put("adminName", sa.getAdminName());
							map.put("adminPwd", nowPwdMD5);
							map.put("modifyUser", sa.getId());
							map.put("modifyTime", df.format(new Date()));
							wxScenicMapper.updateScenicAdmin(map);
							result.clear();
							result.put("code", "0001");
							result.put("message", "修改成功");
						} else {
							result.clear();
							result.put("code", "0008");
							result.put("message", "该账号密码不正确，请联系景区管理员");
						}
					} else {
						result.clear();
						result.put("code", "0007");
						result.put("message", "该账号出现异常，请联系景区管理员");
					}
				} else {
					result.put("code", "0002");
					result.put("message", "参数不全");
				}
			}
		} catch (Exception e) {
			log.error("景区子管理员修改密码异常：", e);
			result.clear();
			result.put("code", "0000");
			result.put("message", "服务繁忙");
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} finally {
			result.remove("flag");
			return result.toString();
		}
	}

	/*
	 * 订单详情
	 */
	@SuppressWarnings("finally")
	@Transactional
	@ResponseBody
	@RequestMapping(value = "/getOrderDetails", method = RequestMethod.POST)
	public String getOrderDetails(HttpServletRequest request, @RequestBody String jsonparam) {
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject result = new JSONObject();
		try {
			JSONObject json = JSONObject.fromObject(jsonparam);
			result = scenicService.getScenicAdminUUID(json, "1");
			if (result.optBoolean("flag")) {// 验证成功
				String orderNo = json.optString("orderNo");
				if (orderNo != null && !orderNo.isEmpty()) {
					map.put("orderNo", orderNo);
					List<OrderGuide> list = wxScenicMapper.getOrderGuide(map);
					if (list != null && !list.isEmpty()) {
						OrderGuidePojo OGPojo = new OrderGuidePojo();
						JSONObject OGjson = JSONObject.fromObject(list.get(0));
						OGPojo = (OrderGuidePojo) JSONObject.toBean(OGjson, OrderGuidePojo.class);
						// 查询该订单下退还记录 status:1 綁定记录；2 解绑记录
						map.clear();
						map.put("orderNo", list.get(0).getOrderNo());
						map.put("status", 2);
						List<BindGuideLog> bglist = wxScenicMapper.getBindGuideLog(map);
						if (bglist != null && !bglist.isEmpty()) {
							OGPojo.setGuideRebackNum(OGPojo.getGuideUseNum() - bglist.size());
						} else {
							OGPojo.setGuideRebackNum(OGPojo.getGuideUseNum());
						}
						OGPojo.setGuideNoUseNum(OGPojo.getGuideNum() - OGPojo.getGuideUseNum());
						result.clear();
						result.put("data", OGPojo);
						result.put("code", "0001");
						result.put("message", "success");
					} else {
						result.put("code", "0009");
						result.put("message", "订单为无效订单，无法查看");
					}
				} else {
					result.put("code", "0002");
					result.put("message", "参数不全");
				}
			}
		} catch (Exception e) {
			log.error("订单详情异常：", e);
			result.put("code", "0000");
			result.put("message", "服务繁忙");
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} finally {
			result.remove("flag");
			return result.toString();
		}
	}

	/*
	 * type 1:线上授权激活;2现金授权激活 ;3:退还押金；4：退还部分押金；5 不退还 6 全部退还
	 * 
	 */
	@SuppressWarnings("finally")
	@Transactional
	@ResponseBody
	@RequestMapping(value = "/operateGuideAuthorization ", method = RequestMethod.POST)
	public String operateGuideAuthorization(HttpServletRequest request, @RequestBody String jsonparam) {
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject result = new JSONObject();
		try {
			JSONObject json = JSONObject.fromObject(jsonparam);
			result = scenicService.getScenicAdminUUID(json, "2");
			if (result.optBoolean("flag")) {// 验证成功
				String type = json.optString("type");
				ScenicAdmin sa = (ScenicAdmin) JSONObject.toBean(result.optJSONObject("scenicAdmin"),
						ScenicAdmin.class);
				if (type != null && !type.isEmpty() && sa != null) {
					if (type.equals("1")) {// 线上授权激活
						result = scenicService.getGuideAuthorization(json, sa);
					} else if (type.equals("2")) {// 线下现金授权激活
						result = scenicService.getGuideAuthorizationByCash(json, sa);
					} else {//
						result = scenicService.getGuideRefund(json, sa, type);
					}
				} else {
					result.put("code", "0002");
					result.put("message", "参数不全");
				}
			}
		} catch (Exception e) {
			log.error("订单操作异常：", e);
			result.put("code", "0000");
			result.put("message", "服务繁忙");
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} finally {
			result.remove("flag");
			result.remove("scenicAdmin");
			System.out.println("result:"+result.toString());
			return result.toString();
		}
	}

	/*
	 * 导览笔点击次数记录
	 * 
	 * 每天生成一天记录
	 * 
	 */
	@SuppressWarnings("finally")
	@Transactional
	@ResponseBody
	@RequestMapping(value = "/uploadGuideClick", method = RequestMethod.POST)
	public String uploadGuideClick(HttpServletRequest request, @RequestBody String jsonparam) {
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject result = new JSONObject();
		try {
			JSONObject json = JSONObject.fromObject(jsonparam);
			result = scenicService.getScenicAdminUUID(json, "2");
			if (result.optBoolean("flag")) {// 验证成功
				// String str = json.optString("str");
				// JSONArray list = FileBin.getDoc_fileList(str);
				List<GuideClickPOJO> list = JSONArray.toList(json.getJSONArray("list"), GuideClickPOJO.class);
				ScenicAdmin sa = (ScenicAdmin) JSONObject.toBean(result.optJSONObject("scenicAdmin"),
						ScenicAdmin.class);
				if (list != null && !list.isEmpty()) {
					System.out.println("list:"+list);
					scenicService.operateGuideClick(list,sa.getFk());
					result.put("code", Constant.code.CODE_1);
					result.put("message", Constant.message.MESSAGE_1);
				} else {
					result.put("code", Constant.code.CODE_2);
					result.put("message", Constant.message.MESSAGE_2);
				}
			}
		} catch (Exception e) {
			log.error("导览笔点击次数记录异常：", e);
			result.put("code", "0000");
			result.put("message", "服务繁忙");
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} finally {
			result.remove("flag");
			result.remove("scenicAdmin");
			return result.toString();
		}
	}

	/*
	 * 现金租用
	 */
	@SuppressWarnings("finally")
	@ResponseBody
	@RequestMapping(value = "/getCashLease", method = RequestMethod.POST)
	public String getCashLease(HttpServletRequest request, @RequestBody String jsonparam) {
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject result = new JSONObject();
		try {
			JSONObject json = JSONObject.fromObject(jsonparam);
			result = scenicService.getScenicAdminUUID(json, "2");
			if (result.optBoolean("flag")) {// 验证成功
				String scenicId = json.optString("scenicId");
				if (scenicId != null && !scenicId.isEmpty()) {
					map.put("scenicId", scenicId);
					ScenicGuideFee sgf = wxScenicMapper.getScenicGuideFee(map);
					ScenicAdmin sc = (ScenicAdmin) JSONObject.toBean(result.optJSONObject("scenicAdmin"),
							ScenicAdmin.class);
					map.put("userType", 3);
					map.put("paymentType", 4);
					map.put("operateId", sc.getId());
					BigDecimal totalCash = orderMapper.getExpenseGuideLogPrice(map);
					if (sgf != null) {
						result.clear();
						sgf.setTotalCash(totalCash);
						result.put("data", sgf);
						result.put("code", "0001");
						result.put("message", "success");
					} else {
						result.put("code", Constant.code.CODE_23);
						result.put("message", Constant.message.MESSAGE_23);
					}
				} else {
					result.put("code", "0002");
					result.put("message", "参数不全");
				}
			}
		} catch (Exception e) {
			log.error("现金租用详情异常：", e);
			result.put("code", "0000");
			result.put("message", "服务繁忙");
		} finally {
			result.remove("flag");
			result.remove("scenicAdmin");
			return result.toString();
		}
	}

	/*
	 * 根据导览笔内存编号 获取导览笔外部编号 或者是 退还详情
	 * 
	 * type 1 根据内存编号获取导览笔状态和编号 2 获取订单相关信息(退还根据内存编号) 3 获取订单相关信息(退还根据外在编号)
	 * 
	 */
	@SuppressWarnings({ "finally", "unused" })
	@Transactional
	@ResponseBody
	@RequestMapping(value = "/getGuideNoByGuideIds", method = RequestMethod.POST)
	public String getGuideNoByGuideIds(HttpServletRequest request, @RequestBody String jsonparam) {
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject result = new JSONObject();
		try {
			JSONObject json = JSONObject.fromObject(jsonparam);
			result = scenicService.getScenicAdminUUID(json, "1");
			if (result.optBoolean("flag")) {// 验证成功
				String type = json.optString("type");
				System.out.println("type:"+type);
				if (type != null && !type.isEmpty()) {
					if (type.equals("1") || type.equals("2")) {
						Integer guideIds = json.optInt("guideIds");
						if (guideIds != null) {
							if (guideIds == -1) {
								result.put("code", Constant.code.CODE_25);
								result.put("message", Constant.message.MESSAGE_25);
								return result.toString();
							} else {
								map.put("guideIds", guideIds);
								Guide g = wxScenicMapper.getGuide(map);
								if (g != null) {
									map.clear();
									map.put("guideNo", g.getGuideNo());
									List<BindGuideLog> list = wxScenicMapper.getBindGuideLog(map);
									if (type.equals("1")) {
										if (list.isEmpty() || (!list.isEmpty() && list.get(0).getStatus() == 2)) {// 导览笔可以授权
											g.setStatus(2);
										} else {
											g.setStatus(1);// 使用中，待退还
										}
										result.put("data", g);
										result.put("code", Constant.code.CODE_1);
										result.put("message", Constant.message.MESSAGE_1);
									} else if (type.equals("2")) {
										OrderGuideVo ogv = new OrderGuideVo();
										ogv.setGuideIds(guideIds);
										ogv.setGuideNo(g.getGuideNo());
										if (list.isEmpty() || (!list.isEmpty() && list.get(0).getStatus() == 2)) {// 不可退还
											ogv.setGuideStatus(2);
											result.put("code", Constant.code.CODE_24);
											result.put("message", Constant.message.MESSAGE_24);
											result.put("data", ogv);
											return result.toString();
										} else {// 可以退还， 查看流水判断
											map.put("orderNo", list.get(0).getOrderNo());
											List<OrderGuide> oglist = wxScenicMapper.getOrderGuide(map);
											if (oglist != null && !oglist.isEmpty()) {
												map.clear();
												map.put("expenseGuideNo", oglist.get(0).getExpenseId());
												if ((oglist.get(0).getUserId() == 0
														|| oglist.get(0).getUserId() == null)
														&& (oglist.get(0).getOpenID().equals("0")
																|| oglist.get(0).getOpenID() == null)) {
													ogv.setSource(3);// 现金来源
												} else if ((oglist.get(0).getUserId() == 0
														|| oglist.get(0).getUserId() == null)
														&& (!oglist.get(0).getOpenID().equals("0")
																&& oglist.get(0).getOpenID() != null)) {
													ogv.setSource(1);// 线上
												}
												ogv.setGuideStatus(1);
												ogv.setOrderNo(list.get(0).getOrderNo());
												ogv.setPrice(oglist.get(0).getPrice());
												ogv.setScenicId(oglist.get(0).getScenicId());
												ogv.setScenicName(oglist.get(0).getScenicName());
												ogv.setUsePrice(oglist.get(0).getUsePrice());
											} else {
												result.put("code", Constant.code.CODE_9);
												result.put("message", Constant.message.MESSAGE_9);
												return result.toString();
											}
										}
										result.put("data", ogv);
										result.put("code", Constant.code.CODE_1);
										result.put("message", Constant.message.MESSAGE_1);
									}
								}
							}
						} else {
							result.put("code", Constant.code.CODE_2);
							result.put("message", Constant.message.MESSAGE_2);
						}
					} else if (type.equals("3")) {
						String guideNo = json.optString("guideNo");
						map.clear();
						map.put("guideNo", guideNo);
						// Guide g = wxScenicMapper.getGuide(map);
						List<BindGuideLog> list = wxScenicMapper.getBindGuideLog(map);
						OrderGuideVo ogv = new OrderGuideVo();
						// ogv.setGuideIds(g.getGuideIds());
						ogv.setGuideNo(guideNo);
						if (list.isEmpty() || (!list.isEmpty() && list.get(0).getStatus() == 2)) {// 不可退还
							ogv.setGuideStatus(2);
							result.put("code", Constant.code.CODE_24);
							result.put("message", Constant.message.MESSAGE_24);
							result.put("data", ogv);
							return result.toString();
						} else {// 可以退还， 查看流水判断
							map.put("orderNo", list.get(0).getOrderNo());
							List<OrderGuide> oglist = wxScenicMapper.getOrderGuide(map);
							if (oglist != null && !oglist.isEmpty()) {
								map.clear();
								map.put("expenseGuideNo", oglist.get(0).getExpenseId());
								if ((oglist.get(0).getUserId() == 0 || oglist.get(0).getUserId() == null)
										&& (oglist.get(0).getOpenID().equals("0")
												|| oglist.get(0).getOpenID() == null)) {
									ogv.setSource(3);// 现金来源
								} else if ((oglist.get(0).getUserId() == 0 || oglist.get(0).getUserId() == null)
										&& (!oglist.get(0).getOpenID().equals("0")
												&& oglist.get(0).getOpenID() != null)) {
									ogv.setSource(1);// 线上
								}
								ogv.setGuideStatus(1);
								ogv.setOrderNo(list.get(0).getOrderNo());
								ogv.setPrice(oglist.get(0).getPrice());
								ogv.setScenicId(oglist.get(0).getScenicId());
								ogv.setScenicName(oglist.get(0).getScenicName());
								ogv.setUsePrice(oglist.get(0).getUsePrice());
							} else {
								result.put("code", Constant.code.CODE_9);
								result.put("message", Constant.message.MESSAGE_9);
								return result.toString();
							}
						}
						result.put("data", ogv);
						result.put("code", Constant.code.CODE_1);
						result.put("message", Constant.message.MESSAGE_1);
						// if (list.isEmpty() || (!list.isEmpty() &&
						// list.get(0).getStatus() == 2)) {// 不可退还
						// g.setStatus(2);
						// result.put("data", g);
						// result.put("code", Constant.code.CODE_15);
						// result.put("message", Constant.message.MESSAGE_15);
						// return result.toString();
						// } else {// 可以退还， 查看流水判断
						// map.put("orderNo", list.get(0).getOrderNo());
						// List<OrderGuide> og =
						// wxScenicMapper.getOrderGuide(map);
						// if (og != null && !og.isEmpty()) {
						// map.clear();
						// map.put("expenseGuideNo", og.get(0).getExpenseId());
						// if ((og.get(0).getUserId() == 0 ||
						// og.get(0).getUserId() == null)
						// && (og.get(0).getOpenID().equals("0") ||
						// og.get(0).getOpenID() == null)) {
						// og.get(0).setSource(3);// 现金来源
						// } else if ((og.get(0).getUserId() == 0 ||
						// og.get(0).getUserId() == null)
						// && (!og.get(0).getOpenID().equals("0") &&
						// og.get(0).getOpenID() != null)) {
						// og.get(0).setSource(1);// 线上
						// }
						// result.put("data", og.get(0));
						// result.put("code", Constant.code.CODE_1);
						// result.put("message", Constant.message.MESSAGE_1);
						// } else {
						// result.put("code", Constant.code.CODE_0);
						// result.put("message", Constant.message.MESSAGE_0);
						// return result.toString();
						// }
						// }
					}
				} else {
					result.put("code", Constant.code.CODE_2);
					result.put("message", Constant.message.MESSAGE_2);
				}
			}
		} catch (Exception e) {
			log.error("导览笔退还详情异常：", e);
			result.put("code", "0000");
			result.put("message", "服务繁忙");
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} finally {
			result.remove("flag");
			return result.toString();
		}
	}

	/*
	 * 现金租用金额
	 */
	@SuppressWarnings("finally")
	@ResponseBody
	@RequestMapping(value = "/getCashTotal", method = RequestMethod.POST)
	public String getCashTotal(HttpServletRequest request, @RequestBody String jsonparam) {
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject result = new JSONObject();
		try {
			JSONObject json = JSONObject.fromObject(jsonparam);
			result = scenicService.getScenicAdminUUID(json, "2");
			if (result.optBoolean("flag")) {// 验证成功
				String scenicId = json.optString("scenicId");
				if (scenicId != null && !scenicId.isEmpty()) {
					ScenicAdmin sc = (ScenicAdmin) JSONObject.toBean(result.optJSONObject("scenicAdmin"),
							ScenicAdmin.class);
					map.put("userType", 3);
					map.put("paymentType", 4);
					map.put("operateId", sc.getId());
					BigDecimal totalCash = orderMapper.getExpenseGuideLogPrice(map);
					result.clear();
					if (totalCash != null) {
						result.put("data", totalCash);
					} else {
						result.put("data", new BigDecimal("0.00"));
					}
					result.put("code", "0001");
					result.put("message", "success");
					//日志记录
					scenicService.operateLoginLog(sc.getId(), 2, 4);
				} else {
					result.put("code", "0002");
					result.put("message", "参数不全");
				}
			}
		} catch (Exception e) {
			log.error("现金租金金额异常：", e);
			result.put("code", "0000");
			result.put("message", "服务繁忙");
		} finally {
			result.remove("flag");
			result.remove("scenicAdmin");
			return result.toString();
		}
	}

	/*
	 * 租赁退还接口
	 */
	@SuppressWarnings("finally")
	@ResponseBody
	@RequestMapping(value = "/getBindGuideLogList", method = RequestMethod.POST)
	public String getBindGuideLogList(HttpServletRequest request, @RequestBody String jsonparam) {
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject result = new JSONObject();
		try {
			JSONObject json = JSONObject.fromObject(jsonparam);
			result = scenicService.getScenicAdminUUID(json, "2");
			Integer num = json.optInt("num");
			Integer pageNum = json.optInt("pageNum");
			if (result.optBoolean("flag")) {// 验证成功
				if (num != null && num > 0 && pageNum != null && pageNum > 0) {
					ScenicAdmin sc = (ScenicAdmin) JSONObject.toBean(result.optJSONObject("scenicAdmin"),
							ScenicAdmin.class);
					map.put("start", num * (pageNum - 1));
					map.put("num", num);
					map.put("operateId", sc.getId());
					List<BindGuideLog> list = wxScenicMapper.getBindGuideLogList(map);
					result.clear();
					result.put("data", list);
					result.put("code", "0001");
					result.put("message", "success");
				} else {
					result.put("code", "0002");
					result.put("message", "参数不全");
				}
			}
		} catch (Exception e) {
			log.error("租赁退还接口异常：", e);
			result.put("code", "0000");
			result.put("message", "服务繁忙");
		} finally {
			result.remove("flag");
			result.remove("scenicAdmin");
			return result.toString();
		}
	}

	/*
	 * 导览笔apk版本接口
	 */
	@SuppressWarnings("finally")
	@ResponseBody
	@RequestMapping(value = "/getVersion", method = RequestMethod.POST)
	public String getVersion(HttpServletRequest request, @RequestBody String jsonparam) {
		JSONObject result = new JSONObject();
		HashMap<String, Object> map = new HashMap<String, Object>();
		try {
			map.clear();
			map.put("mbSystemType", 2);
			map.put("operateType", 4);
			List<Version> list = wxScenicMapper.getVersion(map);
			result.put("data", list.get(0));
			result.put("code", "0001");
			result.put("message", "操作成功！");
		} catch (Exception e) {
			log.error("APP版本校验失败：", e);
			result.put("code", "0000");
			result.put("message", "平台繁忙，请稍候！");
		}
		return result.toString();
	}
}
