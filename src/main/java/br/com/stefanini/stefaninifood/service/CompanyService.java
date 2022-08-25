package br.com.stefanini.stefaninifood.service;

import br.com.stefanini.stefaninifood.controller.dto.CompletedOrderDTO;
import br.com.stefanini.stefaninifood.controller.dto.ReceivedOrderDTO;
import br.com.stefanini.stefaninifood.model.Consumer;
import br.com.stefanini.stefaninifood.model.Order;
import br.com.stefanini.stefaninifood.model.OrderedItens;
import br.com.stefanini.stefaninifood.model.StatusOrder;
import br.com.stefanini.stefaninifood.repository.ConsumerRepository;
import br.com.stefanini.stefaninifood.repository.OrderRepository;
import br.com.stefanini.stefaninifood.repository.OrderedItensRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    @Autowired
    OrderedItensRepository orderedItensRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ConsumerRepository consumerRepository;

    public ResponseEntity<?> retrieveOrdersByCompany(Long id){
        try {
            List<OrderedItens> orders = orderedItensRepository.findByCompanyId(id);
            if (orders.size() > 0) {
                List<Object[]> inPreparationList = orderRepository.findReceivedOrdersByCompany(id);
                for (int i = 0; i < inPreparationList.size(); i++) {
                    System.out.println(inPreparationList.get(i)[6].getClass().getSimpleName());
                    if (inPreparationList.get(i)[6].getClass().getSimpleName() != "BigInteger") {
                        Long consumerId = Long.parseLong(inPreparationList.get(i)[6].toString());
//                        String adress = consumerRepository.findById(consumerId).get().getAddress().toString();
//                        inPreparationList.get(i)[6] = adress;
                    }
                    System.out.println(inPreparationList.get(i)[6].getClass().getSimpleName());
                }
                return ResponseEntity.status(HttpStatus.OK).body(ReceivedOrderDTO.converter(inPreparationList));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Não há pedidos em aberto para esta empresa");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Erro ao carregar pedidos.");
        }
    }

    public ResponseEntity<?> completeOrder(Long id){
        try {
            Optional<Order> order = orderRepository.findById(id);
            if (order.isPresent()) {
                if (order.get().getStatus().equals(StatusOrder.EM_PREPARACAO)) {
                    order.get().setStatus(StatusOrder.FINALIZADO);
                    orderRepository.save(order.get());
                    return ResponseEntity.status(HttpStatus.OK).body("Pedido finalizado com sucesso.");
                }
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Pedido já finalizado.");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pedido não encontrado.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Erro ao carregar pedidos.");
        }
    }

    public ResponseEntity<?> salesListById(Long id){
        try {
            List<Object[]> orders = orderRepository.findCompletedOrders(id);
            if (orders.size() > 0) {
                List<CompletedOrderDTO> soldList = CompletedOrderDTO.converter(orders);
                return ResponseEntity.status(HttpStatus.OK).body(soldList);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum pedido finalizado.");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Erro ao carregar pedidos.");
        }
    }
}