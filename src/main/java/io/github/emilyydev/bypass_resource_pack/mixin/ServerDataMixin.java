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
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerData.class)
public abstract class ServerDataMixin {

    @Shadow private ServerData.ServerPackStatus packStatus;

    @Inject(
            method = "write",
            at = @At("TAIL")
    )
    protected void addToWrite(final CallbackInfoReturnable<CompoundTag> cir) {
        if (!"BYPASS".equals(this.packStatus.name())) return;
        cir.getReturnValue().putBoolean("bypassTextures", true);
    }

    @Inject(
            method = "read",
            at = @At("TAIL")
    )
    private static void addToRead(final CompoundTag compoundTag, final CallbackInfoReturnable<ServerData> cir) {
        if (!compoundTag.contains("bypassTextures")) return;
        if (!compoundTag.getBoolean("bypassTextures")) return;

        cir.getReturnValue().setResourcePackStatus(ServerData.ServerPackStatus.valueOf("BYPASS"));
    }
}
