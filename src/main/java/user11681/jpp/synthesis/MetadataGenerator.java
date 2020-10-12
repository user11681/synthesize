package user11681.jpp.synthesis;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;
import net.fabricmc.loader.metadata.EntrypointMetadata;
import net.fabricmc.loader.metadata.ModMetadataV1;
import user11681.reflect.Accessor;
import user11681.reflect.Classes;
import user11681.reflect.Invoker;

public class MetadataGenerator {
    private static final MethodHandle metadataConstructor = Invoker.findConstructor(Classes.load("net.fabricmc.loader.metadata.ModMetadataV1$EntrypointContainer$Metadata"), MethodType.methodType(void.class, String.class, String.class));

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static void addEntrypoint(final String modID, final String entrypoint, final String adapter, final String target) {
        List<EntrypointMetadata> entrypoints = ((ModMetadataV1) Jpp.fabric.getModContainer(modID).get().getMetadata()).getEntrypoints(entrypoint);

        if (entrypoints.isEmpty()) {
            entrypoints = ReferenceArrayList.wrap(new EntrypointMetadata[1], 0);

            Accessor.putObject(Jpp.fabric.getModContainer(modID).get().getMetadata(), "entrypoints", entrypoints);
        } else {
            for (final EntrypointMetadata metadata : entrypoints) {
                if (metadata.getValue().equals(target)) {
                    return;
                }
            }
        }

        try {
            entrypoints.add((EntrypointMetadata) metadataConstructor.invoke(adapter, target));
        } catch (final Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
