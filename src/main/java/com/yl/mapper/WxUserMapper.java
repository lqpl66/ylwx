package com.yl.mapper;

import java.util.Map;

import com.yl.bean.WxUser;

public interface WxUserMapper {
	// 添加用户
	void addWxUser(Map<String, Object> map);

	// 获取用户信息
	WxUser getWxUser(Map<String, Object> map);

	// 删除用户信息
	void deleteWxUser(Map<String, Object> map);
}
