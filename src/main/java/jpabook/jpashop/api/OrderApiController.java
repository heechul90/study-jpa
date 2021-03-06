package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.service.OrderSearch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping(value = "/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> orders = orderRepository.findAllByCriteria(new OrderSearch());
        for (Order order : orders) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(orderItem -> orderItem.getItem().getName());
        }
        return orders;
    }

    @GetMapping(value = "/api/v2/orders")
    public JsonResult ordersV2() {
        List<Order> orders = orderRepository.findAllByCriteria(new OrderSearch());
        List<OrderDto> resultList = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(Collectors.toList());
        return new JsonResult(resultList.size(), resultList);
    }

    @GetMapping(value = "/api/v3/orders")
    public JsonResult ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        for (Order order : orders) {
            System.out.println("order ref = " + order + " id= " + order.getId());
        }

        List<OrderDto> resultList = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(Collectors.toList());
        return new JsonResult(resultList.size(), resultList);
    }

    @GetMapping(value = "/api/v3.1/orders")
    public JsonResult ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> resultList = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(Collectors.toList());
        return new JsonResult(resultList.size(), resultList);
    }

    @GetMapping(value = "/api/v4/orders")
    public JsonResult ordersV4() {
        List<OrderQueryDto> resultList = orderQueryRepository.findOrderQueryDtos();
        return new JsonResult(resultList.size(), resultList);
    }

    @GetMapping(value = "/api/v5/orders")
    public JsonResult ordersV5() {
        List<OrderQueryDto> resultList = orderQueryRepository.findAllByDto_optimization();

        return new JsonResult(resultList.size(), resultList);
    }

    @GetMapping(value = "/api/v6/orders")
    public JsonResult ordersV6() {
        List<OrderFlatDto> orders = orderQueryRepository.findAllByDto_flat();
        List<OrderQueryDto> resultList = orders.stream()
                .collect(Collectors.groupingBy(order -> new OrderQueryDto(order.getOrderId(), order.getName(), order.getOrderDate(), order.getOrderStatus(), order.getAddress()),
                        Collectors.mapping(order -> new OrderItemQueryDto(order.getOrderId(), order.getItemName(), order.getOrderPrice(), order.getCount()), Collectors.toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(Collectors.toList());

        return new JsonResult(resultList.size(), resultList);
    }

    @Getter
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }


    @Data
    @AllArgsConstructor
    static class JsonResult<T> {
        private int count;
        private T data;
    }
}
