package com.hantek.ttia.module.reportutils;

import com.hantek.ttia.protocol.a1a4.RegularReport;

public interface IReport {
    void updateReport();
    void report(RegularReport report);
}
