package com.api.payment.job;

import com.api.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentJob {

    private final OrderService orderService;

    public PaymentJob(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void processOrdersWithStatusPending() {
        log.info("[ORDER JOB] - Iniciando processamento de pedidos pendentes...");
        int processedCount = orderService.processOrdersWithStatusPending();
        if (processedCount == 0) {
            log.info("[ORDER JOB] - Processamento finalizado. Não há pedidos pendentes disponíveis.");
        } else {
            log.info("[ORDER JOB] - Processamento de pedidos pendentes finalizado. Pedidos processados: {}", processedCount);
        }
    }

}
