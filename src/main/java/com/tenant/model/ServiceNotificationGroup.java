package com.tenant.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "db_fn_service_notification_group")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ServiceNotificationGroup extends Auditable<String> {
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "com.tenant.service.id.IdGenerator")
    @GeneratedValue(generator = "idGenerator")
    private Long id;
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "notification_group_id")
    private NotificationGroup notificationGroup;
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "service_id")
    private Service service;
}
