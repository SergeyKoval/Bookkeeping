package by.bk.entity.user.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {
    private String title;
    private String color;
    @Builder.Default
    private String textColor = "#FFFFFF";
    @Builder.Default
    private boolean active = true;
}
