package spck.core.renderer.backend;

import org.lwjgl.util.shaderc.Shaderc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SPIRVCompiler {
  private static final Logger log = LoggerFactory.getLogger(SPIRVCompiler.class);

  public static SPIRV compile(ByteBuffer shader, int kind, String name){
    log.debug("Compiling shader {} to SPIR-V...", name);

    try (final var stack = stackPush()) {
      final var compiler = Shaderc.shaderc_compiler_initialize();
      final var result = Shaderc.shaderc_compile_into_spv(
          compiler,
          shader,
          kind,
          stack.UTF8(name, true),
          stack.UTF8("main", true),
          NULL
      );

      final var warnings = Shaderc.shaderc_result_get_num_warnings(result);
      final var errors = Shaderc.shaderc_result_get_num_errors(result);
      final var status = Shaderc.shaderc_result_get_compilation_status(result);

      log.debug("* {} warning, {} error", warnings, errors);
      log.debug("* Compilation status: {}", ShadercCompilationStatus.translate(status));

      if (status != Shaderc.shaderc_compilation_status_success) {
        log.error("* Shader {} compilation failed: {}", name, Shaderc.shaderc_result_get_error_message(result));
        throw new RuntimeException("Shader compilation failed");
      }

      final var spirv = Shaderc.shaderc_result_get_bytes(result);
      Shaderc.shaderc_compiler_release(compiler);

      byte[] bytes = new byte[spirv.remaining()];
      spirv.get(bytes);
      return new SPIRV(bytes, result);
    }
  }
}
