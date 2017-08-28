package com.yl.Wechat;

public class WechatConfig {
	 /**
	    * 预支付请求地址
	    * https://api.mch.weixin.qq.com/pay/unifiedorder
	    */
	   public static final String  PrepayUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	   /**
	    * 商户APPID
	    * 
	    * wxc9e385e97966ab61  服务号1
	    * 
	    * wxa095cf3d62e9ea06  服务号2
	    */
	   public static final String  AppId = "wxa095cf3d62e9ea06";

	   /**
	    * 商户账户
	    *  1393632002  app
	    *  1448523202  服务号1
	    *  1473957302  服务号2
	    */
	   public static final String  MchId = "1473957302";

	   /**
	    * 商户秘钥  32位，在微信商户平台中设置
	    * Z0ivPhjvCydn2sW9VjIosGaSbQ7Zkvl2   app
	    * FhP4lRysibT1QbPQukfEPBXj8lmPyUM5  服务号支付秘钥1
	    * f5e754310bfbd50819172c7afeff25ab   服务号秘钥
	    * f77ac9f341c8c7178dbcf27864e70ede  服务号秘钥2
	    *  1XIrbhRCfrd1fHJoaUvToMG74irvA7A2     
	    *  Q1n1YhpnbrS1KHoWMUZpI4DUrGxlowIt     服务号支付秘钥2
	    */
	   public static final String  AppSercret = "Q1n1YhpnbrS1KHoWMUZpI4DUrGxlowIt";
	   /**
	    * 查询订单地址
	    */
	   public static final String  OrderUrl = "https://api.mch.weixin.qq.com/pay/orderquery";

	   /**
	    * 关闭订单地址
	    */
	   public static final String  CloseOrderUrl = "https://api.mch.weixin.qq.com/pay/closeorder";

	   /**
	    * 申请退款地址https://api.mch.weixin.qq.com/secapi/pay/refund
	    */
	   public static final String  RefundUrl = "https://api.mch.weixin.qq.com/secapi/pay/refund";

	   /**
	    * 查询退款地址
	    */
	   public static final String  RefundQueryUrl = "https://api.mch.weixin.qq.com/pay/refundquery";

	   /**
	    * 下载账单地址
	    */
	   public static final String  DownloadBillUrl = "https://api.mch.weixin.qq.com/pay/downloadbill";

	   /**
	    * 服务器异步通知页面路径
	    */
	   public static String notify_url_1 = "http://wxpay.rongyuecar.com:18080/YlServer/mvc/Wechatpay/expenseNotify";
	   public static String notify_url_2 = "http://wxpay.rongyuecar.com:18080/YlServer/mvc/Wechatpay/rechargeNotify";
	   /**
	    * 页面跳转同步通知页面路径
	    */
//	   public static String return_url = getProperties().getProperty("return_url");

	   /**
	    * 退款通知地址
	    */
//	   public static String refund_notify_url = getProperties().getProperty("refund_notify_url");

	   /**
	    * 退款需要证书文件，证书文件的地址
	    */
//	   public static String refund_file_path = getProperties().getProperty("refund_file_path");

	   /**
	    * 商品名称
	    */
//	   public static String subject =  getProperties().getProperty("subject");

	   /**
	    * 商品描述
	    */
//	   public static String body = "佑途特产";
//
//	   private static  Properties properties;
//
//	   public static synchronized Properties getProperties(){
//	      if(properties == null){
//	         String path = System.getenv(RSystemConfig.KEY_WEB_HOME_CONF) + "/weichart.properties";
//	         properties = PropertiesUtil.getInstance().getProperties(path);
//	      }
//	      return properties;
//	   }

//	}
}