package com.gtladd.gtladditions.api.machine.trait;

public interface IOpticalComputationProvider {

    long requestCWU(long cwu, boolean simulate);

    long getMaxCWU();

    boolean canBridge();
}
