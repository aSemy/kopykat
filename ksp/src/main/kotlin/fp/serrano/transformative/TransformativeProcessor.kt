package fp.serrano.transformative

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import fp.serrano.transformative.utils.hasGeneratedMarker

internal class TransformativeProcessor(private val codegen: CodeGenerator, private val logger: KSPLogger) :
  SymbolProcessor {

  override fun process(resolver: Resolver): List<KSAnnotated> {
    with(resolver.getAllFiles()) {
      if (none { file -> file.hasGeneratedMarker() }) {
        flatMap { file -> file.declarations }
          .filterIsInstance<KSClassDeclaration>()
          .forEach {
            logger.logging("Processing ${it.simpleName}", it)
            it.process()
          }
      }
    }
    return emptyList()
  }


  private fun KSClassDeclaration.process() {
    when {
      isDataClass() -> listOf(TransformFunctionKt, MutableCopyKt).writeAllTo(codegen)
    }
  }
}

private fun KSClassDeclaration.isDataClass() =
  Modifier.DATA in modifiers && primaryConstructor != null