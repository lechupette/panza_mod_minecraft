package com.ruletita.mod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(RuletitaMod.MODID)
public final class RuletitaMod {
    public static final String MODID = "ruletita";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    private static final List<RegistryObject<Item>> LISTA = new ArrayList<>();

    private static Item.Properties props(String name) {
        return new Item.Properties().setId(ITEMS.key(name));
    }

    private static RegistryObject<Item> simple(String name, int stack) {
        RegistryObject<Item> r = ITEMS.register(name, () -> new Item(props(name).stacksTo(stack)));
        LISTA.add(r);
        return r;
    }

    private static RegistryObject<Item> food(String name, int nutrition, float saturation, float seconds, int stack) {
        RegistryObject<Item> r = ITEMS.register(name, () -> new Item(props(name).stacksTo(stack)
                .component(DataComponents.FOOD, new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build())
                .component(DataComponents.CONSUMABLE, Consumable.builder().consumeSeconds(seconds).build())));
        LISTA.add(r);
        return r;
    }

    // ---- Pollo con carne
    public static final RegistryObject<Item> POLLO_CARNE_CRUDO = food("pollo_carne_crudo", 6, 0.3f, 4.0f, 16);
    public static final RegistryObject<Item> POLLO_CARNE = food("pollo_carne", 10, 0.6f, 4.0f, 16);

    // ---- Cafe
    public static final RegistryObject<Block> COFFEE_PLANT = BLOCKS.register("coffee_plant", () -> new CoffeeCropBlock(
            BlockBehaviour.Properties.of().setId(BLOCKS.key("coffee_plant")).mapColor(MapColor.PLANT).noCollision()
                    .randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)));
    public static final RegistryObject<Item> CEREZAS_CAFE = ITEMS.register("cerezas_cafe",
            () -> new BlockItem(COFFEE_PLANT.get(), props("cerezas_cafe").useItemDescriptionPrefix()));
    public static final RegistryObject<Item> CAFE_TOSTADO = simple("cafe_tostado", 64);
    public static final RegistryObject<Item> CAFE_TOSTADO2 = simple("cafe_tostado2", 64);
    public static final RegistryObject<Item> CAFE_MOLIDO = simple("cafe_molido", 64);
    public static final RegistryObject<Item> TAZA = ITEMS.register("taza", () -> new TazaItem(props("taza").stacksTo(1)));
    public static final RegistryObject<Item> TAZA_AGUA = simple("taza_agua", 1);
    public static final RegistryObject<Item> TAZA_AGUA_CAFE = simple("taza_agua_cafe", 1);
    public static final RegistryObject<Item> TAZA_CAFE = ITEMS.register("taza_cafe", () -> new Item(props("taza_cafe").stacksTo(1)
            .component(DataComponents.FOOD, new FoodProperties.Builder().alwaysEdible().nutrition(0).saturationModifier(0f).build())
            .component(DataComponents.CONSUMABLE, Consumable.builder().consumeSeconds(1.6f)
                    .animation(ItemUseAnimation.DRINK).sound(SoundEvents.GENERIC_DRINK).hasConsumeParticles(false).build())));

    // ---- Frascos
    public static final RegistryObject<Item> FORMOL = simple("formol", 16);
    public static final RegistryObject<Item> FRASCO_VACIO = simple("frasco_vacio", 16);
    public static final RegistryObject<Item> FRASCO_FORMOL = simple("frasco_formol", 16);
    public static final RegistryObject<Item> FRASCO_PEZ_COD = simple("frasco_pez_cod", 16);
    public static final RegistryObject<Item> FRASCO_PEZ_SALMON = simple("frasco_pez_salmon", 16);
    public static final RegistryObject<Item> FRASCO_PEZ_TROPICAL = simple("frasco_pez_tropical", 16);
    public static final RegistryObject<Item> FRASCO_PEZ_GLOBO = simple("frasco_pez_globo", 16);

    // ---- Sonido
    public static final RegistryObject<SoundEvent> FART = SOUNDS.register("fart",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MODID, "fart")));

    // ---- Pestana creativa
    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("ruletita", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ruletita"))
            .icon(() -> TAZA_CAFE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(CEREZAS_CAFE.get());
                for (RegistryObject<Item> i : LISTA) output.accept(i.get());
                output.accept(TAZA.get());
                output.accept(TAZA_CAFE.get());
            }).build());

    public RuletitaMod(FMLJavaModLoadingContext context) {
        var bus = context.getModBusGroup();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        SOUNDS.register(bus);
        TABS.register(bus);

        TickEvent.PlayerTickEvent.Post.BUS.addListener(Eventos::onPlayerTick);
        LivingEntityUseItemEvent.Finish.BUS.addListener(Eventos::onUseFinish);
        PlayerSleepInBedEvent.BUS.addListener(Eventos::onSleep);
    }
}
