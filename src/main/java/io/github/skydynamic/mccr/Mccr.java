package io.github.skydynamic.mccr;

import io.github.skydynamic.mccr.event.MccrEvents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Mccr.MODID)
public class Mccr {
    public static final String MODID = "mccr";

    public Mccr(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.register(MccrEvents.class);
    }

    public static ResourceLocation location(String location) {
        return ResourceLocation.fromNamespaceAndPath("mccr", location);
    }
}
