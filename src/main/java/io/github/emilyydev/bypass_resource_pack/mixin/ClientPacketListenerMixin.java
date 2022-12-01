//
// Bypass Resource Pack - Fabric mod to reject and bypass forced resource packs
// Copyright (C) 2022  emilyy-dev
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

import io.github.emilyydev.bypass_resource_pack.BypassableConfirmScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

  @Unique private static final long SPOOFED_ACCEPT_DELAY_SECONDS =
      Long.parseLong(
          System.getProperty(
              "bypassResourcePack.spoofedAcceptDelaySeconds",
              System.getenv().getOrDefault("BYPASS_RESOURCE_PACK_SPOOFED_ACCEPT_DELAY_SECONDS", "5")
          )
      );

  // Some cheeky sneaky servers will check the time between Action.ACCEPTED -> Action.SUCCESSFULLY_LOADED
  //  to ensure the client "actually loaded" the resource pack
  @Unique private static final Executor DELAYED_EXECUTOR =
      SPOOFED_ACCEPT_DELAY_SECONDS > 0L ?
              CompletableFuture.delayedExecutor(SPOOFED_ACCEPT_DELAY_SECONDS, TimeUnit.SECONDS) :
              Runnable::run;

  @Shadow
  @Final
  private Minecraft minecraft;

  @Shadow protected abstract void downloadCallback(CompletableFuture<?> downloadFuture);

  @Shadow protected abstract void send(ServerboundResourcePackPacket.Action packStatus);

  @ModifyArg(
      // lambda in 'this.minecraft.execute(() -> ', synthetic method
      method = "method_34013",
      at = @At(
              value = "INVOKE",
              target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"
      ), index = 0
  )
  private Screen setScreenBypassAction(final Screen screen) {
    ((BypassableConfirmScreen) screen).bypassResourcePack$setBypassAction(() -> {
      this.minecraft.setScreen(null);
      bypassPack();
    });
    return screen;
  }

  @Inject(
      method = "handleResourcePack",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/Minecraft;getCurrentServer()Lnet/minecraft/client/multiplayer/ServerData;",
          shift = At.Shift.AFTER
      ),
      cancellable = true
  )
  private void setBypassStatusAction(final ClientboundResourcePackPacket clientboundResourcePackPacket, final CallbackInfo ci) {
      final ServerData serverData = minecraft.getCurrentServer();
      if (serverData != null && serverData.getResourcePackStatus().name().equals("BYPASS")) {
        bypassPack();
        ci.cancel();
      }
  }

  private void bypassPack() {
    send(ServerboundResourcePackPacket.Action.ACCEPTED);
    downloadCallback(CompletableFuture.runAsync(() -> {}, DELAYED_EXECUTOR));
  }
}
