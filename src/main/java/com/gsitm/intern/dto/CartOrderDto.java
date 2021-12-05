package com.gsitm.intern.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class CartOrderDto {

    private Long cartItemId;

    //장바구니에서 여러 개의 상품 주문, 자기자신을 List로 갖도록 구현
    private List<CartOrderDto> cartOrderDtoList;

    private String notice;  //알림 전송 방식 추가
}
