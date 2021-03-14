package spck.core.renderer.backend;

public class ShadercCompilationStatus {
  public static String translate(int code) {
    switch (code) {
      case 0:
        return "shaderc_compilation_status_success";
      case 1:
        return "shaderc_compilation_status_invalid_stage";
      case 2:
        return "shaderc_compilation_status_compilation_error";
      case 3:
        return "shaderc_compilation_status_internal_error";
      case 4:
        return "shaderc_compilation_status_null_result_object";
      case 5:
        return "shaderc_compilation_status_invalid_assembly";
      case 6:
        return "shaderc_compilation_status_validation_error";
      case 7:
        return "shaderc_compilation_status_transformation_error";
      case 8:
        return "shaderc_compilation_status_configuration_error";
      default:
        throw new RuntimeException("Unknown compilation status: "+code);
    }
  }
}
