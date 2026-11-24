package com.nextround.nextroundapi.repository;

import com.nextround.nextroundapi.enums.ApplicationStatus;

public interface JobApplicationStatusCount{
    ApplicationStatus getStatus();
    long getCount();
}
