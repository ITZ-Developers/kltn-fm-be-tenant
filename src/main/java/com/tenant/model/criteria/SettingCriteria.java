package com.tenant.model.criteria;

import com.tenant.constant.FinanceConstant;
import com.tenant.model.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class SettingCriteria {
    private Long id;
    private String groupName;
    private String keyName;
    private Boolean isSystem;
    private Integer status;
    private Integer isPaged = FinanceConstant.IS_PAGED_TRUE; // 0: false, 1: true
    private Integer sortDate;

    public Specification<Setting> getCriteria() {
        return new Specification<Setting>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Setting> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if (StringUtils.isNoneBlank(getGroupName())) {
                    predicates.add(cb.like(cb.lower(root.get("groupName")), "%" + getGroupName().toLowerCase() + "%"));
                }
                if (StringUtils.isNoneBlank(getKeyName())) {
                    predicates.add(cb.like(cb.lower(root.get("keyName")), "%" + getKeyName().toLowerCase() + "%"));
                }
                if (getIsSystem() != null) {
                    predicates.add(cb.equal(root.get("isSystem"), getIsSystem()));
                }
                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                if(getSortDate() != null){
                    if(getSortDate().equals(FinanceConstant.SORT_DATE_ASC)){
                        query.orderBy(cb.asc(root.get("createdDate")));
                    }else{
                        query.orderBy(cb.desc(root.get("createdDate")));
                    }
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
