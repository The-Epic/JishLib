package me.jishuna.jishlib;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.YamlConfiguration;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.TextReplacementConfig;

public class ComponentLocalization {
	private static final Pattern PATTERN = Pattern.compile("%[^%]*%");
	private static final ComponentLocalization instance = new ComponentLocalization();

	private final Map<String, String> localizationMap = new HashMap<>();
	private final Map<Component, Component> componentMap = new HashMap<>();
	private YamlConfiguration config;

	public String localize(String key) {
		return this.localizationMap.computeIfAbsent(key,
				mapKey -> StringUtils.parseToLegacy(config.getString(mapKey, "Missing key: " + mapKey)));
	}

	public String localize(String key, Object... format) {
		return MessageFormat.format(localize(key), format);
	}

	public Component localizeComponent(Component component) {
		return componentMap.computeIfAbsent(component, key -> component.replaceText(
				TextReplacementConfig.builder().match(PATTERN).replacement(this::replacePlaceholders).build()));
	}

	private Component replacePlaceholders(MatchResult result, Builder builder) {
		String key = result.group().replace("%", "");
		return StringUtils.parseComponent(config.getString(key, "Missing key: " + key));
	}

	public void setConfig(YamlConfiguration config) {
		this.config = config;
		this.localizationMap.clear();
		this.componentMap.clear();
	}

	public static ComponentLocalization getInstance() {
		return instance;
	}

}