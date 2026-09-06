package com.solarbiscuit.ratlib.economy;

public final class CoinServices {
    private static CoinService instance = CoinService.NONE;

    private CoinServices() {}

    public static void register(CoinService service) {
        instance = service == null ? CoinService.NONE : service;
    }

    public static CoinService get() {
        return instance;
    }

    public static boolean isLoaded() {
        return instance.isPresent();
    }
}
