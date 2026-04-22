package com.dazhuangyuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dazhuangyuan.common.Result;
import com.dazhuangyuan.entity.*;
import com.dazhuangyuan.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 */
@RestController
@RequestMapping("/api/pay")
public class PayController {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 产品列表
     */
    @GetMapping("/products")
    public Result<?> products() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1).orderByAsc(Product::getSortOrder);
        return Result.ok(productMapper.selectList(wrapper));
    }

    /**
     * 创建订单
     */
    @PostMapping("/createOrder")
    public Result<?> createOrder(@RequestBody Map<String, Object> params,
                                  javax.servlet.http.HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long productId = Long.valueOf(params.get("productId").toString());
        String payType = (String) params.get("payType");

        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() != 1) {
            return Result.error(404, "产品不存在或已下架");
        }

        User user = userMapper.selectById(userId);
        if (user.getVipLevel() >= getVipLevel(product.getProductType())) {
            return Result.error(400, "您已拥有同等或更高等级的VIP权限");
        }

        // 生成订单号
        String orderNo = "DY" + System.currentTimeMillis() + 
                String.format("%04d", new java.util.Random().nextInt(10000));

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setAmount(product.getPrice());
        order.setPayType(payType);
        order.setPayStatus(0);
        order.setCreatedAt(java.time.LocalDateTime.now());
        order.setExpireAt(java.time.LocalDateTime.now().plusMinutes(30));
        orderMapper.insert(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("amount", product.getPrice());
        result.put("productName", product.getName());
        result.put("expireTime", order.getExpireAt());

        // TODO: 对接真实支付SDK
        // 微信支付 → 返回JSAPI参数
        // 支付宝 → 返回支付表单
        result.put("payParams", new HashMap<>());
        result.put("payUrl", "");

        return Result.ok("订单创建成功", result);
    }

    /**
     * 微信支付回调
     */
    @PostMapping("/notify/wechat")
    public String wechatNotify(@RequestBody String body) {
        // TODO: 对接微信支付SDK验证签名
        // 1. 验证签名
        // 2. 解析回调数据
        // 3. 更新订单状态
        // 4. 更新用户VIP等级
        // 5. 返回success给微信
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>";
    }

    /**
     * 支付宝回调
     */
    @PostMapping("/notify/alipay")
    public String alipayNotify(@RequestParam Map<String, String> params) {
        // TODO: 对接支付宝SDK验证签名
        // 1. 验证签名
        // 2. 更新订单状态
        // 3. 更新用户VIP等级
        // 4. 返回success给支付宝
        return "success";
    }

    /**
     * 查询订单状态
     */
    @GetMapping("/status/{orderNo}")
    public Result<?> orderStatus(@PathVariable String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = orderMapper.selectOne(wrapper);
        if (order == null) return Result.error(404, "订单不存在");
        return Result.ok(order);
    }

    private int getVipLevel(String productType) {
        switch (productType) {
            case "basic": return 1;
            case "professional": return 2;
            case "premium": return 3;
            default: return 0;
        }
    }
}
