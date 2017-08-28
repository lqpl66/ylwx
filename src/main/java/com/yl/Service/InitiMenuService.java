package com.yl.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.stereotype.Service;

import com.yl.Utils.GetExistAccessToken;
import com.yl.bean.Button;
import com.yl.bean.CommonButton;
import com.yl.bean.ComplexButton;
import com.yl.bean.ViewButton;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class InitiMenuService {
	/**
	 * 创建自定义菜单(每天限制1000次)
	 */
	public static int createMenu(String menu) {
		String jsonMenu = JSONObject.fromObject(menu).toString();

		int status = 0;

		// System.out.println("菜单："+jsonMenu);
		String path = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=" + GetExistAccessToken.getToken();
		// https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN
		try {
			URL url = new URL(path);
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setDoOutput(true);
			http.setDoInput(true);
			http.setRequestMethod("POST");
			http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8;");
			http.connect();
			OutputStream os = http.getOutputStream();
			os.write(jsonMenu.getBytes("UTF-8"));
			os.close();

			InputStream is = http.getInputStream();
			int size = is.available();
			byte[] bt = new byte[size];
			is.read(bt);
			String message = new String(bt, "UTF-8");
			JSONObject jsonMsg = JSONObject.fromObject(message);
			status = Integer.parseInt(jsonMsg.getString("errcode"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return status;
	}

	/**
	 * 封装菜单数据
	 */
	public static String getMenu() {
		JSONObject menu = new JSONObject();
		JSONArray button = new JSONArray();
//		// 租导览笔
//		ComplexButton cx_1 = new ComplexButton();
//		cx_1.setName("租导览笔");
//		// cx_1.setName("a");
//		CommonButton cx_1_cn_1 = new CommonButton();
//		cx_1_cn_1.setName("如何租用");
//		// cx_1_cn_1.setName("a1");
//		cx_1_cn_1.setKey("how_rent");
//		cx_1_cn_1.setType("click");
//		ViewButton cx_1_cn_2 = new ViewButton();
//		cx_1_cn_2.setName("租用记录");
//		// cx_1_cn_2.setName("a2");
//		cx_1_cn_2.setUrl(
//				"https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxa095cf3d62e9ea06&redirect_uri=http%3A%2F%2Fwxyl.uto168.com%2Fylwx%2Forder%2ForderGuideList&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect");
//		cx_1_cn_2.setType("view");
//		cx_1.setSub_button(new Button[] { cx_1_cn_1, cx_1_cn_2 });
//		// 下载游乐
//		CommonButton cn_2 = new CommonButton();
//		cn_2.setName("下载游乐");
//		// cn_2.setName("b");
//		cn_2.setKey("download_app");
//		cn_2.setType("click");
//		CommonButton cn_3 = new CommonButton();
//		cn_3.setName("商务合作");
//		// cn_3.setName("c");
//		cn_3.setKey("cooperation");
//		cn_3.setType("click");
//		// Menu menu=new Menu();
//		// menu.setComplexButton(new ComplexButton[]{cx_1});
//		// menu.setButton(new Button[]{cn_2});
//		button.add(cx_1);
//		button.add(cn_2);
//		button.add(cn_3);
		
		// 租导览笔
		ViewButton cv_1 = new ViewButton();
		cv_1.setName("租用记录");
		// cx_1_cn_2.setName("a2");
		cv_1.setUrl(
				"https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxa095cf3d62e9ea06&redirect_uri=http%3A%2F%2Fwxyl.uto168.com%2Fylwx%2Forder%2ForderGuideList&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect");
		cv_1.setType("view");
		
		CommonButton cn_2 = new CommonButton();
		cn_2.setName("如何租用");
		// cn_2.setName("b");
		cn_2.setKey("how_rent");
		cn_2.setType("click");
		
		CommonButton cn_3 = new CommonButton();
		cn_3.setName("商务合作");
		// cn_3.setName("c");
		cn_3.setKey("cooperation");
		cn_3.setType("click");
		button.add(cv_1);
		button.add(cn_2);
		button.add(cn_3);
		
		
		menu.put("button", button);

		return menu.toString();

	}

	public static void main(String args[]) {
		JSONObject m = JSONObject.fromObject(getMenu());
		String f = m.toString();
		System.out.println(f);
		int s = createMenu(f);
		System.out.println(s);
	}
}
