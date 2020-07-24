package com.qianlima.demo.webflux.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/15 16:30
 * @Version 0.0.1
 */
@Slf4j
@RestControllerAdvice
public class WebFluxControllerAdvice {

    @ExceptionHandler(Exception.class)
    public Mono<String> handleError1(Exception e) {
        log.error("exception::::",e);
        return Mono.just("have error~");
    }
    // need web-mvc model
//    public String handleError1(Exception e, RedirectAttributes redirectAttributes) {
//        redirectAttributes.addFlashAttribute("message", e.getCause().getMessage());
//        return "redirect:/uploadStatus";
//    }
}
