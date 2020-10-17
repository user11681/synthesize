package user11681.jpp.synthesis;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.metadata.EntrypointMetadata;
import net.fabricmc.loader.metadata.ModMetadataV1;
import net.gudenau.lib.unsafe.Unsafe;
import user11681.reflect.Accessor;
import user11681.reflect.Classes;
import user11681.reflect.Invoker;

@SuppressWarnings({"deprecation", "OptionalGetWithoutIsPresent"})
public class MetadataGenerator {
    private static final Map<String, LanguageAdapter> adapterMap = Accessor.getObject(FabricLoader.INSTANCE, "adapterMap");

    private static final MethodHandle metadataConstructor = Invoker.findConstructor(Classes.load("net.fabricmc.loader.metadata.ModMetadataV1$EntrypointContainer$Metadata"), MethodType.methodType(void.class, String.class, String.class));
    private static final MethodHandle entrypointStorageAdd = Invoker.bind(Accessor.getObject(FabricLoader.INSTANCE, "entrypointStorage"), "add", MethodType.methodType(void.class, ModContainer.class, String.class, EntrypointMetadata.class, Map.class));

    public static void addEntrypoint(final String modID, final String entrypoint, final String adapter, final String target) {
        try {
            final ModContainer mod = (ModContainer) JppTransformer.fabric.getModContainer(modID).get();
            final ModMetadataV1 modMetadata = (ModMetadataV1) mod.getMetadata();

            List<EntrypointMetadata> entrypoints = modMetadata.getEntrypoints(entrypoint);

            if (entrypoints.isEmpty()) {
                entrypoints = ReferenceArrayList.wrap(new EntrypointMetadata[1], 0);

                ((Map<String, List<EntrypointMetadata>>) Accessor.getObject((Object) Accessor.getObject(mod.getMetadata(), "entrypoints"), "metadataMap")).put(entrypoint, entrypoints);
            } else {
                for (final EntrypointMetadata entrypointMetadata : entrypoints) {
                    if (entrypointMetadata.getValue().equals(target)) {
                        return;
                    }
                }
            }

            final EntrypointMetadata entrypointMetadata = (EntrypointMetadata) metadataConstructor.invoke(adapter, target);

            entrypoints.add(entrypointMetadata);
            entrypointStorageAdd.invokeExact(mod, entrypoint, entrypointMetadata, adapterMap);
        } catch (final Throwable throwable) {
            throw Unsafe.throwException(throwable);
        }
    }
}
