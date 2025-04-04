package com.tenant.storage.tenant.model;

import com.tenant.storage.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "db_fn_chatroom")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ChatRoom extends Auditable<String> {
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "com.tenant.service.id.IdGenerator")
    @GeneratedValue(generator = "idGenerator")
    private Long id;
    private String name;
    private String avatar;
    private Integer kind; // 1: group, 2: direct message
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_Id")
    private Account owner;
}
