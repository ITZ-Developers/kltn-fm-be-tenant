package com.tenant.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "db_fn_key_information_group")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class KeyInformationGroup extends Auditable<String>  {
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "com.tenant.service.id.IdGenerator")
    @GeneratedValue(generator = "idGenerator")
    private Long id;
    private String name;
    @Column(columnDefinition = "text")
    private String description;
}
