package com.TH.demo.Controller;

import com.TH.demo.Model.Product;
import com.TH.demo.Services.ProductServices;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Principal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Value("${gemini.api-key}")
    private String geminiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String geminiModel;

    @Autowired
    private ProductServices productServices;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/user/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> body, Principal principal) {
        String message = body.getOrDefault("message", "");
        if (message == null || message.isBlank()) {
            return Map.of("reply", "");
        }
        String pageContext = body.getOrDefault("pageContext", "");
        String username = principal != null ? principal.getName() : null;

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + geminiModel + ":generateContent?key=" + geminiKey;

            String systemInstruction = buildSystemInstruction(username, pageContext);

            String payload = mapper.writeValueAsString(Map.of(
                    "systemInstruction", Map.of(
                            "parts", List.of(Map.of("text", systemInstruction))
                    ),
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", message))
                    ))
            ));

            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                log.warn("[Gemini trả về HTTP {}: {}]", resp.statusCode(), resp.body());
                return Map.of("reply", "Xin lỗi, AI tạm thời không phản hồi.");
            }

            JsonNode root = mapper.readTree(resp.body());
            String reply = root.path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text").asText("").trim();
            if (reply.isEmpty()) {
                reply = "(AI không trả lời được câu này)";
            }
            return Map.of("reply", reply);
        } catch (Exception e) {
            log.error("[Lỗi khi gọi Gemini]", e);
            return Map.of("reply", "Xin lỗi, có lỗi khi gọi AI.");
        }
    }

    private String buildSystemInstruction(String username, String pageContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là trợ lý ảo (chatbot) của TH Shop — cửa hàng điện tử trực tuyến tại Việt Nam ")
          .append("chuyên bán máy ảnh, ống kính, camera giám sát và phụ kiện công nghệ. ")
          .append("Luôn trả lời NGẮN GỌN, lịch sự, bằng TIẾNG VIỆT. ")
          .append("Nếu khách hỏi về giá, tồn kho, hoặc gợi ý sản phẩm, hãy dùng đúng dữ liệu trong DANH MỤC bên dưới. ")
//          .append("Không bịa sản phẩm không có trong danh mục. ")
          .append("Nếu khách hỏi về đặt hàng/thanh toán: hướng dẫn họ Thêm vào giỏ → Giỏ hàng → Đặt hàng → quét QR thanh toán → bấm \"Tôi đã chuyển khoản\". ")
          .append("Nếu không biết, hãy nói thẳng và đề nghị liên hệ shop qua hotline 0327644604 hoặc email nguyentienhung04042004@gmail.com.\n\n");

        if (username != null && !username.isEmpty()) {
            sb.append("Khách hàng đang đăng nhập: ").append(username).append("\n");
        }
        if (pageContext != null && !pageContext.isBlank()) {
            sb.append("Trang khách đang xem: ").append(pageContext)
              .append(" (").append(describePage(pageContext)).append(")\n");
        }

        sb.append("\n=== DANH MỤC SẢN PHẨM ===\n");
        try {
            Page<Product> products = productServices.getAllProduct(PageRequest.of(0, 50));
            if (products.isEmpty()) {
                sb.append("(Hiện chưa có sản phẩm nào trong kho)\n");
            } else {
                for (Product p : products) {
                    sb.append("- ").append(p.getName())
                      .append(" | Giá: ").append(p.getPrice() == null ? 0L : p.getPrice().longValue()).append("đ")
                      .append(" | Tồn kho: ").append(p.getQuantity()).append(" sp")
                      .append("\n");
                }
            }
        } catch (Exception e) {
            log.warn("[Không lấy được danh mục sản phẩm cho chatbot: {}]", e.getMessage());
        }
        return sb.toString();
    }

    private String describePage(String path) {
        if (path == null) return "";
        if (path.startsWith("/user/cart")) return "trang giỏ hàng";
        if (path.startsWith("/user/order/") && path.endsWith("/pay")) return "trang thanh toán QR";
        if (path.startsWith("/user/order/success")) return "trang xác nhận đặt hàng thành công";
        if (path.startsWith("/user/order")) return "trang đặt hàng / checkout";
        if (path.startsWith("/user/history")) return "trang lịch sử đơn hàng";
        if (path.startsWith("/user/home")) return "trang chủ";
        if (path.startsWith("/user/about")) return "trang thông tin cá nhân";
        if (path.contains("chitietsanpham") || path.contains("/product/")) return "trang chi tiết sản phẩm";
        return "trang khác";
    }
}
