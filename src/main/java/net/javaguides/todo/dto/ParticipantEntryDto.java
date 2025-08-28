package net.javaguides.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantEntryDto {
    private String username;   // 顯示用名稱（沿用你目前習慣）
    private long count;        // 完成細項數
    private double percentage; // 0~100（四捨五入兩位）
}

