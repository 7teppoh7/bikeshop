package ru.labs.orderservice.services;

import org.springframework.stereotype.Service;
import ru.labs.orderservice.entity.Order;
import ru.labs.orderservice.repositories.OrderRepository;

@Service
public class OrderService  {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
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
}
