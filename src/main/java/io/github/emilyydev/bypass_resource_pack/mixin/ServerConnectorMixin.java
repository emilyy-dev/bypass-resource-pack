//
// Bypass Resource Pack - Fabric mod to reject and bypass forced resource packs
// Copyright (C) 2025  emilyy-dev
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

import io.github.emilyydev.bypass_resource_pack.ModConstants;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.server.ServerPackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.screens.ConnectScreen$1")
public class ServerConnectorMixin {

  @Inject(
      method = "convertPackStatus",
      at = @At("HEAD"),
      cancellable = true
  )
  private static void guardSwitchCase(
      final ServerData.ServerPackStatus status,
      final CallbackInfoReturnable<ServerPackManager.PackPromptStatus> cir
  ) {
    if (status == ModConstants.getBypassStatus()) {
      cir.setReturnValue(ServerPackManager.PackPromptStatus.ALLOWED);
    }
  }
}
