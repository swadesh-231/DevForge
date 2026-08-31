package com.devforge.entity;

import com.devforge.entity.enums.MessageRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "chat_messages",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_messages_session_sequence",
                        columnNames = {"chat_session_id", "sequence_order"}
                )
        }
)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_messages_seq")
    @SequenceGenerator(name = "chat_messages_seq", sequenceName = "chat_messages_seq", allocationSize = 50)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_session_id", nullable = false, updatable = false)
    private ChatSession chatSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ToString.Include
    private MessageRole role;

    @Column(nullable = false)
    private Integer sequenceOrder;

    @OneToMany(
            mappedBy = "chatMessage",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("sequenceOrder ASC")
    @Builder.Default
    private List<ChatEvent> events = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(nullable = false)
    private Integer tokensIn = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer tokensOut = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
