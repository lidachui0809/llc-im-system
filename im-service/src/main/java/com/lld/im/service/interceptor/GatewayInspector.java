
package com.lld.im.service.interceptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.enums.GateWayErrorCode;
import com.lld.im.common.exception.ApplicationExceptionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Objects;

/**
 * 鉴权
 */
@Component
@Slf4j
public class GatewayInspector implements HandlerInterceptor {

    @Autowired
    private SignCheck signCheck;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String appId =request.getParameter("appId");
        String s = request.getRequestURL().toString();
        if (StrUtil.isBlank(appId)) {
            processResp(ResponseVO.errorResponse(GateWayErrorCode.APPID_NOT_EXIST), response);
            return false;
        }
        String userId =request.getParameter("userId");
        if (StrUtil.isBlank(userId)) {
            processResp(ResponseVO.errorResponse(GateWayErrorCode.OPERATER_NOT_EXIST), response);
            return false;
        }
        String identify = request.getParameter("identify");
        if (StrUtil.isBlank(identify)) {
            processResp(ResponseVO.errorResponse(GateWayErrorCode.USERSIGN_NOT_EXIST), response);
            return false;
        }
        String userSign = request.getHeader("Authorization");
        if (StrUtil.isBlank(userSign)) {
            processResp(ResponseVO.errorResponse(GateWayErrorCode.USERSIGN_NOT_EXIST), response);
            return false;
        }
        //TODO 签名检验
        ApplicationExceptionEnum resultEnum = signCheck.checkSign(appId, userId, userSign, identify);
        if (resultEnum != BaseErrorCode.SUCCESS) {
            processResp(ResponseVO.errorResponse(GateWayErrorCode.USERSIGN_OPERATE_NOT_MATE), response);
            return false;
        }
        return true;
    }

    private String getBodyParamV(JSONObject jsonObject,String paramKey) {
        if(jsonObject==null){
            return "";
        }
        String string = jsonObject.getString(paramKey);
        if(string==null)
            return "";
        return string;
    }

    private JSONObject getDataJsonObj(HttpServletRequest request)  {
        try {
            ServletInputStream inputStream = request.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String str="";
            StringBuilder data = new StringBuilder();
            while ((str=reader.readLine())!=null){
                data.append(str);
            }
            inputStream.close();
            return JSONObject.parseObject(data.toString());
        } catch (IOException e) {
            log.error("解析发生异常！");
            return null;
        }
    }

    private void processResp(ResponseVO vo, HttpServletResponse response) {
        try {
            log.error("im-server 网关拦截 ms={}",vo.getMsg());
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(JSONObject.toJSONString(vo));
        } catch (IOException e) {
            log.error(" processResp error: {}", e.getMessage());
        }
    }

}
