/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum CompanionMode implements StringRepresentable {
    FOLLOW, GUARD, REST, HUNT, DEAD, SORT;

    public static final Codec<CompanionMode> CODEC = StringRepresentable.fromEnum(CompanionMode::values);
    @Override public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }
    public String translationKey() { return "companion.howlingwerewolf.mode." + getSerializedName(); }
    public boolean hasEntity() { return this != HUNT && this != DEAD; }
}
