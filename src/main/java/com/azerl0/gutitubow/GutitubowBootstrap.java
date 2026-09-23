package com.azerl0.gutitubow;

import com.azerl0.gutitubow.command.*;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@NullMarked
public class GutitubowBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY.newHandler(
                event -> {
                    // All code is contained here.
                    try {
                        URI uri = this.getClass().getResource("/GutitubowDatapack").toURI();
                        event.registrar().discoverPack(uri, "provided");
                    } catch (URISyntaxException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ));

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            AbilityCommand.register(event.registrar());
            CarePackageCommand.register(event.registrar());
            GameCommand.register(event.registrar());
            GamePlayerCommand.register(event.registrar());
            GameEventCommand.register(event.registrar());
            DebugCommand.register(event.registrar());
            ItemCommand.register(event.registrar());
            GameTeamCommand.register(event.registrar());
            SkinCommand.register(event.registrar());
            NameCommand.register(event.registrar());
            FakePlayerCommand.register(event.registrar());
            MapCommand.register(event.registrar());
            GutitubowCommand.register(event.registrar());
        }));
    }
}
