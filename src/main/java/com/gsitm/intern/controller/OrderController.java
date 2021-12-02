package com.gsitm.intern.controller;

import com.gsitm.intern.dto.MemberFormDto;
import com.gsitm.intern.dto.OrderDto;
import com.gsitm.intern.dto.OrderHistDto;
import com.gsitm.intern.entity.Member;
import com.gsitm.intern.repository.MemberRepository;
import com.gsitm.intern.service.EmailService;
import com.gsitm.intern.service.OrderService;
import com.gsitm.intern.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final MemberRepository memberRepository;

    @PostMapping(value = "/order")
    public @ResponseBody ResponseEntity order (@RequestBody @Valid OrderDto orderDto,
                                               BindingResult bindingResult, Principal principal){
        //주문 정보를 받는 orderDto 객체에 데이터 바인딩 시 에러 있는지 검사
        if(bindingResult.hasErrors()){
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }
            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }
        String email = principal.getName();
        String phone = memberRepository.findByEmail(email).getPhone();
        Long orderId;

        String notice = orderDto.getNotice();

        try {
            //화면에서 넘어오는 주문 정보와 회원의 이메일 정보를 이용해 주문로직 호출
            orderId = orderService.order(orderDto, email);

            if(notice.equals("email")){
                emailService.sendOrderEmail(email, orderDto);
            } else if(notice.equals("sms")){
                smsService.sendOrderSms(phone, orderDto);
            }

        } catch(Exception e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }

    @GetMapping(value = {"/orders", "/orders/{page}"})
    public String orderHist(@PathVariable("page") Optional<Integer> page,
                            Principal principal, Model model){
        //한 번에 갖고 올 주문 개수 4개로 설정
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0,4);

        Page<OrderHistDto> orderHistDtoList = orderService.getOrderList(principal.getName(), pageable);

        model.addAttribute("orders", orderHistDtoList);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("maxPage", 5);

        return "order/orderHist";
    }

    @PostMapping("/order/{orderId}/cancel")
    public @ResponseBody ResponseEntity cancelOrder(@PathVariable("orderId") Long orderId, Principal principal){

        //자바스크립트에서 취소할 주문 번호는 조작이 가능, 다른 사람의 주문 취소하지 못하도록 주문 취소 권한 검사
        if(!orderService.validateOrder(orderId, principal.getName())){
            return new ResponseEntity<String>("주문 취소 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        //주문 취소 로직 호출
        orderService.cancelOrder(orderId);
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }

}
