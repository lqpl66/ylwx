package com.yl.Utils;

public interface Constant {
	// 0000:服务繁忙
	// 0001:查询成功
	// 0002：参数不全
	// 0003:账户冻结，无法登录
	// 0004:账号或密码错误，无法登录
	// 0005:登录时间失效，请重新登录
	// 0006：该账号不是子管理员账号,无法登录该APP
	// 0007:账户出现异常,请联系景区管理员
	// 0008:密码不正确,请联系景区管理员
	// 0009: 订单为无效订单，无法查看
	public interface code {
		public static final String CODE_0 = "0000";
		public static final String CODE_1 = "0001";
		public static final String CODE_2 = "0002";
		public static final String CODE_3 = "0003";
		public static final String CODE_4 = "0004";
		public static final String CODE_5 = "0005";
		public static final String CODE_6 = "0006";
		public static final String CODE_7 = "0007";
		public static final String CODE_8 = "0008";
		public static final String CODE_9 = "0009";
		public static final String CODE_10 = "0010";
		public static final String CODE_11 = "0011";
		public static final String CODE_12 = "0012";
		public static final String CODE_13 = "0013";
		public static final String CODE_14 = "0014";
		public static final String CODE_15 = "0015";
		public static final String CODE_16 = "0016";
		public static final String CODE_17 = "0017";
		public static final String CODE_18 = "0018";
		public static final String CODE_19 = "0019";
		public static final String CODE_20 = "0020";
		public static final String CODE_21 = "0021";
		public static final String CODE_22 = "0022";
		public static final String CODE_23 = "0023";
		public static final String CODE_24= "0024";
		public static final String CODE_25= "0025";
		public static final String CODE_26= "0026";
	}

	// 0000:服务繁忙
	// 0001:查询成功
	// 0002：参数不全
	// 0003:账户冻结,无法登录
	// 0004:账号或密码错误,无法登录
	// 0005:登录时间失效,请重新登录
	// 0006：该账号不是子管理员账号,无法登录该APP
	// 0007:账户出现异常,请联系景区管理员
	// 0008:密码不正确,请联系景区管理员
	// 0009: 订单为无效订单,请联系景区管理员
	public interface message {

		public static final String MESSAGE_0 = "服务繁忙";
		public static final String MESSAGE_1 = "操作成功";
		public static final String MESSAGE_2 = "参数不全";
		public static final String MESSAGE_3 = "账户冻结,无法登录";
		public static final String MESSAGE_4 = "账号或密码错误,无法登录";
		public static final String MESSAGE_5 = "登录时间失效,请重新登录";
		public static final String MESSAGE_6 = "该账号不是子管理员账号,无法登录该APP";
		public static final String MESSAGE_7 = "账户出现异常,请联系景区管理员";
		public static final String MESSAGE_8 = "密码不正确,请联系景区管理员";
		public static final String MESSAGE_9 = "订单为无效订单,请联系游乐平台";
		public static final String MESSAGE_10 = "该订单出现异常,请联系游乐平台";
		public static final String MESSAGE_11 = "该订单已无待授权的导览笔";
		public static final String MESSAGE_12 = "该导览笔已被冻结,无法授权,请联系游乐平台";
		public static final String MESSAGE_13 = "该导览笔尚未归还,无法授权,请联系游乐平台";
		public static final String MESSAGE_14 = "暂无该导览笔信息,无法退还,请联系游乐平台";
		public static final String MESSAGE_15 = "该导览笔已归还,请不要重复操作";
		public static final String MESSAGE_16 = "订单与该导览笔授权信息不符,无法退还,请重新扫码";
		public static final String MESSAGE_17 = "订单已无导览笔可退还,请不要重复操作";
		public static final String MESSAGE_18 = "该订单已无法授权,请重新扫码或请联系游乐平台";
		public static final String MESSAGE_19 = "该订单已无法退还导览笔,请重新扫码或请联系游乐平台";
		public static final String MESSAGE_20 = "该订单不符合手动退还的条件,请确认后操作";
		public static final String MESSAGE_21 = "手动退还金额超出退款上限,请确认后操作";
		public static final String MESSAGE_22 = "导览笔无效,请确认后操作";
		public static final String MESSAGE_23 = "无收取费用信息";
		public static final String MESSAGE_24 = "该导览笔已归还,无需退还";
		public static final String MESSAGE_25 = "该导览笔不合法,";
		public static final String MESSAGE_26 = "该导览笔不是在该景区授权,无法退还，请到授权景区处退还";
	}

}
