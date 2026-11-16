package io.github.malekinasim.cdcdemo.orderservice.repository;

import io.github.malekinasim.cdcdemo.orderservice.model.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * The order, locked for update until the transaction commits.
     *
     * <p>Every path that changes an order reads it through this method, so two requests for the
     * same order cannot both pass their status check and then overwrite each other: adding a line
     * to an order that is being placed at that moment waits instead.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberForUpdate(@Param("orderNumber") String orderNumber);

    /** The order with its customer, lines and the products of those lines, in one query. */
    @Query("""
            select distinct o from Order o
              join fetch o.customer
              left join fetch o.lines l
              left join fetch l.product
            where o.orderNumber = :orderNumber
            """)
    Optional<Order> findDetailByOrderNumber(@Param("orderNumber") String orderNumber);

    /** Next value of the order number sequence, so ORD_NO is readable and gap-tolerant. */
    @Query(value = "select nextval('ord_no_seq')", nativeQuery = true)
    long nextOrderNumberSequence();
}
