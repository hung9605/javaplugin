package anotation;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

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
				String className = typeElement.getSimpleName().toString();
				String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
				processingEnv.getMessager().printMessage(Kind.NOTE, "Starting .....");
				
				try {
					JavaFileObject jFileObject = filer.createSourceFile(packageName + "." + className +"impl");
					Writer writer = jFileObject.openWriter();
					writer.write("package " + packageName +" \n\n");
					writer.write("pulbic Class " + className +"Impl{ \n\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		return false;
	}

}
