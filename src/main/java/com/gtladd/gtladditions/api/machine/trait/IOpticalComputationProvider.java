package com.gtladd.gtladditions.api.machine.trait;

public interface IOpticalComputationProvider {

    long requestCWU(long cwu, boolean simulate);

    long remainCWU();

    long getMaxCWU();

    boolean canBridge();
}
