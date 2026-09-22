package com.gtladd.gtladditions.api.recipe.ledger;

final class LedgerCacheBudget {

    int remaining;

    LedgerCacheBudget(int limit) {
        this.remaining = limit;
    }

    boolean tryAcquire() {
        if (remaining <= 0) return false;
        remaining--;
        return true;
    }

    void refund(int count) {
        if (count > 0) remaining += count;
    }
}
