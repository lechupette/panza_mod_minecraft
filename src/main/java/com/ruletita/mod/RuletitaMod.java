package com.ruletita.mod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
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
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
        RegistryObject<Item> item = ITEMS.register(name, () -> new Item(props(name).stacksTo(stack)));
        LISTA.add(item);
        return item;
    }

    private static RegistryObject<Item> jar(String name) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new SpecimenJarBlock(
                BlockBehaviour.Properties.of().setId(BLOCKS.key(name)).mapColor(MapColor.COLOR_LIGHT_GRAY)
                        .strength(0.3f).noOcclusion()));
        RegistryObject<Item> item = ITEMS.register(name,
                () -> new BlockItem(block.get(), props(name).useItemDescriptionPrefix()));
        LISTA.add(item);
        return item;
    }

    private static RegistryObject<Item> food(String name, int nutrition, float saturation, float seconds, int stack) {
        RegistryObject<Item> item = ITEMS.register(name, () -> new Item(props(name).stacksTo(stack)
                .component(DataComponents.FOOD, new FoodProperties.Builder().alwaysEdible().nutrition(nutrition).saturationModifier(saturation).build())
                .component(DataComponents.CONSUMABLE, Consumable.builder().consumeSeconds(seconds).build())));
        LISTA.add(item);
        return item;
    }

    // Alimentos del mod: siempre se pueden consumir, incluso con la barra llena.
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

    // ---- Frascos de colección: bloques decorativos colocables y recuperables.
    public static final RegistryObject<Item> GUIA_ESTUDIANTE = ITEMS.register("guia_estudiante",
            () -> new GuideBookItem(props("guia_estudiante").stacksTo(1)));
    static { LISTA.add(GUIA_ESTUDIANTE); }
    public static final RegistryObject<Item> FORMOL = simple("formol", 16);
    public static final RegistryObject<Item> FRASCO_VACIO = jar("frasco_vacio");
    public static final RegistryObject<Item> FRASCO_FORMOL = jar("frasco_formol");
    public static final RegistryObject<Item> FRASCO_PEZ_COD = jar("frasco_pez_cod");
    public static final RegistryObject<Item> FRASCO_PEZ_SALMON = jar("frasco_pez_salmon");
    public static final RegistryObject<Item> FRASCO_PEZ_TROPICAL = jar("frasco_pez_tropical");
    public static final RegistryObject<Item> FRASCO_PEZ_GLOBO = jar("frasco_pez_globo");

    public static final RegistryObject<SoundEvent> FART = SOUNDS.register("fart",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MODID, "fart")));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("ruletita", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ruletita"))
            .icon(() -> TAZA_CAFE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(CEREZAS_CAFE.get());
                for (RegistryObject<Item> item : LISTA) output.accept(item.get());
                output.accept(TAZA.get());
                output.accept(TAZA_CAFE.get());
                // Kuadros: cuadros con medida configurada (1x1 a 4x4), solo en el inventario creativo.
                parameters.holders().lookup(Registries.PAINTING_VARIANT).ifPresent(lookup -> {
                    for (int w = 1; w <= 4; w++) {
                        for (int h = 1; h <= 4; h++) {
                            final int ww = w, hh = h;
                            var key = ResourceKey.create(Registries.PAINTING_VARIANT,
                                    Identifier.fromNamespaceAndPath("kuadro", "k_" + ww + "x" + hh));
                            lookup.get(key).ifPresent(holder -> {
                                ItemStack stack = new ItemStack(Items.PAINTING);
                                CompoundTag tag = new CompoundTag();
                                tag.putInt("kuadro", 1);
                                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                                stack.set(DataComponents.ITEM_NAME, Component.literal("Kuadro " + ww + "x" + hh));
                                stack.set(DataComponents.PAINTING_VARIANT, holder);
                                output.accept(stack);
                            });
                        }
                    }
                });
            }).build());

    public RuletitaMod(FMLJavaModLoadingContext context) {
        var bus = context.getModBusGroup();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        SOUNDS.register(bus);
        TABS.register(bus);

        RegisterCommandsEvent.BUS.addListener(Eventos::registerCommands);
        TickEvent.PlayerTickEvent.Post.BUS.addListener(Eventos::onPlayerTick);
        LivingEntityUseItemEvent.Start.BUS.addListener(Eventos::onUseStart);
        LivingEntityUseItemEvent.Finish.BUS.addListener(Eventos::onUseFinish);
        PlayerInteractEvent.RightClickItem.BUS.addListener(Eventos::onRightClickItem);
        PlayerSleepInBedEvent.BUS.addListener(Eventos::onSleep);
        net.minecraftforge.event.village.VillagerTradesEvent.BUS.addListener(Eventos::onVillagerTrades);
    }
}
