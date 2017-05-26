package com.pcq;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Administrator on 2017/5/22 0022.
 */
@Controller
@RequestMapping("/testService")
public class TestService {
    @RequestMapping("test")
    @ResponseBody
    public String test(){
        return "123";

    }
}
