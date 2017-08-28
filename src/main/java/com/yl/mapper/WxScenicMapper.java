package com.yl.mapper;

import java.util.List;
import java.util.Map;

import com.yl.bean.BaseGuideFile;
import com.yl.bean.BindGuideLog;
import com.yl.bean.Guide;
import com.yl.bean.GuideClick;
import com.yl.bean.OrderGuide;
import com.yl.bean.RefundReason;
import com.yl.bean.Scenic;
import com.yl.bean.ScenicAdmin;
import com.yl.bean.ScenicGuide;
import com.yl.bean.ScenicGuideFee;
import com.yl.bean.ScenicSite;
import com.yl.bean.Version;

public interface WxScenicMapper {

	// 获取景区信息
	Scenic getScenic(Map<String, Object> map);

	// 获取景区下导览笔信息
	List<ScenicGuide> getScenicGuide(Map<String, Object> map);

	// 用户的租用导览笔记录
	List<OrderGuide> getOrderGuide(Map<String, Object> map);

	OrderGuide getOrderGuideInfo(Map<String, Object> map);

	// 景区子管理员信息
	ScenicAdmin getScenicAdmin(Map<String, Object> map);

	// 景区子管理员更新信息
	void updateScenicAdmin(Map<String, Object> map);

	// 获取景区站点信息
	List<ScenicSite> getScenicSite(Map<String, Object> map);

	// 获取导览笔绑定和解绑记录
	List<BindGuideLog> getBindGuideLog(Map<String, Object> map);

	// 获取导览笔操作列表
	List<BindGuideLog> getBindGuideLogList(Map<String, Object> map);

	// 添加导览笔绑定和解绑记录
	void saveBindGuideLog(Map<String, Object> map);

	// 添加导览笔某景点的点击播放次数
	void saveGuideClick(Map<String, Object> map);

	// 更新导览笔某景点的点击播放次数
	void updateGuideClick(Map<String, Object> map);

	GuideClick getGuideClick(Map<String, Object> map);

	BaseGuideFile getBaseGuideFile(Map<String, Object> map);

	// 景区导览笔费用详情
	ScenicGuideFee getScenicGuideFee(Map<String, Object> map);

	// 景区导览笔
	Guide getGuide(Map<String, Object> map);

	// 获取退还原因
	List<RefundReason> getRefundReason();

	// 版本校验
	List<Version> getVersion(Map<String, Object> map);

	public void operateLoginLog(Map<String, Object> map);
}
