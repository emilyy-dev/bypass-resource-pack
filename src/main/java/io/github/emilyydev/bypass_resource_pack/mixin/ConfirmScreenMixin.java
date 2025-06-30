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

import io.github.emilyydev.bypass_resource_pack.BypassableConfirmScreen;
import io.github.emilyydev.bypass_resource_pack.ModConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfirmScreen.class)
public abstract class ConfirmScreenMixin extends Screen implements BypassableConfirmScreen {

  @Shadow protected LinearLayout layout;
  @Unique private Runnable bypassAction = null;

  protected ConfirmScreenMixin(final Component title) {
    super(title);
  }

  @Inject(
      method = "init",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/gui/screens/ConfirmScreen;addButtons(Lnet/minecraft/client/gui/layouts/LinearLayout;)V",
          shift = At.Shift.AFTER
      )
  )
  protected void addBypassButton(final CallbackInfo ci) {
    if (this.bypassAction != null) {
      this.layout.addChild(Button.builder(ModConstants.BYPASS_TEXT, $ -> this.bypassAction.run()).build());
    }
  }

  @Override
  public void bypassResourcePack$setBypassAction(final Runnable bypassAction) {
    if (this.bypassAction == null) {
      this.bypassAction = bypassAction;
    }
  }
}
