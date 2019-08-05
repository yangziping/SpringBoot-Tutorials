package com.feichaoyu.exception.controller;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author feichaoyu
 * @Date 2019/8/4
 */
@Controller
public class MyErrorController extends BasicErrorController {

    public MyErrorController() {
        super(new DefaultErrorAttributes(), new ErrorProperties());
    }

    /**
     * 定义500的ModelAndView
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(produces = "text/html", value = "/500")
    public ModelAndView errorHtml500(HttpServletRequest request,
                                     HttpServletResponse response) {
        response.setStatus(getStatus(request).value());
        Map<String, Object> model = getErrorAttributes(request,
                isIncludeStackTrace(request, MediaType.TEXT_HTML));
        model.put("msg", "自定义错误信息");
        return new ModelAndView("error/500", model);
    }

    /**
     * 定义500的错误JSON信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/500")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> error500(HttpServletRequest request) {
        Map<String, Object> body = getErrorAttributes(request,
                isIncludeStackTrace(request, MediaType.TEXT_HTML));
        HttpStatus status = getStatus(request);
        return new ResponseEntity<>(body, status);
    }

    /**
     * 定义404的ModelAndView
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(produces = "text/html", value = "/404")
    public ModelAndView errorHtml400(HttpServletRequest request,
                                     HttpServletResponse response) {
        response.setStatus(getStatus(request).value());
        Map<String, Object> model = getErrorAttributes(request,
                isIncludeStackTrace(request, MediaType.TEXT_HTML));
        model.put("msg", "自定义错误信息");
        return new ModelAndView("error/404", model);
    }
}
