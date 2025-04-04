package com.tenant.storage.tenant.model;

import com.tenant.storage.Auditable;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "db_fn_chatroom_member")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ChatRoomMember extends Auditable<String> {
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "com.tenant.service.id.IdGenerator")
    @GeneratedValue(generator = "idGenerator")
    private Long id;
    private String nickName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_Id")
    private Account member;
    @Column(columnDefinition = "TEXT")
    private String lastReadMessage;
}
