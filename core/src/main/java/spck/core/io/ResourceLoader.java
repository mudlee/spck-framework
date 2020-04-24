package spck.core.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.memAlloc;

public class ResourceLoader {
    private static final Logger log = LoggerFactory.getLogger(ResourceLoader.class);

    public static ByteBuffer load(String path){
        try {
            URL url = ResourceLoader.class.getResource(path);

            if (url == null) {
                throw new RuntimeException("Resource not found: " + path);
            }

            int resourceSize = url.openConnection().getContentLength();

            log.debug("Loading resource '{}' ({}bytes)", url.getFile(), resourceSize);

            ByteBuffer resource = memAlloc(resourceSize);

            try (BufferedInputStream bis = new BufferedInputStream(url.openStream())) {
                int b;
                do {
                    b = bis.read();
                    if (b != -1) {
                        resource.put((byte) b);
                    }
                } while (b != -1);
            }

            resource.flip();

            return resource;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
