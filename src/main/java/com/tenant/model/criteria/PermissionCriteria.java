package com.tenant.model.criteria;

import com.tenant.model.Permission;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class PermissionCriteria {
    private Long id;
    private String name;
    private String nameGroup;
    private Integer status;

    public Specification<Permission> getCriteria() {
        return new Specification<Permission>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Permission> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if(getId() != null){
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if(StringUtils.isNoneBlank(getName())){
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().toLowerCase() + "%"));
                }
                if(StringUtils.isNoneBlank(getNameGroup())){
                    predicates.add(cb.like(cb.lower(root.get("nameGroup")), "%" + getNameGroup().toLowerCase() + "%"));
                }
                if(getStatus() != null){
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
