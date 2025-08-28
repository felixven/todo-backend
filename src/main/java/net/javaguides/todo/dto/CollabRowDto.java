package net.javaguides.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 協作榜：每位使用者的累計細項完成數（僅統計有細項且參與者≥2的任務） */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollabRowDto {
    private Long userId;
    private String userName;
    private long collabCount; // 完成過的細項數
}
