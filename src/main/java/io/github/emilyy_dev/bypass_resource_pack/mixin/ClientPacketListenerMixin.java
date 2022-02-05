//
// Bypass Resource Pack - Fabric mod to reject and bypass forced resource packs
// Copyright (C) 2021  emilyy-dev
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
//

package io.github.emilyy_dev.bypass_resource_pack.mixin;

import io.github.emilyy_dev.bypass_resource_pack.BypassableConfirmScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

  // some cheeky sneaky servers will check the time between Action.ACCEPTED -> Action.SUCCESSFULLY_LOADED
  // to ensure the client "actually loaded" the resource pack
  private static @Unique final Executor DELAYED_EXECUTOR = CompletableFuture.delayedExecutor(5L, TimeUnit.SECONDS);

  @Shadow private @Final Minecraft minecraft;

  @Shadow protected abstract void downloadCallback(CompletableFuture<?> downloadFuture);
  @Shadow protected abstract void send(ServerboundResourcePackPacket.Action packStatus);

  @ModifyArg(
      // lambda in 'this.minecraft.execute(() -> ', synthetic method
      method = "lambda$handleResourcePack$10(Ljava/lang/String;Ljava/lang/String;ZLnet/minecraft/network/protocol/game/ClientboundResourcePackPacket;)V",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"
      ), index = 0
  )
  private Screen setScreenBypassAction(final Screen screen) {
    ((BypassableConfirmScreen) screen).bypassResourcePack$setBypassAction(() -> {
      this.minecraft.setScreen(null);
      send(ServerboundResourcePackPacket.Action.ACCEPTED);
      downloadCallback(CompletableFuture.runAsync(() -> { }, DELAYED_EXECUTOR));
    });

    return screen;
  }
}
