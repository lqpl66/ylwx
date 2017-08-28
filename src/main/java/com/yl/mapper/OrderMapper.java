package com.yl.mapper;

import java.math.BigDecimal;
import java.util.Map;

import com.yl.bean.Admin;
import com.yl.bean.ExpenseGuideLog;
import com.yl.bean.Order;
import com.yl.bean.RefundReason;

public interface OrderMapper {
	// 获取订单号
	String getOrder(Map<String, Object> map);

	// 保存订单
	void saveOrder(Order o);

	// 修改订单
	void updateOrder(Map<String, Object> map);

	// 插入用户订单流水
	void saveExpenseUserlog(Map<String, Object> map);

	// 插入导览笔订单流水
	void saveExpenseGuidelog(Map<String, Object> map);

	// 插入平台订单流水
	void saveExpenseSystemlog(Map<String, Object> map);

	// 插入订单记录表
	void saveOrderLog(Map<String, Object> map);

	// 平台流水管理员
	Admin getAdmin(Map<String, Object> map);

	// 订单详情
	Order getOrderDetails(Map<String, Object> map);

	// 导览笔附属订单保存
	void saveGuideOrder(Map<String, Object> map);

	// 导览笔附属订单修改
	void updateGuideExpand(Map<String, Object> map);

	// 导览笔状态修改
	void updateGuideStatus(Map<String, Object> map);

	// 管理员当天现金流水总金额
	BigDecimal getExpenseGuideLogPrice(Map<String, Object> map);
	//导览笔流水记录
	ExpenseGuideLog getExpenseGuideLog(Map<String, Object> map);

}
