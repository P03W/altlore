package mc.altlore.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class Altlore implements ClientModInitializer {
    public static KeyBinding keyBinding;
    public static HashMap<Item, String[]> lore = new HashMap<Item, String[]>();
    
    @Override
    public void onInitializeClient() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.altlore.show",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_ALT,
            "category.altlore.keybinds"
        ));
        
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {
                @Override
                public void reload(ResourceManager manager) {
                    lore.clear();
                    for(Identifier id : manager.findResources("altlore", path -> path.endsWith(".altlore"))) {
                        try(InputStream stream = manager.getResource(id).getInputStream()) {
                            final char[] buffer = new char[8192];
                            final StringBuilder result = new StringBuilder();
                            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                                int charsRead;
                                while ((charsRead = reader.read(buffer, 0, buffer.length)) > 0) {
                                    result.append(buffer, 0, charsRead);
                                }
        
                                String[] text = result.toString().split("\n");
                                handleLines(text);
                            }
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
    
                private void handleLines(String[] text) {
                    Arrays.stream(text).forEach(s -> {
                        String[] slice = s.split("=");
                        Identifier itemID = new Identifier(slice[0]);
                        Item item = Registry.ITEM.get(itemID);
                        if (item == Items.AIR) {
                            System.err.println("Unable to find item " + itemID);
                        } else {
                            String loreTextRaw = Arrays.stream(slice)
                                .skip(1)
                                .collect(Collectors.joining("="))
                                .replaceAll("\n", "")
                                .replaceAll("\r", "");
                            lore.put(item, loreTextRaw.split("\\\\n"));
                        }
                    });
                }
    
                @Override
                public Identifier getFabricId() {
                    return new Identifier("altlore", "lore");
                }
            });
    }
}
