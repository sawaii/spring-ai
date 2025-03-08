package com.springai.mobile.automation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for web UI pages
 */
@Controller
public class WebController {

    /**
     * Home page
     * @return model and view
     */
    @GetMapping("/")
    public ModelAndView index() {
        return new ModelAndView("index");
    }
} 