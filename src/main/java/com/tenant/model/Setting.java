package com.tenant.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "db_fn_setting")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Setting extends Auditable<String>{
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "com.tenant.service.id.IdGenerator")
    @GeneratedValue(generator = "idGenerator")
    private Long id;
    private String groupName;
    @Column(unique = true)
    private String keyName;
    @Column(columnDefinition = "text")
    private String valueData;
    @Column(name = "data_type")
    private String dataType; // int, string, boolean, double, richtext
    private Boolean isSystem;
}
