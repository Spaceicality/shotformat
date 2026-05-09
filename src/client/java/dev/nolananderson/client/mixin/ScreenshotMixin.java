package dev.nolananderson.client.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import dev.nolananderson.client.ShotFormatConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.ClickEvent.OpenFile;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

@Mixin(Screenshot.class)
public class ScreenshotMixin {

	@Inject(method = "grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
	private static void onGrab(File workDir, @Nullable String forceName, RenderTarget target, int downscaleFactor, Consumer<Component> callback, CallbackInfo ci) {
		ShotFormatConfig.Format format = ShotFormatConfig.get().format;

		if (format == ShotFormatConfig.Format.PNG) return; // let vanilla handle PNG

		ci.cancel();

		Screenshot.takeScreenshot(target, downscaleFactor, image -> {
			File picDir = new File(workDir, "screenshots");
			picDir.mkdir();

			File file;
			if (forceName == null) {
				file = new File(picDir, Util.getFilenameFormattedDateTime() + "." + format.getExtension());
			} else {
				file = new File(picDir, forceName + "." + format.getExtension());
			}

			Util.ioPool().execute(() -> {
				try {
					int width = image.getWidth();
					int height = image.getHeight();

					// getPixels() returns ARGB int array
					int[] pixels = image.getPixels();

					BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
					buffered.setRGB(0, 0, width, height, pixels, 0, width);

					if (format == ShotFormatConfig.Format.JPEG || format == ShotFormatConfig.Format.JPG) {
						ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
						JPEGImageWriteParam params = new JPEGImageWriteParam(null);
						params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
						params.setCompressionQuality(0.85f);
						try (ImageOutputStream ios = ImageIO.createImageOutputStream(file)) {
							writer.setOutput(ios);
							writer.write(null, new javax.imageio.IIOImage(buffered, null, null), params);
						}
					} else {
						// BMP
						ImageIO.write(buffered, format.name().toLowerCase(), file);
					}

					Component component = Component.literal(file.getName())
							.withStyle(ChatFormatting.UNDERLINE)
							.withStyle(s -> s.withClickEvent(new OpenFile(file.getAbsoluteFile())));
					callback.accept(Component.translatable("screenshot.success", component));
				} catch (Exception e) {
					callback.accept(Component.translatable("screenshot.failure", e.getMessage()));
				} finally {
					image.close();
				}
			});
		});
	}
}