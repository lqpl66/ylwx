package com.yl.Utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class GetProperties {

	public static String getXmlurl() {
		Resource resource = null;
		Properties props = null;
		String xmlurl = null;
		try {
			resource = new ClassPathResource("/resources.properties");
			props = PropertiesLoaderUtils.loadProperties(resource);
			xmlurl = (String) props.get("xmlurl");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return xmlurl;
	}

	// 用于返回数据时景区图片回显路径拼接
	public static String getscenicImgUrl() {
		Resource resource = null;
		Properties props = null;
		String fileUrl = null;
		try {
			resource = new ClassPathResource("/resources.properties");
			props = PropertiesLoaderUtils.loadProperties(resource);
			fileUrl = (String) props.get("scenicImgUrl");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileUrl;
	}

	public static String getServerUrl() {
		Resource resource = null;
		Properties props = null;
		String fileUrl = null;
		try {
			resource = new ClassPathResource("/resources.properties");
			props = PropertiesLoaderUtils.loadProperties(resource);
			fileUrl = (String) props.get("serverUrl");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileUrl;
	}

	public static String getNotify_url_Wechat_guide() {
		Resource resource = null;
		Properties props = null;
		String fileUrl = null;
		try {
			resource = new ClassPathResource("/resources.properties");
			props = PropertiesLoaderUtils.loadProperties(resource);
			fileUrl = (String) props.get("notify_url_Wechat_guide");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileUrl;
	}
	//refund_notify_url_guide
	public static String refund_notify_url_guide() {
		Resource resource = null;
		Properties props = null;
		String fileUrl = null;
		try {
			resource = new ClassPathResource("/resources.properties");
			props = PropertiesLoaderUtils.loadProperties(resource);
			fileUrl = (String) props.get("refund_notify_url_guide");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileUrl;
	}
	
	public static BigDecimal refund_deductionPrice() {
		Resource resource = null;
		Properties props = null;
		String fileUrl = null;
		BigDecimal dd =null;
		try {
			resource = new ClassPathResource("/resources.properties");
			props = PropertiesLoaderUtils.loadProperties(resource);
			fileUrl = (String) props.get("deductionPrice");
			 dd = new BigDecimal(fileUrl);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dd;
	}
	
}
