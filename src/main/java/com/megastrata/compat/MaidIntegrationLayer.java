package com.megastrata.compat;

import net.minecraftforge.fml.ModList;

public class MaidIntegrationLayer {
    private static boolean maidModLoaded = false;
    private static boolean alexsCavesLoaded = false;

    public static void init() {
        maidModLoaded = ModList.get().isLoaded("touhou_little_maid") || ModList.get().isLoaded("littlemaidrebirth");
        alexsCavesLoaded = ModList.get().isLoaded("alexscaves");
    }

    public static boolean isMaidModActive() {
        return maidModLoaded;
    }

    public static boolean isAlexsCavesActive() {
        return alexsCavesLoaded;
    }
}
