package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.OrderDetailRequest;
import org.example.projetc_backend.dto.OrderDetailResponse;
import org.example.projetc_backend.service.OrderDetailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/order-details")
public class OrderDetailController {

    private final OrderDetailService orderDetailService;

    public OrderDetailController(OrderDetailService orderDetailService) {
        this.orderDetailService = orderDetailService;
    }

    // THAY ĐỔI LỚN: Loại bỏ endpoint POST để tạo OrderDetail riêng lẻ
    /*
    @PostMapping
    public ResponseEntity<OrderDetailResponse> createOrderDetail(@Valid @RequestBody OrderDetailRequest request) {
        try {
            OrderDetailResponse newOrderDetail = orderDetailService.createOrderDetail(request);
            return new ResponseEntity<>(newOrderDetail, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    */

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> getOrderDetailById(@PathVariable Integer id) {
        try {
            OrderDetailResponse orderDetail = orderDetailService.getOrderDetailById(id);
            return new ResponseEntity<>(orderDetail, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderDetailResponse>> getOrderDetailsByOrderId(@PathVariable Integer orderId) {
        try {
            List<OrderDetailResponse> orderDetails = orderDetailService.getOrderDetailsByOrderId(orderId);
            return new ResponseEntity<>(orderDetails, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<OrderDetailResponse>> getAllOrderDetails() {
        List<OrderDetailResponse> orderDetails = orderDetailService.getAllOrderDetails();
        return new ResponseEntity<>(orderDetails, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> updateOrderDetail(
            @PathVariable Integer id,
            @Valid @RequestBody OrderDetailRequest request) {
        try {
            OrderDetailResponse updatedOrderDetail = orderDetailService.updateOrderDetail(id, request);
            return new ResponseEntity<>(updatedOrderDetail, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderDetail(@PathVariable Integer id) {
        try {
            orderDetailService.deleteOrderDetail(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}