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
				MyMapper annotation = element.getAnnotation(MyMapper.class);
				boolean useSpring = annotation.componentModelSpring();
				try {
					JavaFileObject jFileObject = filer.createSourceFile(packageName + "." + interfaceName +"Impl");
					try(Writer writer = jFileObject.openWriter()){
					writer.write("package " + packageName +"; \n\n");
					
					if(useSpring) {
						writer.write("import org.springframework.stereotype.Component;\n\n");
						writer.write("@Component \n");
						
					}
					writer.write("public class " + interfaceName +"Impl implements "+interfaceName +" { \n\n");
					List<ExecutableElement> lstField =  ElementFilter.methodsIn(typeElement.getEnclosedElements());
					lstField.stream().forEach(item -> {
						String methodName = item.getSimpleName().toString();
						String returnType = item.getReturnType().toString();
						VariableElement param = item.getParameters().get(0);
						String paramName = param.getSimpleName().toString();
						String paramType = param.asType().toString();
						boolean isReturnList = returnType.startsWith("java.util.List");
						boolean isParamList = paramType.startsWith("java.util.List");
					
						try {
							writer.write("@Override  \n");
							writer.write("public " + returnType +" " + methodName + "(" + paramType +" " + paramName + " ){ \n");
							writer.write(" if( null == "+paramName + ") return null ; \n");
						if (isParamList && isReturnList) {
						   String inputType = paramType.substring(paramType.indexOf("<") + 1, paramType.lastIndexOf(">"));
						   String outputType = returnType.substring(returnType.indexOf("<") + 1, returnType.lastIndexOf(">"));
						   writer.write("    java.util.List<" + outputType + "> result = new java.util.ArrayList<>();\n");
				           writer.write("    for (" + inputType + " item : " + paramName + ") {\n");
				           writer.write("        if(item != null){\n");
				           writer.write("            " + outputType + " target = new " + outputType + "();\n");
				           // Get field elements
				            TypeElement input = processingEnv.getElementUtils().getTypeElement(inputType);
				            TypeElement output = processingEnv.getElementUtils().getTypeElement(outputType);
				            List<VariableElement> inputFields = ElementFilter.fieldsIn(input.getEnclosedElements());
				            List<VariableElement> outputFields = ElementFilter.fieldsIn(output.getEnclosedElements());

				            for (VariableElement outField : outputFields) {
				                String fieldName = outField.getSimpleName().toString();
				                for (VariableElement inField : inputFields) {
				                    if (inField.getSimpleName().toString().equals(fieldName)
				                            && inField.asType().toString().equals(outField.asType().toString())) {
				                        String methodSuffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
				                        writer.write("            target.set" + methodSuffix + "(item.get" + methodSuffix + "());\n");
				                    }
				                }
				            }

				            writer.write("            result.add(target);\n");
				            writer.write("        }\n");
				            writer.write("    }\n");
				            writer.write("    return result;\n");
				            writer.write("}\n\n");
						}else {
							TypeElement input = processingEnv.getElementUtils().getTypeElement(paramType);
					        TypeElement output = processingEnv.getElementUtils().getTypeElement(returnType);
					    	List<VariableElement> inputVariableElements = ElementFilter.fieldsIn(input.getEnclosedElements());
							List<VariableElement> outputVariableElements = ElementFilter.fieldsIn(output.getEnclosedElements());
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
							 }
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
