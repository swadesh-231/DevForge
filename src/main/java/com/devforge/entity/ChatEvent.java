package com.devforge.entity;

import com.devforge.entity.enums.ChatEventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "chat_events",
        indexes = {
                @Index(name = "idx_chat_events_message_id", columnList = "chat_message_id")
        }
)
public class ChatEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_events_seq")
    @SequenceGenerator(name = "chat_events_seq", sequenceName = "chat_events_seq", allocationSize = 50)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_message_id", nullable = false, updatable = false)
    private ChatMessage chatMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ToString.Include
    private ChatEventType type;

    @Column(nullable = false)
    private Integer sequenceOrder;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 1024)
    private String filePath;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;
}
