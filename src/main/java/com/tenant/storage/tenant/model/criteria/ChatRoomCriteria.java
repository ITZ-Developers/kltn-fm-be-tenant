package com.tenant.storage.tenant.model.criteria;

import com.tenant.storage.tenant.model.Account;
import com.tenant.storage.tenant.model.ChatRoom;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ChatRoomCriteria {
    private Long id;
    private String name;
    private String avatar;
    private Integer kind;
    private Long ownerId;
    private Integer status;

    public Specification<ChatRoom> getCriteria() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (getId() != null) {
                predicates.add(cb.equal(root.get("id"), getId()));
            }
            if (StringUtils.isNotBlank(getName())) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(getAvatar())) {
                predicates.add(cb.like(cb.lower(root.get("avatar")), "%" + getAvatar().toLowerCase() + "%"));
            }
            if (getKind() != null) {
                predicates.add(cb.equal(root.get("kind"), getKind()));
            }
            if (getOwnerId() != null) {
                Join<ChatRoom, Account> join = root.join("owner", JoinType.INNER);
                predicates.add(cb.equal(join.get("id"), getOwnerId()));
            }
            if (getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), getStatus()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}