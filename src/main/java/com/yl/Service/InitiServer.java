package com.yl.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.yl.Utils.SignUtils;

@Service
public class InitiServer {
     public  String getEchostr(HttpServletRequest request, HttpServletResponse response){
	        // 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。  
	        String signature = request.getParameter("signature");  
	        // 时间戳  
	        String timestamp = request.getParameter("timestamp");  
	        // 随机数  
	        String nonce = request.getParameter("nonce");  
	        // 随机字符串  
	        String echostr = request.getParameter("echostr");  
            // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，否则接入失败  
	        System.out.println(signature);
	        System.out.println(timestamp);
	        System.out.println(nonce);
	        System.out.println(echostr);
            if (!SignUtils.checkSignature(signature, timestamp, nonce)) {  
            	echostr = null;
            } 
    	 return echostr;
     }
	    
	
}
