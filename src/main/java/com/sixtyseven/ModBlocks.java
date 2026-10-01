package com.sixtyseven;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModBlocks {
    public static final Block BLOCK_67 = new Block67(AbstractBlock.Settings.create()
            .mapColor(MapColor.PURPLE)
            .strength(6.7f)
            .requiresTool()
            .luminance(state -> 7)
            .sounds(BlockSoundGroup.AMETHYST_BLOCK));

    public static void register() {
        Identifier id = Identifier.of(SixtySevenMod.MOD_ID, "block_67");
        Registry.register(Registries.BLOCK, id, BLOCK_67);
        Registry.register(Registries.ITEM, id, new BlockItem(BLOCK_67, new Item.Settings().rarity(Rarity.EPIC)));
    }
}
