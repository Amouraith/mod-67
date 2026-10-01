package com.sixtyseven;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {
    public static final Item FRAGMENT_67 = new Item(new Item.Settings().rarity(Rarity.EPIC));

    // Danio base 62 + netherite 4 + 1 de la mano = 67 en el tooltip
    public static final Item SWORD_67 = new Sword67Item(new Item.Settings()
            .rarity(Rarity.EPIC)
            .fireproof()
            .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.NETHERITE, 62, -2.0f)));

    public static final Item PROYECTOR = new ProjectorItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof());

    public static final Item HELMET_67 = armor(ArmorItem.Type.HELMET);
    public static final Item CHESTPLATE_67 = armor(ArmorItem.Type.CHESTPLATE);
    public static final Item LEGGINGS_67 = armor(ArmorItem.Type.LEGGINGS);
    public static final Item BOOTS_67 = armor(ArmorItem.Type.BOOTS);

    private static Item armor(ArmorItem.Type type) {
        return new Armor67Item(ModArmor.MATERIAL, type, new Item.Settings()
                .rarity(Rarity.EPIC)
                .fireproof()
                .maxDamage(type.getMaxDamage(37)));
    }

    private static void reg(String name, Item item) {
        Registry.register(Registries.ITEM, Identifier.of(SixtySevenMod.MOD_ID, name), item);
    }

    public static void register() {
        reg("fragment_67", FRAGMENT_67);
        reg("sword_67", SWORD_67);
        reg("proyector", PROYECTOR);
        reg("helmet_67", HELMET_67);
        reg("chestplate_67", CHESTPLATE_67);
        reg("leggings_67", LEGGINGS_67);
        reg("boots_67", BOOTS_67);

        ItemGroup tab = FabricItemGroup.builder()
                .icon(() -> new ItemStack(SWORD_67))
                .displayName(Text.translatable("itemGroup.sixtyseven.main"))
                .entries((context, entries) -> {
                    entries.add(SWORD_67);
                    entries.add(PROYECTOR);
                    entries.add(HELMET_67);
                    entries.add(CHESTPLATE_67);
                    entries.add(LEGGINGS_67);
                    entries.add(BOOTS_67);
                    entries.add(FRAGMENT_67);
                    entries.add(ModBlocks.BLOCK_67);
                })
                .build();
        Registry.register(Registries.ITEM_GROUP, Identifier.of(SixtySevenMod.MOD_ID, "main"), tab);
    }
}
