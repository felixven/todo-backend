package net.javaguides.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 完成者榜：每位完成者的任務完成數（每任務只計第一完成者 1 次；Reopen 後重算） */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinisherRowDto {
    private String userName;  // 目前 Todo.completedBy 存的是顯示名稱（例如 firstName）
    private long finishCount;
}
