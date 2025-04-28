package io.github.skydynamic.mccr.event;

import io.github.skydynamic.mccr.networking.Networking;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class MccrEvents {
    @SubscribeEvent
    public static void onRegisterPayloadHandler(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        Networking.register(registrar);
    }
}
