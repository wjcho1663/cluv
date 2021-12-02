package com.gsitm.intern.service;

import com.gsitm.intern.dto.CartOrderDto;
import com.gsitm.intern.dto.MemberFormDto;
import com.gsitm.intern.dto.OrderDto;
import com.gsitm.intern.entity.AuthToken;
import com.gsitm.intern.entity.Item;
import com.gsitm.intern.entity.Order;
import com.gsitm.intern.entity.OrderItem;
import com.gsitm.intern.repository.ItemRepository;
import com.gsitm.intern.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.json.simple.JSONObject;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityNotFoundException;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class SmsService {

    private final AuthTokenService authTokenService;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    @Async
    public void sendSms(String phone, String text) {
        String api_key = "NCS1TB65HVL5RIAO";
        String api_secret = "EWBEAGXE92QMSABDLAP6BYLRNHCUZL8O";
        Message coolsms = new Message(api_key, api_secret);
        HashMap<String, String> params = new HashMap<String, String>();

        params.put("to", phone);
        params.put("from", "010-3583-7031");
        params.put("type", "SMS");
        params.put("text", text);
        params.put("app_version", "test app 1.2");

        try {
            JSONObject obj = (JSONObject) coolsms.send(params);
            System.out.println(obj.toString());
        } catch (CoolsmsException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getCode());
        }
    }
//
//    public void sendSmsAuthCode(String email) {
//        String subject = "인증코드입니다.";
//        AuthToken authToken = authTokenService.getToken(email);
//        String text = "인증코드는 " + authToken.getCode() + "입니다.";
//        sendEmail(email, subject, text);
//    }
//
    public void sendOrderSms(String phone, OrderDto orderDto){

        Item item = itemRepository.findById(orderDto.getItemId()).
                orElseThrow(EntityNotFoundException::new);

        String text = "[GS SHOP] 주문 상품 내역\n" + "주문 상품 : " + item.getItemNm() + "\n주문 수량 : " + orderDto.getCount() +
                "\n주문 금액 : " + item.getPrice() * orderDto.getCount() + "원";
        sendSms(phone, text);
    }

    public void sendCartOrderSms(String phone, Long orderId){

        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);

        String smsText = "[GS SHOP]주문상품 내역\n";

        StringBuffer sb = new StringBuffer(smsText);
        //TODO. 장바구니 데이터 불러오기
        for(OrderItem orderItem : order.getOrderItems()) {
            sb.append(orderItem.getItem().getItemNm());
            sb.append("(");
            sb.append(orderItem.getItem().getPrice());
            sb.append(" 원) x ");
            sb.append(orderItem.getCount() + "개\n");
        }

        sb.append("\n주문 금액 : ");
        sb.append(order.getTotalPrice());
        sb.append("원\n");

        String text = sb.toString();

        sendSms(phone, text);
    }

//
}
