package com.elibrary.model.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import java.util.Date;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public class BaseEntity<T> {

    @CreatedBy
    protected T createdBy;

    @CreatedDate
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    protected Date createdAt;

    @LastModifiedBy
    protected T updatedBy;

    @LastModifiedDate
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    protected Date updatedAt;
}
