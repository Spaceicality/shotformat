package dev.nolananderson.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public class ShotFormatConfig {

	public enum Format {
		PNG, JPEG, JPG, BMP;

		public String getExtension() {
			return switch (this) {
				case PNG -> "png";
				case JPEG, JPG -> "jpg";
				case BMP -> "bmp";
			};
		}
	}

	public Format format = Format.PNG;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("shotformat.json");
	private static ShotFormatConfig instance = load();

	public static ShotFormatConfig get() { return instance; }

	public static ShotFormatConfig load() {
		if (Files.exists(CONFIG_PATH)) {
			try {
				String json = Files.readString(CONFIG_PATH);
				return GSON.fromJson(json, ShotFormatConfig.class);
			} catch (IOException e) {
				System.err.println("[shotformat] Failed to load config: " + e.getMessage());
			}
		}
		return new ShotFormatConfig();
	}

	public static void save() {
		try {
			Files.writeString(CONFIG_PATH, GSON.toJson(instance));
		} catch (IOException e) {
			System.err.println("[shotformat] Failed to save config: " + e.getMessage());
		}
	}
}