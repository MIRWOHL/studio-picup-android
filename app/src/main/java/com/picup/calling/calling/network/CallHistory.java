package com.picup.calling.network;

import com.google.gson.annotations.SerializedName;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by frank.truong on 3/29/2017.
 */

public class CallHistory {
    @SerializedName("actualCDRsReturned")
    private int actualCDRCount = 0;
    @SerializedName("totalMatchingCDRs")
    private int matchingCDRCount = 0;
    private SortedSet<Cdr> cdrs = new TreeSet<>(new Comparator<Cdr>() {
            @Override
            public int compare(Cdr crd1, Cdr crd2) {
                if (crd1.getCallCalendar().after(crd2.getCallCalendar())) {
                    return -1;
                } else if (crd1.getCallCalendar().before(crd2.getCallCalendar())) {
                    return 1;
                }
                return 0;
            }
        });

    public int getActualCDRCount() {
        return actualCDRCount;
    }

    public void setActualCDRCount(int actualCDRCount) {
        this.actualCDRCount = actualCDRCount;
    }

    public int getMatchingCDRCount() {
        return matchingCDRCount;
    }

    public void setMatchingCDRCount(int matchingCDRCount) {
        this.matchingCDRCount = matchingCDRCount;
    }

    public SortedSet<Cdr> getCdrs() {
        return cdrs;
    }

    public void setCdrs(SortedSet<Cdr> cdrs) {
        this.cdrs = cdrs;
    }
}
