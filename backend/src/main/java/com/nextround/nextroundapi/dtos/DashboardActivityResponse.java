package com.nextround.nextroundapi.dtos;

import java.util.List;

public record DashboardActivityResponse(String granularity, List<ActivityPoint> data) {
}
