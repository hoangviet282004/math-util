package com.project.KoiBookingSystem.service;

import com.project.KoiBookingSystem.model.request.BookingRequest;
import com.project.KoiBookingSystem.model.response.BookingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Service
public class VnpayServiceAvaliableTour {


    // này tui chia ra 1 khu vực riêng để dùng cái vnpay trả về url

    @Autowired
    BookingService bookingService;

    public String createUrl(BookingRequest bookingRequest) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime createDate = LocalDateTime.now();
        String formattedCreateDate = createDate.format(formatter);


        BookingResponse booking = bookingService.createTicket(bookingRequest);
        float money = booking.getTotalPrice() * 100;  // để mất thập phân kiểu int (sân nhà của ng ta)
        String amount = String.valueOf((int) money);
        // muốn tạo order thì chỉ cần thêm hàm order ở đây


        String tmnCode = "K8GYRSRJ";
        String secretKey = "GZRDJNWZ5DZCJF1PF3WV4MP7YX7ZT8H6";
        String vnpUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        String returnUrl = "https://mail.google.com/mail/u/0/?tab=rm&ogbl#inbox/?orderID=" + booking.getBookingId();
        // trang thông báo thành công (font-end) làm
        // đính kèm Id cho orders (sản phẩm)
        String currCode = "VND";

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_CurrCode", currCode);
        vnpParams.put("vnp_TxnRef", booking.getBookingId()); //!!! Do Orders id đang là kiểu UUID (String)
        vnpParams.put("vnp_OrderInfo", "Thanh toan cho ma GD: " + booking.getBookingId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Amount", amount);
        //!!! do vnp_Amount là do người ta quy định, dùng ké thì biết điều tý => phải đúng định dạng

        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_CreateDate", formattedCreateDate);
        vnpParams.put("vnp_IpAddr", "128.199.178.23");

        StringBuilder signDataBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            signDataBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            signDataBuilder.append("=");
            signDataBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            signDataBuilder.append("&");
        }
        signDataBuilder.deleteCharAt(signDataBuilder.length() - 1); // Remove last '&'

        String signData = signDataBuilder.toString();
        String signed = generateHMAC(secretKey, signData);

        vnpParams.put("vnp_SecureHash", signed);

        StringBuilder urlBuilder = new StringBuilder(vnpUrl);
        urlBuilder.append("?");
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            urlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            urlBuilder.append("=");
            urlBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            urlBuilder.append("&");
        }
        urlBuilder.deleteCharAt(urlBuilder.length() - 1); // Remove last '&'

        return urlBuilder.toString();
    }

    private String generateHMAC(String secretKey, String signData) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmacSha512.init(keySpec);
        byte[] hmacBytes = hmacSha512.doFinal(signData.getBytes(StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();
        for (byte b : hmacBytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
