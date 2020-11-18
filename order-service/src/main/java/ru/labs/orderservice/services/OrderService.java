package ru.labs.orderservice.services;

import org.springframework.stereotype.Service;
import ru.labs.orderservice.entity.Order;
import ru.labs.orderservice.entity.dto.Offer;
import ru.labs.orderservice.repositories.OrderRepository;

import java.util.Date;

@Service
public class OrderService  {

    private final OrderRepository orderRepository;
    private final StatusService statusService;

    public OrderService(OrderRepository orderRepository, StatusService statusService) {
        this.orderRepository = orderRepository;
        this.statusService = statusService;
    }

    public Order findOrderById(Integer id){
        return orderRepository.findById(id).orElse(null);
    }

    public Iterable<Order> findAllOrders(){
        return orderRepository.findAll();
    }

    public void deleteOrderById(Integer id){
        orderRepository.deleteById(id);
    }

    public Order saveOrder(Order order){
        return orderRepository.save(order);
    }

    public boolean validateOrder(Order order) {
        return (order != null &&
                order.getCustomer() != null &&
                order.getCustomer().getId() != null &&
                order.getOffer() != null &&
                order.getOffer().getId() != null &&
                order.getPaid() != null &&
                order.getDeliveryTime() != null &&
                order.getStatus() != null &&
                order.getStatus().getId() != null &&
                order.getName() != null);
    }

    public Order createOrder(Integer customerId, Offer offer) {
        Order order = Order.builder()
                .customerId(customerId)
                .name("Заказ пользователя " + customerId + " на предложение " + offer.getName() + " (ID: " + offer.getId() + ")")
                .offerId(offer.getId())
                .status(statusService.findByName("ACCEPTED"))
                .paid(false)
                .deliveryTime(new Date())
                .build();
        return orderRepository.save(order);
    }
}
