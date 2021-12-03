package com.gsitm.intern.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "sms_notice")
@Getter
@Setter
@ToString
public class SmsNotice extends BaseEntity {
    @Id
    @Column(name = "sms_notice_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Lob
    private String smsContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

}
