// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A {@link Toast} implementation which displays an arbitrary message along with an optional {@link ItemStack}.
 */
public class ItemToast implements Toast {
    private static final ResourceLocation TEXTURE = new ResourceLocation("toast/recipe");
    public static final Object TRANSFER_NO_RESPONSE_TOKEN = new Object();

    private static final long DISPLAY_TIME = 7000L;
    private static final int MAX_LINE_SIZE = 200;

    private static final int IMAGE_SIZE = 16;
    private static final int LINE_SPACING = 10;
    private static final int MARGIN = 8;

    private final ItemStack stack;
    private final Component title;
    private final List<FormattedCharSequence> message;
    private final Object token;
    private final int width;

    private boolean isNew = true;
    private long firstDisplay;

    public ItemToast(Minecraft minecraft, ItemStack stack, Component title, Component message, Object token) {
        this.stack = stack;
        this.title = title;
        this.token = token;

        var font = minecraft.font;
        this.message = font.split(message, MAX_LINE_SIZE);
        width = Math.max(MAX_LINE_SIZE, this.message.stream().mapToInt(font::width).max().orElse(MAX_LINE_SIZE)) + MARGIN * 3 + IMAGE_SIZE;
    }

    public void showOrReplace(ToastComponent toasts) {
        var existing = toasts.getToast(ItemToast.class, getToken());
        if (existing != null) {
            existing.isNew = true;
        } else {
            toasts.addToast(this);
        }
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return MARGIN * 2 + LINE_SPACING + message.size() * LINE_SPACING;
    }

    @Override
    public Object getToken() {
        return token;
    }

    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent component, long time) {
        if (isNew) {

            firstDisplay = time;
            isNew = false;
        }

        if (width == 160 && message.size() <= 1) {
            graphics.blitSprite(TEXTURE, 0, 0, width, height());
        } else {

            var height = height();

            var bottom = Math.min(4, height - 28);
            renderBackgroundRow(graphics, width, 0, 0, 28);

            for (var i = 28; i < height - bottom; i += 10) {
                renderBackgroundRow(graphics, width, 16, i, Math.min(16, height - i - bottom));
            }

            renderBackgroundRow(graphics, width, 32 - bottom, height - bottom, bottom);
        }

        var textX = MARGIN;
        if (!stack.isEmpty()) {
            textX += MARGIN + IMAGE_SIZE;
            graphics.renderFakeItem(stack, MARGIN, MARGIN + height() / 2 - IMAGE_SIZE);
        }

        graphics.drawString(component.getMinecraft().font, title, textX, MARGIN, 0xff500050, false);
        for (var i = 0; i < message.size(); ++i) {
            graphics.drawString(component.getMinecraft().font, message.get(i), textX, LINE_SPACING + (i + 1) * LINE_SPACING, 0xff000000, false);
        }

        return time - firstDisplay < DISPLAY_TIME ? Visibility.SHOW : Visibility.HIDE;
    }

    private void renderBackgroundRow(GuiGraphics graphics, int x, int u, int y, int height) {
        int leftOffset = u == 0 ? 20 : 5;
        int rightOffset = Math.min(60, x - leftOffset);

        graphics.blitSprite(TEXTURE, 160, 32, 0, u, 0, y, leftOffset, height);
        for (int k = leftOffset; k < x - rightOffset; k += 64) {
            graphics.blitSprite(TEXTURE, 160, 32, 32, u, k, y, Math.min(64, x - k - rightOffset), height);
        }

        graphics.blitSprite(TEXTURE, 160, 32, 160 - rightOffset, u, x - rightOffset, y, rightOffset, height);
    }
}
