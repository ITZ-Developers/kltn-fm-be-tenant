package com.tenant.storage.tenant.model.criteria;

import com.tenant.storage.tenant.model.Account;
import com.tenant.storage.tenant.model.ChatRoom;
import com.tenant.storage.tenant.model.ChatRoomMember;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
@Data
public class ChatRoomMemberCriteria {
    private Long id;
    private String nickName;
    private Long roomId;
    private Long memberId;
    private String lastReadMessage;
    private Integer status;

    public Specification<ChatRoomMember> getCriteria() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (getId() != null) {
                predicates.add(cb.equal(root.get("id"), getId()));
            }
            if (StringUtils.isNotBlank(getNickName())) {
                predicates.add(cb.like(cb.lower(root.get("nickName")), "%" + getNickName().toLowerCase() + "%"));
            }
            if (getRoomId() != null) {
                Join<ChatRoomMember, ChatRoom> join = root.join("room", JoinType.INNER);
                predicates.add(cb.equal(join.get("id"), getRoomId()));
            }
            if (getMemberId() != null) {
                Join<ChatRoomMember, Account> join = root.join("member", JoinType.INNER);
                predicates.add(cb.equal(join.get("id"), getMemberId()));
            }
            if (StringUtils.isNotBlank(getLastReadMessage())) {
                predicates.add(cb.like(cb.lower(root.get("lastReadMessage")), "%" + getLastReadMessage().toLowerCase() + "%"));
            }
            if (getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), getStatus()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}