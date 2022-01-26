package com.example.springmvcbasics.basic.response;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ResponseViewController {

    @RequestMapping("/response-view-v1")
    public ModelAndView responseViewV1() {
        return new ModelAndView("response/hello")
                .addObject("data", "parkhanbeen");
    }

    @RequestMapping("/response-view-v2")
    public String responseViewV2(Model model) {
        model.addAttribute("data", "parkhanbeen");
        return "response/hello";
    }

    @RequestMapping("response/hello")
    public void responseViewV3(Model model) {
        model.addAttribute("data", "parkhanbeen");
    }

}
