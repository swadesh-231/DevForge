package com.devforge.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class ProjectMemberId implements Serializable {
    private Long projectId;
    private Long userId;
}
