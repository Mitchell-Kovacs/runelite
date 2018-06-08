/*
 * Copyright (c) 2017, Mitchell <https://github.com/Mitchell-Kovacs>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.thieving;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Provides;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;

import javax.inject.Inject;

@PluginDescriptor(
	name = "Thieving"
)
public class ThievingPlugin extends Plugin
{
	private static Pattern dodgyCheckPattern = Pattern.compile(
		"Your dodgy necklace has ([1-9]|10) charges? left\\.");
	private static Pattern dodgyProtectPattern = Pattern.compile(
		"Your dodgy necklace protects you\\..*It has ([1-9]) charges? left\\..*");
	private static Pattern dodgyBreakPattern = Pattern.compile(
		"Your dodgy necklace protects you\\..*It then crumbles to dust\\..*");

	private static int MAX_DODGY_CHARGES = 10;

	@Inject
	private ThievingOverlay overlay;

	@Inject
	private ThievingConfig config;

	@Inject
	private Notifier notifier;

	@Provides
	ThievingConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ThievingConfig.class);
	}

	@Override
	public Overlay getOverlay()
	{
		return overlay;
	}

	@Override
	protected void startUp()
	{
		overlay.setDodgyCharges(config.dodgyNecklace());
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{

		String eventMessage = event.getMessage();
		Matcher dodgyCheckMatcher = dodgyCheckPattern.matcher(eventMessage);
		Matcher dodgyProtectMatcher = dodgyProtectPattern.matcher(eventMessage);
		Matcher dodgyBreakMatcher = dodgyBreakPattern.matcher(eventMessage);
		if (event.getType() == ChatMessageType.SERVER)
		{
			if (dodgyBreakMatcher.find())
			{
				if (config.dodgyNotification())
				{
					notifier.notify("Your dodgy necklace has crumbled to dust.");
				}
				overlay.setDodgyCharges(MAX_DODGY_CHARGES);
			}
			else if (dodgyCheckMatcher.find())
			{
				overlay.setDodgyCharges(Integer.parseInt(dodgyCheckMatcher.group(1)));
			}
			else if (dodgyProtectMatcher.find())
			{
				overlay.setDodgyCharges(Integer.parseInt(dodgyProtectMatcher.group(1)));
			}
		}
		else if (event.getType() == ChatMessageType.FILTERED)
		{
			if (dodgyProtectMatcher.find())
			{
				overlay.setDodgyCharges(Integer.parseInt(dodgyProtectMatcher.group(1)));
			}
		}
	}
}
