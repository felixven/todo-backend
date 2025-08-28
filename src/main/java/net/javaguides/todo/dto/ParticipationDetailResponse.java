package net.javaguides.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationDetailResponse {
    private Long todoId;
    private boolean eligibleForCollabBoard; // 參與者數 >= 2
    private long totalCompletedItems;
    private boolean currentUserIsParticipant;
    private List<ParticipantEntryDto> participants;
}
