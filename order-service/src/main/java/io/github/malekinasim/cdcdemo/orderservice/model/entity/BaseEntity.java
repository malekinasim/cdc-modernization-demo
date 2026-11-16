package io.github.malekinasim.cdcdemo.orderservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Columns every table in this legacy-style schema carries.
 *
 * <p>{@code LAST_UPD_BY} is not decoration: the sync process writes {@code SYNC} there, and the
 * middleware drops CDC events with that value so a change is never routed back to the system it
 * came from. See docs/domain-model.md section 5.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @LastModifiedBy
    @Column(name = "LAST_UPD_BY", nullable = false, length = 30)
    private String lastUpdatedBy;

    @LastModifiedDate
    @Column(name = "LAST_UPD_TS", nullable = false)
    private Instant lastUpdatedAt;
}
