package com.gtladd.gtladditions.mixin.mc;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Set;

@Mixin(Holder.Reference.class)
public abstract class ReferenceMixin<T> implements Holder<T> {

    @Shadow
    private Set<TagKey<T>> tags;

    /**
     * @author .
     * @reason .
     */
    @Overwrite
    public void bindTags(Collection<TagKey<T>> tags) {
        this.tags = new ReferenceOpenHashSet<>(tags);
    }
}
