package com.nextround.nextroundapi.repository;

import java.time.LocalDate;

public interface JobApplicationActivityCount {
    LocalDate getPeriod();
    long getCount();
}