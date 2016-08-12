package com.hantek.ttia.view;

import com.hantek.ttia.module.roadutils.Road;

public interface BranchCallback {
    void branchSubmit(Road branch);
    void branchBack();
}
