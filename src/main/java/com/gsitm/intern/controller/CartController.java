package com.gsitm.intern.controller;

import com.gsitm.intern.dto.CartDetailDto;
import com.gsitm.intern.dto.CartItemDto;
import com.gsitm.intern.dto.CartOrderDto;
import com.gsitm.intern.repository.MemberRepository;
import com.gsitm.intern.service.CartService;
import com.gsitm.intern.service.EmailService;
import com.gsitm.intern.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final MemberRepository memberRepository;

    @PostMapping(value = "/cart")
    public @ResponseBody
    ResponseEntity order(@RequestBody @Valid CartItemDto cartItemDto,
                         BindingResult bindingResult, Principal principal){

        //cartItemDto 객체에 데이터 바인딩 시 에러 있는지 검사
        if(bindingResult.hasErrors()){
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }
            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }
        //현재 로그인한 사용자의 이메일 정보 저장
        String email = principal.getName();
        Long cartItemId;

        try {
            //화면으로부터 넘어온 장바구니에 담을 상품 정보와 현재 로그인한 사용자의 이메일 정보를 이용해서
            //장바구니에 상품을 담는 로직 호출
            cartItemId = cartService.addCart(cartItemDto, email);
        } catch(Exception e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        //결과값으로 생성된 장바구니 상품 아이디와 요청 성공 HTTP 응답 상태코드 반환
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @GetMapping(value = "/cart")
    public String orderHist(Principal principal, Model model){
        //현재 로그인한 사용자의 이메일 정보를 이용해 장바구니에 담겨있는 상품 정보 조회
        List<CartDetailDto> cartDetailList = cartService.getCartList(principal.getName());
        //조회한 장바구니 상품 정보 뷰로 전달
        model.addAttribute("cartItems", cartDetailList);
        return "cart/cartList";
    }

    @PatchMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity updateCartItem(@PathVariable("cartItemId") Long cartItemId,
                                                       int count, Principal principal){

        //장바구니에 담겨있는 상품 개수를 0개 이하로 업데이트 요청하면 에러 메시지 담아서 반환
        if(count <= 0){
            return new ResponseEntity<String>("최소 1개 이상 담아주세요.", HttpStatus.BAD_REQUEST);
        }
        //수정 권한 체크
        else if(!cartService.validateCartItem(cartItemId, principal.getName())){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        //장바구니 상품 개수 업데이트
        cartService.updateCartItemCount(cartItemId, count);
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @DeleteMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity deleteCartItem(@PathVariable("cartItemId") Long cartItemId,
                                                     Principal principal){

        //수정 권한 체크
        if(!cartService.validateCartItem(cartItemId, principal.getName())){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        //해당 장바구니 상품 삭제
        cartService.deleteCartItem(cartItemId);
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @PostMapping(value = "/cart/orders")
    public @ResponseBody ResponseEntity orderCartItem(@RequestBody CartOrderDto cartOrderDto,
                                                      Principal principal){

        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();

        //주문할 상품 선택하지 않았는지 체크
        if(cartOrderDtoList == null || cartOrderDtoList.size() == 0){
            return new ResponseEntity<String>("주문할 상품을 선택해주세요.", HttpStatus.FORBIDDEN);
        }

        //주문 권한 체크
        for(CartOrderDto cartOrder : cartOrderDtoList){
            if(!cartService.validateCartItem(cartOrder.getCartItemId(), principal.getName())){
                return new ResponseEntity<String>("주문 권한이 없습니다.", HttpStatus.FORBIDDEN);
            }
        }
        //주문 로직 호출 결과 생성된 주문 변화 반환
        Long orderId = cartService.orderCartItem(cartOrderDtoList, principal.getName());

        String email = principal.getName();
        String phone = memberRepository.findByEmail(email).getPhone();
        String notice = cartOrderDto.getNotice();

        if(notice.equals("email")){
            emailService.sendCartOrderEmail(email, orderId);
        } else if(notice.equals("sms")){
            smsService.sendCartOrderSms(phone, orderId);
        }
        //생성된 주문 번호와 요청이 성공한 HTTP 응답 상태 코드 반환
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }
}
