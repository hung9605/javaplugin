package anotation;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("anotation.MyMapper")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MyMapperProcessor extends AbstractProcessor {
	
	private Filer filer;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.filer = processingEnv.getFiler();
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<? extends Element> lstUse =  roundEnv.getElementsAnnotatedWith(MyMapper.class);
		for(Element element: lstUse) {
			if(element.getKind() == ElementKind.INTERFACE) {
				TypeElement typeElement = (TypeElement) element;
				String interfaceName = typeElement.getSimpleName().toString();
				String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
				processingEnv.getMessager().printMessage(Kind.NOTE, "Starting .....");
				
				try {
					JavaFileObject jFileObject = filer.createSourceFile(packageName + "." + interfaceName +"Impl");
					try(Writer writer = jFileObject.openWriter()){
					writer.write("package " + packageName +"; \n\n");
					writer.write("public class " + interfaceName +"Impl implements "+interfaceName +" { \n\n");
					
					List<ExecutableElement> lstField =  ElementFilter.methodsIn(typeElement.getEnclosedElements());
					lstField.stream().forEach(item -> {
						String methodName = item.getSimpleName().toString();
						String returnType = item.getReturnType().toString();
						VariableElement param = item.getParameters().get(0);
						String paramName = param.getSimpleName().toString();
						String paramType = param.asType().toString();
						TypeElement input = processingEnv.getElementUtils().getTypeElement(paramType);
						TypeElement output = processingEnv.getElementUtils().getTypeElement(returnType);
						List<VariableElement> inputVariableElements = ElementFilter.fieldsIn(input.getEnclosedElements());
						List<VariableElement> outputVariableElements = ElementFilter.fieldsIn(output.getEnclosedElements());
						try {
							writer.write("@Override  \n");
							writer.write("public " + returnType +" " + methodName + "(" + paramType +" " + paramName + " ){ \n");
							writer.write(" if( null == "+paramName + ") return null ; \n");
							writer.write("      " + returnType +" target = new " + returnType + "(); \n");
							for (VariableElement outField : outputVariableElements) {
								String fieldName = outField.getSimpleName().toString();
								for (VariableElement inField : inputVariableElements) {
									if(inField.getSimpleName().toString().equals(fieldName)
											&& inField.asType().toString().equals(outField.asType().toString()) ) {
										 String methodSuffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
										 writer.write(" target.set"+methodSuffix+"("+paramName+".get"+methodSuffix+"()); \n");
									}
								}
							}
						
							writer.write("        return target;\n");
							writer.write("} \n\n");
							
						} catch (IOException e) {
							
						}
						
					});
					writer.write("} \n\n");
					}
				} catch (Exception e) {
			
				}
				
			}
		}
		processingEnv.getMessager().printMessage(Kind.NOTE, "Mapper .....");
		return false;
	}

}
