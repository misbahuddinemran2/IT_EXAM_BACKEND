package com.examplatform.appconfig.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SlideReorderRequest {
    // Ordered list of slide IDs — backend will assign slideOrder 1,2,3...
    private List<String> slideIds;
}