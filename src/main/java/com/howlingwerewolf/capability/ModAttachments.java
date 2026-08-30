/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.capability;

import com.howlingwerewolf.HowlingWerewolf;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** NeoForge data attachments used by the mod. */
public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HowlingWerewolf.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WerewolfData>> WEREWOLF =
            ATTACHMENT_TYPES.register("werewolf_data", () -> AttachmentType.serializable(WerewolfData::new)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {}
}
