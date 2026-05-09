package dev.nolananderson.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			ConfigBuilder builder = ConfigBuilder.create()
					.setParentScreen(parent)
					.setTitle(Component.literal("Screenshot Format Settings"));

			ConfigEntryBuilder entryBuilder = builder.entryBuilder();
			ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

			general.addEntry(entryBuilder
					.startEnumSelector(
							Component.literal("Screenshot Format"),
							ShotFormatConfig.Format.class,
							ShotFormatConfig.get().format
					)
					.setDefaultValue(ShotFormatConfig.Format.PNG)
					.setSaveConsumer(val -> ShotFormatConfig.get().format = val)
					.build()
			);

			builder.setSavingRunnable(ShotFormatConfig::save);
			return builder.build();
		};
	}
}