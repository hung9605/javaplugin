package anotation;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("anotation.MyGetterSetter")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MyGetterSetterProcessor extends AbstractProcessor {
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element elem : roundEnv.getElementsAnnotatedWith(MyGetterSetter.class)) {
            if (elem.getKind() == ElementKind.CLASS) {
                TypeElement classElement = (TypeElement) elem;
                String className = classElement.getSimpleName().toString();
                String packageName = processingEnv.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();
                try {
                    JavaFileObject jfo = filer.createSourceFile(packageName + "." + className);
                    try (Writer writer = jfo.openWriter()) {
                        writer.write("package " + packageName + ";\n\n");
                        writer.write("public class " + className +" {\n");

                        for (VariableElement field : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
                            String fieldName = field.getSimpleName().toString();
                            String fieldType = field.asType().toString();
                            String methodSuffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

                            writer.write("    public " + fieldType + " get" + methodSuffix + "() {\n");
                            writer.write("        return this." + fieldName + ";\n");
                            writer.write("    }\n");

                            writer.write("    public void set" + methodSuffix + "(" + fieldType + " " + fieldName + ") {\n");
                            writer.write("        this." + fieldName + " = " + fieldName + ";\n");
                            writer.write("    }\n");
                        }
                        writer.write("}\n");
                    }
                } catch (Exception e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error: " + e.getMessage());
                }
            }
        }
        return true;
    }
}
