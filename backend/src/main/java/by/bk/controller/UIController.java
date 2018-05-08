package by.bk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Sergey Koval
 */
@Controller
public class UIController {
    @RequestMapping(value = "/**/{path:[^\\.]+}", method = RequestMethod.GET)
    public String redirect() {
        return "forward:/";
    }
}