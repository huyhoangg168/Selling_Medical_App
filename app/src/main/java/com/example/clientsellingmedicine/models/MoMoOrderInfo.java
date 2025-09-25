package com.example.clientsellingmedicine.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoMoOrderInfo {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private String amount;
    private String orderInfo;
    private String orderType;
    private String transId;
    private String resultCode;
    private String message;
    private String payType;
    private String responseTime;
    private String extraData;
    private String signature;

//    public MoMoOrderInfo(String partnerCode, String orderId, String requestId, String amount, String orderInfo, String orderType, String transId, String resultCode, String message, String payType, String responseTime, String extraData, String signature) {
//        this.signature = signature;
//        this.extraData = extraData;
//        this.responseTime = responseTime;
//        this.payType = payType;
//        this.message = message;
//        this.resultCode = resultCode;
//        this.transId = transId;
//        this.orderType = orderType;
//        this.orderInfo = orderInfo;
//        this.amount = amount;
//        this.requestId = requestId;
//        this.orderId = orderId;
//        this.partnerCode = partnerCode;
//    }
}
