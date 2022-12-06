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

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.emilyydev.bypass_resource_pack.BypassableConfirmScreen.BYPASS_TEXT;

@Mixin(ServerData.ServerPackStatus.class)
public abstract class ServerPackStatusMixin {

  @Shadow @Final @Mutable private static ServerData.ServerPackStatus[] $VALUES;

  // The new enum constant for the server pack status
  private static final ServerData.ServerPackStatus BYPASS = addVariant("BYPASS", "bypass");

  @Invoker("<init>")
  public static ServerData.ServerPackStatus serverPackStatus$invokeInit(final String internalName, final int internalId, String name) {
    throw new AssertionError();
  }

  // Very hacky way to set the name without translation key on the resources.
  @Inject(
      method = "getName",
      at = @At("HEAD"),
      cancellable = true
  )
  private void addToGetName(final CallbackInfoReturnable<Component> cir) {
    if (BYPASS == (Object) this) cir.setReturnValue(BYPASS_TEXT);
  }

  @Unique
  private static ServerData.ServerPackStatus addVariant(final String internalName, final String name) {
    final List<ServerData.ServerPackStatus> variants = Arrays.asList(ServerPackStatusMixin.$VALUES);
    final ServerData.ServerPackStatus status = serverPackStatus$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1, name);

    variants.add(status);
    ServerPackStatusMixin.$VALUES = variants.toArray(new ServerData.ServerPackStatus[0]);
    return status;
  }
}
