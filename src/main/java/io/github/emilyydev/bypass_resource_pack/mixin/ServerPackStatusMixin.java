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

import io.github.emilyydev.bypass_resource_pack.ModConstants;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(ServerData.ServerPackStatus.class)
public class ServerPackStatusMixin {

  // The new enum constant for the server pack status
  private static ServerData.ServerPackStatus BYPASS;

  @Invoker("<init>")
  public static ServerData.ServerPackStatus serverPackStatus$invokeInit(
      final String enumName,
      final int enumOrdinal,
      final String name
  ) {
    throw new AssertionError();
  }

  @Inject(
      method = "$values",
      at = @At("TAIL"),
      cancellable = true
  )
  private static void addVariant(final CallbackInfoReturnable<ServerData.ServerPackStatus[]> cir) {
    ServerData.ServerPackStatus[] values = cir.getReturnValue();
    final int ordinal = values.length;
    cir.setReturnValue(values = Arrays.copyOfRange(values, 0, ordinal + 1));
    values[ordinal] = BYPASS = serverPackStatus$invokeInit(ModConstants.ENUM_NAME, ordinal, "bypass");
  }

  // Very hacky way to set the name without translation key on the resources.
  @Inject(
      method = "getName",
      at = @At("HEAD"),
      cancellable = true
  )
  private void addToGetName(final CallbackInfoReturnable<Component> cir) {
    if (BYPASS == (Object) this) {
      cir.setReturnValue(ModConstants.BYPASS_TEXT);
    }
  }
}
