package com.eyyupsehmec.ortakkuran.model;

import java.time.LocalDateTime;
import java.util.List;

public record MonitorResult(
        int pageCount,
        List<String> pages,
        LocalDateTime checkedAt
) {
}