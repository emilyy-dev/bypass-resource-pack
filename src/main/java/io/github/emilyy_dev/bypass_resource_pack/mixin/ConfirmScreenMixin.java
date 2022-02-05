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
import net.minecraft.client.gui.components.Button;
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

  private @Unique Runnable bypassAction = null;

  protected ConfirmScreenMixin(final Component title) {
    super(title);
  }

  @Shadow protected abstract void addExitButton(final Button button);

  @Inject(
      method = "addButtons(I)V",
      at = @At("TAIL")
  )
  protected void addBypassButton(final int y, final CallbackInfo ci) {
    if (this.bypassAction != null) {
      addExitButton(new Button(this.width / 2 - 75, y + 20 + 5, 150, 20, BYPASS_TEXT, button -> this.bypassAction.run()));
    }
  }

  @Override
  public void bypassResourcePack$setBypassAction(final Runnable bypassAction) {
    this.bypassAction = bypassAction;
  }
}
