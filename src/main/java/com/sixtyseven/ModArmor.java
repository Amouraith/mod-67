package com.sixtyseven;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ModArmor {
    public static final RegistryEntry<ArmorMaterial> MATERIAL = create();

    private static RegistryEntry<ArmorMaterial> create() {
        Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 3);
        defense.put(ArmorItem.Type.LEGGINGS, 6);
        defense.put(ArmorItem.Type.CHESTPLATE, 8);
        defense.put(ArmorItem.Type.HELMET, 3);
        defense.put(ArmorItem.Type.BODY, 11);

        // Usa la textura de armadura dorada de Minecraft (queda amarilla)
        ArmorMaterial material = new ArmorMaterial(
                defense,
                25,
                SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
                () -> Ingredient.ofItems(ModItems.FRAGMENT_67),
                List.of(new ArmorMaterial.Layer(Identifier.ofVanilla("gold"))),
                2.0f,
                0.1f);
        return Registry.registerReference(Registries.ARMOR_MATERIAL,
                Identifier.of(SixtySevenMod.MOD_ID, "armor_67"), material);
    }
}
