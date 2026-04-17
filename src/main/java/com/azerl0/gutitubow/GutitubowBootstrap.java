package com.azerl0.gutitubow;

import com.azerl0.gutitubow.command.AbilityCommand;
import com.azerl0.gutitubow.command.CarePackageCommand;
import com.azerl0.gutitubow.command.DebugCommand;
import com.azerl0.gutitubow.command.FakePlayerCommand;
import com.azerl0.gutitubow.command.GameCommand;
import com.azerl0.gutitubow.command.GamePlayerCommand;
import com.azerl0.gutitubow.command.GameTeamCommand;
import com.azerl0.gutitubow.command.ItemCommand;
import com.azerl0.gutitubow.command.SetNameCommand;
import com.azerl0.gutitubow.command.SetSkinCommand;
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
            DebugCommand.register(event.registrar());
            ItemCommand.register(event.registrar());
            GameTeamCommand.register(event.registrar());
            SetSkinCommand.register(event.registrar());
            SetNameCommand.register(event.registrar());
            FakePlayerCommand.register(event.registrar());
        }));
    }
}
