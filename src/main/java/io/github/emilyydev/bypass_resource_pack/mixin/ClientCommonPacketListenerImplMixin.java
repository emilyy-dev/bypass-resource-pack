//
// Bypass Resource Pack - Fabric mod to reject and bypass forced resource packs
// Copyright (C) 2023  emilyy-dev
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
//

package io.github.emilyydev.bypass_resource_pack.mixin;

import com.google.common.collect.Iterables;
import io.github.emilyydev.bypass_resource_pack.BypassableConfirmScreen;
import io.github.emilyydev.bypass_resource_pack.ModConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {

  // TODO: make proper config
  @Unique private static final long SPOOFED_ACCEPT_DELAY_SECONDS =
      Long.parseLong(
          System.getProperty(
              "bypassResourcePack.spoofedAcceptDelaySeconds",
              System.getenv().getOrDefault("BYPASS_RESOURCE_PACK_SPOOFED_ACCEPT_DELAY_SECONDS", "3")
          )
      );

  // Some cheeky sneaky servers will check the time between Action.ACCEPTED -> Action.SUCCESSFULLY_LOADED
  //  to ensure the client "actually loaded" the resource pack
  @Unique private static final Executor DELAYED_EXECUTOR =
      SPOOFED_ACCEPT_DELAY_SECONDS > 0L ?
          CompletableFuture.delayedExecutor(SPOOFED_ACCEPT_DELAY_SECONDS, TimeUnit.SECONDS) :
          Runnable::run;

  @Shadow @Final protected Minecraft minecraft;
  @Shadow @Final protected ServerData serverData;
  @Shadow @Final protected Connection connection;

  @ModifyArg(
      method = "handleResourcePackPush",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"
      ),
      index = 0
  )
  private Screen setScreenBypassAction(final Screen screen) {
    final UUID packId = Iterables.getLast(((PackConfirmScreenMixin) screen).getRequests()).callId();
    ((BypassableConfirmScreen) screen).bypassResourcePack$setBypassAction(() -> {
      this.minecraft.setScreen(null);
      if (this.serverData != null) {
        this.serverData.setResourcePackStatus(ModConstants.getBypassStatus());
        this.minecraft.getDownloadedPackSource().allowServerPacks();
        ServerList.saveSingleServer(this.serverData);
      }

      bypassPack(packId);
    });

    return screen;
  }

  @Inject(
      method = "handleResourcePackPush",
      at = @At(
          value = "FIELD",
          opcode = Opcodes.GETFIELD,
          target = "Lnet/minecraft/client/multiplayer/ClientCommonPacketListenerImpl;serverData:Lnet/minecraft/client/multiplayer/ServerData;",
          ordinal = 0
      ),
      cancellable = true
  )
  private void setBypassStatusAction(final ClientboundResourcePackPushPacket packet, final CallbackInfo ci) {
    if (this.serverData != null && this.serverData.getResourcePackStatus() == ModConstants.getBypassStatus()) {
      bypassPack(packet.id());
      ci.cancel();
    }
  }

  @Unique
  private void bypassPack(final UUID id) {
    this.connection.send(new ServerboundResourcePackPacket(id, ServerboundResourcePackPacket.Action.ACCEPTED));
    DELAYED_EXECUTOR.execute(() -> this.connection.send(new ServerboundResourcePackPacket(id, ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED)));
  }
}
